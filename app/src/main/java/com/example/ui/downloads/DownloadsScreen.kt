package com.example.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.download.utils.FileUtils
import com.example.ui.BrowserViewModel
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaCyan
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPaused
import com.example.ui.theme.StatusWaiting

enum class DownloadFilterTab(val label: String) {
    ALL("All"),
    DOWNLOADING("Downloading"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    FAILED("Failed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(DownloadFilterTab.ALL) }
    var selectedItemForDetail by remember { mutableStateOf<DownloadItem?>(null) }
    var showAddUrlDialog by remember { mutableStateOf(false) }

    val filteredList = remember(downloads, selectedFilter) {
        when (selectedFilter) {
            DownloadFilterTab.ALL -> downloads
            DownloadFilterTab.DOWNLOADING -> downloads.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.WAITING }
            DownloadFilterTab.PAUSED -> downloads.filter { it.status == DownloadStatus.PAUSED }
            DownloadFilterTab.COMPLETED -> downloads.filter { it.status == DownloadStatus.COMPLETED }
            DownloadFilterTab.FAILED -> downloads.filter { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELLED }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = DownloadGreen,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloads",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { showAddUrlDialog = true },
                                modifier = Modifier.testTag("add_download_url_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Download URL",
                                    tint = PaliaCyan
                                )
                            }

                            if (downloads.any { it.status == DownloadStatus.COMPLETED }) {
                                IconButton(
                                    onClick = { viewModel.clearCompletedDownloads() },
                                    modifier = Modifier.testTag("clear_completed_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ClearAll,
                                        contentDescription = "Clear completed",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(DownloadFilterTab.values()) { filter ->
                            val isSelected = filter == selectedFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PaliaCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = PaliaCyan
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = PaliaCyan.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No downloads in ${selectedFilter.label}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showAddUrlDialog = true },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PaliaCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Download URL", color = PaliaCyan)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    DownloadCard(
                        item = item,
                        onClick = { selectedItemForDetail = item },
                        onPause = { viewModel.pauseDownload(item.id) },
                        onResume = { viewModel.resumeDownload(item.id) },
                        onCancel = { viewModel.cancelDownload(item.id) },
                        onRetry = { viewModel.retryDownload(item.id) },
                        onOpen = { FileUtils.openFile(context, item.localFilePath) },
                        onShare = { FileUtils.shareFile(context, item.localFilePath) },
                        onDelete = { viewModel.deleteDownload(item.id, deleteFile = true) }
                    )
                }
            }
        }
    }

    // Details Dialog
    selectedItemForDetail?.let { item ->
        DownloadDetailsDialog(
            item = item,
            onDismiss = { selectedItemForDetail = null },
            onPause = { viewModel.pauseDownload(item.id) },
            onResume = { viewModel.resumeDownload(item.id) },
            onCancel = { viewModel.cancelDownload(item.id) },
            onRetry = { viewModel.retryDownload(item.id) },
            onDelete = { viewModel.deleteDownload(item.id, deleteFile = true) }
        )
    }

