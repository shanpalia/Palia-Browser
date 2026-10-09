package com.example.download.manager

import android.content.Context
import android.os.Environment
import com.example.data.db.DownloadDao
import com.example.data.db.PaliaDatabase
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.repository.SettingsRepository
import com.example.download.engine.DownloadEngine
import com.example.download.notification.DownloadNotificationHelper
import com.example.download.service.DownloadService
import com.example.download.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class PaliaDownloadManager private constructor(private val context: Context) {

    private val db = PaliaDatabase.getDatabase(context)
    private val downloadDao: DownloadDao = db.downloadDao()
    private val settingsRepo = SettingsRepository(context)
    private val notificationHelper = DownloadNotificationHelper(context)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val activeEngines = ConcurrentHashMap<Long, DownloadEngine>()
    private val activeJobs = ConcurrentHashMap<Long, Job>()

    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads()

    companion object {
        @Volatile
        private var INSTANCE: PaliaDownloadManager? = null

        fun getInstance(context: Context): PaliaDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PaliaDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        // Startup recovery: any download marked DOWNLOADING is reset to PAUSED
        scope.launch {
            val active = downloadDao.getActiveDownloads()
            for (item in active) {
                if (item.status == DownloadStatus.DOWNLOADING) {
                    downloadDao.updateStatus(item.id, DownloadStatus.PAUSED, null, null)
                }
            }
        }
    }

    suspend fun enqueueDownload(
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
        customFileName: String? = null
    ): Long {
        val detectedFileName = customFileName ?: FileUtils.extractFileName(url, contentDisposition, mimeType)
        val ext = FileUtils.getExtension(detectedFileName)
        val finalMimeType = mimeType ?: FileUtils.getMimeType(detectedFileName)

        // Save files into user-accessible external app download dir
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads").also { it.mkdirs() }

        var targetFile = File(downloadDir, detectedFileName)
        var counter = 1
        val nameWithoutExt = detectedFileName.substringBeforeLast('.', detectedFileName)
        val extensionWithDot = if (detectedFileName.contains('.')) ".${detectedFileName.substringAfterLast('.')}" else ""

        while (targetFile.exists()) {
            targetFile = File(downloadDir, "$nameWithoutExt($counter)$extensionWithDot")
            counter++
        }

        val item = DownloadItem(
            url = url,
            fileName = targetFile.name,
            fileExtension = ext,
            mimeType = finalMimeType,
            localFilePath = targetFile.absolutePath,
            status = DownloadStatus.WAITING
        )

        val id = downloadDao.insertDownload(item)
        triggerDownloadQueue()
        DownloadService.startService(context)
        return id
    }

    fun pauseDownload(id: Long) {
        // Let the engine finish its current read/write cycle and persist the partial file.
        // Cancelling the coroutine here can cancel the Room update that records PAUSED,
        // leaving the row stuck in DOWNLOADING and making Resume unreliable.
        val engine = activeEngines[id]
        if (engine != null) {
            engine.pause()
        } else {
            scope.launch {
                downloadDao.updateStatus(id, DownloadStatus.PAUSED)
                triggerDownloadQueue()
            }
        }
    }

    fun resumeDownload(id: Long) {
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.WAITING, null)
            triggerDownloadQueue()
            DownloadService.startService(context)
        }
    }

    fun cancelDownload(id: Long) {
        val engine = activeEngines[id]
        if (engine != null) {
            // The engine observes this flag and performs cleanup on its own coroutine.
            engine.cancel()
            return
        }

        // Waiting downloads have no active engine, so they can be cancelled directly.
        scope.launch {
            val item = downloadDao.getDownloadByIdSync(id)
            if (item != null) {
                try { File(item.localFilePath).delete() } catch (_: Exception) {}
            }
            downloadDao.updateStatus(id, DownloadStatus.CANCELLED)
            checkServiceState()
            triggerDownloadQueue()
        }
    }

    fun retryDownload(id: Long) {
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.WAITING, null)
            triggerDownloadQueue()
            DownloadService.startService(context)
        }
    }

    fun deleteDownload(id: Long, deleteFile: Boolean) {
        pauseDownload(id)
        scope.launch {
            if (deleteFile) {
                val item = downloadDao.getDownloadByIdSync(id)
                if (item != null) {
                    try { File(item.localFilePath).delete() } catch (_: Exception) {}
                }
            }
            downloadDao.deleteDownloadById(id)
        }
    }

    fun clearCompleted() {
        scope.launch {
            downloadDao.clearCompleted()
        }
    }

    @Synchronized
    fun triggerDownloadQueue() {
        scope.launch {
            val maxConcurrent = settingsRepo.settings.value.maxSimultaneousDownloads
            val currentActiveCount = activeJobs.size

            if (currentActiveCount >= maxConcurrent) {
                return@launch
            }

            val waitingItems = downloadDao.getActiveDownloads()
                .filter { it.status == DownloadStatus.WAITING }

            val slotsAvailable = maxConcurrent - currentActiveCount
            val itemsToStart = waitingItems.take(slotsAvailable)

            for (item in itemsToStart) {
                startDownloadItem(item)
            }
        }
    }

    private fun startDownloadItem(item: DownloadItem) {
        if (activeJobs.containsKey(item.id)) return

        val engine = DownloadEngine()
        activeEngines[item.id] = engine

        val job = scope.launch(start = CoroutineStart.LAZY) {
            engine.executeDownload(
                item = item,
                onProgress = { downloaded, total, speed, etaSeconds, isResumable ->
                    downloadDao.updateProgress(
                        id = item.id,
                        downloadedBytes = downloaded,
                        totalBytes = total,
                        speedBytesPerSec = speed,
                        etaSeconds = etaSeconds,
                        status = DownloadStatus.DOWNLOADING
                    )
                    // Update foreground notification
                    if (settingsRepo.settings.value.showDownloadNotifications) {
                        val updated = item.copy(
                            downloadedBytes = downloaded,
                            totalBytes = total,
                            speedBytesPerSec = speed,
                            etaSeconds = etaSeconds,
                            status = DownloadStatus.DOWNLOADING,
                            isResumable = isResumable
                        )
                        DownloadService.updateNotification(context, updated, activeJobs.size)
                    }
                },
                onStatusChange = { status, errorMsg ->
                    val completedAt = if (status == DownloadStatus.COMPLETED) System.currentTimeMillis() else null
                    downloadDao.updateStatus(item.id, status, errorMsg, completedAt)

                    activeEngines.remove(item.id)
                    activeJobs.remove(item.id)

                    if (status == DownloadStatus.COMPLETED) {
                        val finishedItem = downloadDao.getDownloadByIdSync(item.id) ?: item
                        publishToPublicDownloads(finishedItem)
                        if (settingsRepo.settings.value.showDownloadNotifications) {
                            notificationHelper.showCompletionNotification(finishedItem)
                        }
                    }

                    checkServiceState()
                    triggerDownloadQueue()
                }
            )
        }
        activeJobs[item.id] = job
        job.start()
    }

    /**
     * Publish the finished file to the user's Downloads folder on Android 10+.
     * The app keeps its working copy so pause/resume and in-app Open/Share remain reliable.
     */
    private fun publishToPublicDownloads(item: DownloadItem) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) return
        val source = File(item.localFilePath)
        if (!source.isFile || source.length() == 0L) return

        val resolver = context.contentResolver
        val relativePath = Environment.DIRECTORY_DOWNLOADS + "/Palia Browser"
        val existing = resolver.query(
            android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(android.provider.MediaStore.Downloads._ID),
            "${android.provider.MediaStore.Downloads.DISPLAY_NAME}=? AND ${android.provider.MediaStore.Downloads.RELATIVE_PATH}=?",
            arrayOf(item.fileName, relativePath),
            null
        )
        existing?.use { cursor ->
            if (cursor.moveToFirst()) return
        }

        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, item.fileName)
            put(android.provider.MediaStore.Downloads.MIME_TYPE, item.mimeType.ifBlank { "application/octet-stream" })
            put(android.provider.MediaStore.Downloads.RELATIVE_PATH, relativePath)
            put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return
        try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: throw java.io.IOException("Could not open Downloads output stream")
            val publishValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
            }
            resolver.update(uri, publishValues, null, null)
        } catch (_: Exception) {
            // Keep the completed in-app download valid even if public publishing is unavailable.
            resolver.delete(uri, null, null)
        }
    }

    private fun checkServiceState() {
        if (activeJobs.isEmpty()) {
            DownloadService.stopService(context)
        }
    }

    fun hasActiveDownloads(): Boolean = activeJobs.isNotEmpty()

    fun getActiveItemForNotification(): DownloadItem? {
        val activeId = activeJobs.keys.firstOrNull() ?: return null
        return kotlinx.coroutines.runBlocking { downloadDao.getDownloadByIdSync(activeId) }
    }
}
