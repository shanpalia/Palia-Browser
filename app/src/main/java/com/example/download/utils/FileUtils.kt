package com.example.download.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLDecoder
import java.util.Locale

object FileUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0

        return when {
            bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb)
            bytes >= mb -> String.format(Locale.US, "%.2f MB", bytes / mb)
            bytes >= kb -> String.format(Locale.US, "%.1f KB", bytes / kb)
            else -> "$bytes B"
        }
    }

    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 B/s"
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0

        return when {
            bytesPerSec >= gb -> String.format(Locale.US, "%.2f GB/s", bytesPerSec / gb)
            bytesPerSec >= mb -> String.format(Locale.US, "%.1f MB/s", bytesPerSec / mb)
            bytesPerSec >= kb -> String.format(Locale.US, "%.0f KB/s", bytesPerSec / kb)
            else -> "$bytesPerSec B/s"
        }
    }

    fun formatEta(seconds: Long): String {
        if (seconds < 0) return "--:-- remaining"
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hrs > 0) {
            String.format(Locale.US, "%02d:%02d:%02d remaining", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d remaining", mins, secs)
        }
    }

    fun extractFileName(url: String, contentDisposition: String? = null, mimeType: String? = null): String {
        var filename: String? = null

        // 1. Try Content-Disposition header
        if (!contentDisposition.isNullOrBlank()) {
            val cd = contentDisposition.trim()
            val filenamePattern = Regex("""filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?""", RegexOption.IGNORE_CASE)
            val match = filenamePattern.find(cd)
            if (match != null && match.groupValues.size > 1) {
                try {
                    filename = URLDecoder.decode(match.groupValues[1].trim(), "UTF-8")
                } catch (_: Exception) {
                    filename = match.groupValues[1].trim()
                }
            }
        }

        // 2. Try URL path
        if (filename.isNullOrBlank()) {
            try {
                val cleanUrl = url.split("?").first().split("#").first()
                val lastSlash = cleanUrl.lastIndexOf('/')
                if (lastSlash >= 0 && lastSlash < cleanUrl.length - 1) {
                    val potentialName = cleanUrl.substring(lastSlash + 1)
                    if (potentialName.isNotBlank() && potentialName.contains(".")) {
                        filename = URLDecoder.decode(potentialName, "UTF-8")
                    }
                }
            } catch (_: Exception) {}
        }

        // 3. Fallback name with extension from mime type
        if (filename.isNullOrBlank() || filename.length < 2) {
            val ext = if (!mimeType.isNullOrBlank()) {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            } else {
                "bin"
            }
            filename = "download_${System.currentTimeMillis()}.$ext"
        }

        // Clean filename of unsafe path characters
        return filename.replace(Regex("""[\\/:*?"<>|]"""), "_")
    }

    fun getExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot >= 0 && lastDot < fileName.length - 1) {
            fileName.substring(lastDot + 1).lowercase(Locale.US)
        } else {
            ""
        }
    }

    fun getMimeType(fileName: String): String {
        val ext = getExtension(fileName)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }

    fun openFile(context: Context, localFilePath: String) {
        val file = File(localFilePath)
        if (!file.exists()) return

        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val mimeType = getMimeType(file.name)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }

    fun shareFile(context: Context, localFilePath: String) {
        val file = File(localFilePath)
        if (!file.exists()) return

        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val mimeType = getMimeType(file.name)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share file via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
