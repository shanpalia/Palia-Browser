package com.example.download.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.data.model.DownloadItem
import com.example.download.manager.PaliaDownloadManager
import com.example.download.notification.DownloadNotificationHelper

class DownloadService : Service() {

    private lateinit var notificationHelper: DownloadNotificationHelper
    private lateinit var downloadManager: PaliaDownloadManager

    override fun onCreate() {
        super.onCreate()
        notificationHelper = DownloadNotificationHelper(this)
        downloadManager = PaliaDownloadManager.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val downloadId = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1L) ?: -1L

        when (action) {
            ACTION_START -> {
                val activeItem = downloadManager.getActiveItemForNotification()
                if (activeItem != null) {
                    val notif = notificationHelper.buildProgressNotification(activeItem)
                    startForeground(DownloadNotificationHelper.FOREGROUND_NOTIFICATION_ID, notif)
                }
            }
            ACTION_PAUSE -> {
                if (downloadId != -1L) {
                    downloadManager.pauseDownload(downloadId)
                }
            }
            ACTION_RESUME -> {
                if (downloadId != -1L) {
                    downloadManager.resumeDownload(downloadId)
                }
            }
            ACTION_CANCEL -> {
                if (downloadId != -1L) {
                    downloadManager.cancelDownload(downloadId)
                }
            }
            ACTION_UPDATE_NOTIF -> {
                val activeItem = downloadManager.getActiveItemForNotification()
                if (activeItem != null) {
                    val notif = notificationHelper.buildProgressNotification(activeItem)
                    startForeground(DownloadNotificationHelper.FOREGROUND_NOTIFICATION_ID, notif)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.download.action.START"
        const val ACTION_PAUSE = "com.example.download.action.PAUSE"
        const val ACTION_RESUME = "com.example.download.action.RESUME"
        const val ACTION_CANCEL = "com.example.download.action.CANCEL"
        const val ACTION_UPDATE_NOTIF = "com.example.download.action.UPDATE_NOTIF"
        const val ACTION_STOP = "com.example.download.action.STOP"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"

        fun startService(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun updateNotification(context: Context, item: DownloadItem, activeCount: Int) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_UPDATE_NOTIF
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {}
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