    // Add Download URL Dialog
    if (showAddUrlDialog) {
        var inputUrl by remember { mutableStateOf("") }
        var customFileName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddUrlDialog = false },
            title = { Text("Add Download URL", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Download Link (HTTP/HTTPS)") },
                        placeholder = { Text("https://example.com/file.zip") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_url_field")
                    )
                    OutlinedTextField(
                        value = customFileName,
                        onValueChange = { customFileName = it },
                        label = { Text("Custom File Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            viewModel.confirmDownload(
                                url = inputUrl.trim(),
                                customFileName = customFileName.ifBlank { null }
                            )
                            showAddUrlDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DownloadGreen),
                    modifier = Modifier.testTag("confirm_add_download_button")
                ) {
                    Text("Start Download", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DownloadCard(
    item: DownloadItem,
    onClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = when (item.status) {
                    DownloadStatus.DOWNLOADING -> PaliaCyan.copy(alpha = 0.5f)
                    DownloadStatus.COMPLETED -> DownloadGreen.copy(alpha = 0.4f)
                    DownloadStatus.FAILED -> StatusFailed.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("download_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon + File Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File Type Emoji / Icon Badge
                val typeEmoji = when (item.fileExtension.lowercase()) {
                    "mp4", "mkv", "avi", "mov" -> "🎬"
                    "mp3", "wav", "flac", "aac" -> "🎵"
                    "pdf", "doc", "docx" -> "📄"
                    "zip", "rar", "7z", "tar" -> "📦"
                    "apk" -> "📱"
                    "jpg", "jpeg", "png", "webp", "gif" -> "🖼️"
                    "xls", "xlsx", "csv" -> "📊"
                    else -> "📁"
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (item.status) {
                                DownloadStatus.COMPLETED -> DownloadGreen.copy(alpha = 0.15f)
                                DownloadStatus.DOWNLOADING -> PaliaCyan.copy(alpha = 0.15f)
                                DownloadStatus.PAUSED -> StatusPaused.copy(alpha = 0.15f)
                                DownloadStatus.FAILED -> StatusFailed.copy(alpha = 0.15f)
                                else -> StatusWaiting.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = typeEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Size and Progress status
                    val downloadedStr = FileUtils.formatFileSize(item.downloadedBytes)
                    val totalStr = if (item.totalBytes > 0) FileUtils.formatFileSize(item.totalBytes) else "Unknown"
                    Text(
                        text = "$downloadedStr / $totalStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Text(
                    text = when (item.status) {
                        DownloadStatus.DOWNLOADING -> "${item.progressPercent}%"
                        DownloadStatus.COMPLETED -> "Done"
                        DownloadStatus.PAUSED -> "Paused"
                        DownloadStatus.WAITING -> "Waiting"
                        DownloadStatus.FAILED -> "Failed"
                        DownloadStatus.CANCELLED -> "Cancelled"
                    },
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

            // Progress Bar (for downloading, paused, waiting)
            if (item.status != DownloadStatus.COMPLETED) {
                LinearProgressIndicator(
                    progress = { if (item.totalBytes > 0) item.progressPercent / 100f else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (item.status) {
                        DownloadStatus.DOWNLOADING -> PaliaCyan
                        DownloadStatus.PAUSED -> StatusPaused
                        DownloadStatus.FAILED -> StatusFailed
                        else -> StatusWaiting
                    },
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }

            // Speed and ETA metrics for active downloading
            if (item.status == DownloadStatus.DOWNLOADING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Speed",
                            tint = PaliaCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = FileUtils.formatSpeed(item.speedBytesPerSec),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = PaliaCyan
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "ETA",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = FileUtils.formatEta(item.etaSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Notice if server does not support resume
            if (!item.isResumable && item.status == DownloadStatus.PAUSED) {
                Text(
                    text = "⚠️ Resume unavailable on this server (HTTP Range not supported)",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusPaused
                )
            }

            // Error notice
            if (!item.errorMessage.isNullOrBlank() && item.status == DownloadStatus.FAILED) {
                Text(
                    text = item.errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusFailed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (item.status) {
                    DownloadStatus.DOWNLOADING -> {
                        Button(
                            onClick = onPause,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaused),
                            modifier = Modifier.testTag("pause_download_${item.id}")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PAUSE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("cancel_download_${item.id}")
                        ) {
                            Text("CANCEL", fontSize = 12.sp)
                        }
                    }

                    DownloadStatus.PAUSED -> {
                        Button(
                            onClick = onResume,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DownloadGreen),
                            modifier = Modifier.testTag("resume_download_${item.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("cancel_download_${item.id}")
                        ) {
                            Text("CANCEL", fontSize = 12.sp)
                        }
                    }

                    DownloadStatus.WAITING -> {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CANCEL", fontSize = 12.sp)
                        }
                    }

                    DownloadStatus.FAILED, DownloadStatus.CANCELLED -> {
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaliaCyan),
                            modifier = Modifier.testTag("retry_download_${item.id}")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RETRY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("DELETE", fontSize = 12.sp)
                        }
                    }

                    DownloadStatus.COMPLETED -> {
                        Button(
                            onClick = onOpen,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DownloadGreen),
                            modifier = Modifier.testTag("open_download_${item.id}")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("OPEN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onClick) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = PaliaCyan)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
