package com.example.download.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.DownloadItem
import com.example.download.service.DownloadService
import com.example.download.utils.FileUtils

class DownloadNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Palia Browser Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time download speed and progress"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)

            val completeChannel = NotificationChannel(
                COMPLETE_CHANNEL_ID,
                "Palia Download Completed",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when downloads finish"
            }
            notificationManager.createNotificationChannel(completeChannel)
        }
    }

    fun buildProgressNotification(item: DownloadItem, activeCount: Int = 1): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "downloads")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pause action
        val pauseIntent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_PAUSE
            putExtra(DownloadService.EXTRA_DOWNLOAD_ID, item.id)
        }
        val pausePendingIntent = PendingIntent.getService(
            context,
            (item.id * 10 + 1).toInt(),
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel action
        val cancelIntent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_CANCEL
            putExtra(DownloadService.EXTRA_DOWNLOAD_ID, item.id)
        }
        val cancelPendingIntent = PendingIntent.getService(
            context,
            (item.id * 10 + 2).toInt(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val downloadedStr = FileUtils.formatFileSize(item.downloadedBytes)
        val totalStr = if (item.totalBytes > 0) FileUtils.formatFileSize(item.totalBytes) else "Unknown"
        val speedStr = FileUtils.formatSpeed(item.speedBytesPerSec)
        val percent = item.progressPercent

        val contentText = "$downloadedStr / $totalStr • $speedStr"
        val titleText = if (activeCount > 1) {
            "Downloading ${item.fileName} (+${activeCount - 1} more)"
        } else {
            "Downloading ${item.fileName}"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Palia Browser")
            .setSubText(titleText)
            .setContentText(contentText)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)

        if (item.totalBytes > 0) {
            builder.setProgress(100, percent, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }

    fun showCompletionNotification(item: DownloadItem) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "downloads")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (item.id + 1000).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, COMPLETE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download Complete")
            .setContentText("${item.fileName} (${FileUtils.formatFileSize(item.downloadedBytes)})")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((item.id + 2000).toInt(), notification)
    }

    fun cancelProgressNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "palia_download_channel"
        const val COMPLETE_CHANNEL_ID = "palia_complete_channel"
        const val FOREGROUND_NOTIFICATION_ID = 10101
    }
}
