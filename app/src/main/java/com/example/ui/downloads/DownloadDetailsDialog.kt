package com.example.ui.downloads

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.download.utils.FileUtils
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaCyan
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPaused
import com.example.ui.theme.StatusWaiting
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadDetailsDialog(
    item: DownloadItem,
    onDismiss: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    // Animated glow transition for active downloads
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Download Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // File Name & Extension Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (item.status) {
                                    DownloadStatus.COMPLETED -> DownloadGreen.copy(alpha = 0.2f)
                                    DownloadStatus.DOWNLOADING -> PaliaCyan.copy(alpha = 0.2f)
                                    DownloadStatus.PAUSED -> StatusPaused.copy(alpha = 0.2f)
                                    DownloadStatus.FAILED -> StatusFailed.copy(alpha = 0.2f)
                                    else -> StatusWaiting.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.fileExtension.uppercase().take(4).ifBlank { "FILE" },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = when (item.status) {
                                DownloadStatus.COMPLETED -> DownloadGreen
                                DownloadStatus.DOWNLOADING -> PaliaCyan
                                DownloadStatus.PAUSED -> StatusPaused
                                DownloadStatus.FAILED -> StatusFailed
                                else -> StatusWaiting
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.status.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = when (item.status) {
                                DownloadStatus.COMPLETED -> DownloadGreen
                                DownloadStatus.DOWNLOADING -> PaliaCyan
                                DownloadStatus.PAUSED -> StatusPaused
                                DownloadStatus.FAILED -> StatusFailed
                                else -> StatusWaiting
                            }
                        )
                    }
                }

                // Progress Bar & Percentage
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val downloadedStr = FileUtils.formatFileSize(item.downloadedBytes)
                        val totalStr = if (item.totalBytes > 0) FileUtils.formatFileSize(item.totalBytes) else "Unknown"
                        Text(
                            text = "$downloadedStr / $totalStr",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "${item.progressPercent}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PaliaCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { if (item.totalBytes > 0) item.progressPercent / 100f else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when (item.status) {
                            DownloadStatus.COMPLETED -> DownloadGreen
                            DownloadStatus.FAILED -> StatusFailed
                            DownloadStatus.PAUSED -> StatusPaused
                            else -> PaliaCyan
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Detailed Specs Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("Current Speed", FileUtils.formatSpeed(item.speedBytesPerSec))
                        DetailRow("Estimated Remaining", FileUtils.formatEta(item.etaSeconds))
                        DetailRow(
                            "Remaining Amount",
                            if (item.totalBytes > item.downloadedBytes) FileUtils.formatFileSize(item.totalBytes - item.downloadedBytes) else "0 B"
                        )
                        DetailRow(
                            "Resumable Support",
                            if (item.isResumable) "Supported (HTTP Range)" else "Unavailable on this server"
                        )
                        DetailRow("MIME Type", item.mimeType)
                        DetailRow(
                            "Created Date",
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.createdAt))
                        )
                    }
                }

                // URL info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "URL",
                        tint = PaliaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Error message if failed
                if (!item.errorMessage.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusFailed.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = StatusFailed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusFailed
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (item.status) {
                    DownloadStatus.DOWNLOADING -> {
                        Button(
                            onClick = onPause,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaused)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause", color = Color.Black)
                        }
                    }
                    DownloadStatus.PAUSED, DownloadStatus.WAITING -> {
                        Button(
                            onClick = onResume,
                            colors = ButtonDefaults.buttonColors(containerColor = DownloadGreen)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume", color = Color.Black)
                        }
                    }
                    DownloadStatus.FAILED -> {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = PaliaCyan)
                        ) {
                            Text("Retry", color = Color.Black)
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        Button(
                            onClick = {
                                FileUtils.openFile(context, item.localFilePath)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DownloadGreen)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open", color = Color.Black)
                        }
                        OutlinedButton(
                            onClick = {
                                FileUtils.shareFile(context, item.localFilePath)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share")
                        }
                    }
                    else -> {}
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDelete()
                    onDismiss()
                }
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
