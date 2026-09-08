package com.example.download.engine

import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DownloadEngine(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    private val isPaused = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)

    fun pause() {
        isPaused.set(true)
    }

    fun cancel() {
        isCancelled.set(true)
    }

    suspend fun executeDownload(
        item: DownloadItem,
        onProgress: suspend (downloaded: Long, total: Long, speed: Long, etaSeconds: Long, isResumable: Boolean) -> Unit,
        onStatusChange: suspend (status: DownloadStatus, errorMessage: String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        isPaused.set(false)
        isCancelled.set(false)

        val targetFile = File(item.localFilePath)
        targetFile.parentFile?.mkdirs()

        var existingBytes = if (targetFile.exists()) targetFile.length() else 0L

        // HTTP Request builder with Range header for resuming
        val requestBuilder = Request.Builder().url(item.url)
        var requestingResume = false

        if (existingBytes > 0L) {
            requestBuilder.addHeader("Range", "bytes=$existingBytes-")
            requestingResume = true
        }

        try {
            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful && response.code != 206) {
                if (response.code == 416) {
                    // Range Not Satisfiable: could already be fully downloaded
                    if (existingBytes > 0 && item.totalBytes > 0 && existingBytes >= item.totalBytes) {
                        onProgress(existingBytes, existingBytes, 0L, 0L, true)
                        onStatusChange(DownloadStatus.COMPLETED, null)
                        return@withContext
                    }
                }
                throw IOException("Server returned HTTP ${response.code}: ${response.message}")
            }

            val responseBody = response.body ?: throw IOException("Empty server response body")
            val contentLength = responseBody.contentLength()

            // Check if server accepted the resume request
            val serverSupportsResume: Boolean
            var totalBytes: Long

            if (requestingResume) {
                if (response.code == 206) {
                    // Range request accepted!
                    serverSupportsResume = true
                    totalBytes = if (contentLength > 0) existingBytes + contentLength else item.totalBytes
                } else {
                    // Server responded with 200 OK instead of 206 Partial Content!
                    // This means server ignored Range header and is streaming from byte 0.
                    serverSupportsResume = false
                    existingBytes = 0L
                    targetFile.delete()
                    totalBytes = contentLength
                }
            } else {
                val acceptRanges = response.header("Accept-Ranges")
                serverSupportsResume = acceptRanges?.equals("bytes", ignoreCase = true) == true || contentLength > 0
                totalBytes = contentLength
            }

            // Check available storage before continuing
            val usableSpace = targetFile.parentFile?.usableSpace ?: Long.MAX_VALUE
            val remainingToDownload = if (totalBytes > existingBytes) totalBytes - existingBytes else 0L
            if (remainingToDownload > 0 && usableSpace < remainingToDownload + 10 * 1024 * 1024) {
                throw IOException("Insufficient storage space: need ${(remainingToDownload / (1024 * 1024))} MB")
            }

            onStatusChange(DownloadStatus.DOWNLOADING, null)

            // RandomAccessFile allows writing from the existingBytes offset
            val randomAccessFile = RandomAccessFile(targetFile, "rw")
            randomAccessFile.seek(existingBytes)

            val inputStream = responseBody.byteStream()
            val buffer = ByteArray(16 * 1024) // 16 KB chunks for high throughput
            var bytesRead: Int

            var currentDownloaded = existingBytes
            var lastProgressTime = System.currentTimeMillis()
            var bytesSinceLastReport = 0L
            var currentSpeed = 0L
            var etaSeconds = -1L

            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isPaused.get() || !coroutineContext.isActive) {
                        randomAccessFile.fd.sync()
                        randomAccessFile.close()
                        inputStream.close()
                        onStatusChange(DownloadStatus.PAUSED, null)
                        return@withContext
                    }

                    if (isCancelled.get()) {
                        randomAccessFile.close()
                        inputStream.close()
                        targetFile.delete()
                        onStatusChange(DownloadStatus.CANCELLED, null)
                        return@withContext
                    }

                    randomAccessFile.write(buffer, 0, bytesRead)
                    currentDownloaded += bytesRead
                    bytesSinceLastReport += bytesRead

                    val now = System.currentTimeMillis()
                    val timeDelta = now - lastProgressTime
                    if (timeDelta >= 800) { // Update progress and speed every ~800ms
                        currentSpeed = ((bytesSinceLastReport * 1000L) / timeDelta).coerceAtLeast(0L)
                        if (totalBytes > 0 && currentSpeed > 0) {
                            val remainingBytes = (totalBytes - currentDownloaded).coerceAtLeast(0L)
                            etaSeconds = remainingBytes / currentSpeed
                        }
                        onProgress(currentDownloaded, totalBytes, currentSpeed, etaSeconds, serverSupportsResume)
                        lastProgressTime = now
                        bytesSinceLastReport = 0L
                    }
                }

                randomAccessFile.fd.sync()
                randomAccessFile.close()
                inputStream.close()

                // Final check: download completed!
                if (totalBytes > 0 && currentDownloaded < totalBytes) {
                    // Incomplete stream
                    throw IOException("Incomplete download: received $currentDownloaded of $totalBytes bytes")
                }

                onProgress(currentDownloaded, currentDownloaded, 0L, 0L, serverSupportsResume)
                onStatusChange(DownloadStatus.COMPLETED, null)

            } catch (e: Exception) {
                try {
                    randomAccessFile.close()
                } catch (_: Exception) {}
                try {
                    inputStream.close()
                } catch (_: Exception) {}
                throw e
            }

        } catch (e: Exception) {
            if (isPaused.get()) {
                onStatusChange(DownloadStatus.PAUSED, null)
            } else if (isCancelled.get()) {
                targetFile.delete()
                onStatusChange(DownloadStatus.CANCELLED, null)
            } else {
                val errorMsg = when (e) {
                    is UnknownHostException -> "Network disconnected. Please check your connection."
                    is SocketTimeoutException -> "Connection timed out. Server is not responding."
                    is ConnectException -> "Server is unavailable or refusing connection."
                    is IOException -> e.message ?: "Download failed due to network error."
                    else -> "Download error: ${e.localizedMessage}"
                }
                onStatusChange(DownloadStatus.FAILED, errorMsg)
            }
        }
    }
}
