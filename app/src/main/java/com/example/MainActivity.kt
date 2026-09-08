package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DownloadStatus
import com.example.data.repository.ThemeMode
import com.example.download.utils.FileUtils
import com.example.ui.BrowserViewModel
import com.example.ui.MainNavigationTab
import com.example.ui.bookmarks.BookmarksScreen
import com.example.ui.browser.BrowserScreen
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.tabs.TabsScreen
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaBrowserTheme
import com.example.ui.theme.PaliaCyan

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val isDark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            PaliaBrowserTheme(darkTheme = isDark) {
                PaliaBrowserApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "downloads") {
            viewModel.navigateTo(MainNavigationTab.DOWNLOADS)
        }

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val url = intent.data.toString()
            viewModel.openUrl(url)
        }
    }
}

@Composable
private fun PaliaBrowserSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = com.shanpalia.paliabrowser.R.drawable.palia_browser_icon),
                contentDescription = "Palia Browser",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Palia Browser",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PaliaCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Fast • Safe • Download",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "© Shanpalia",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun PaliaBrowserApp(viewModel: BrowserViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200)
        showSplash = false
    }

    if (showSplash) {
        PaliaBrowserSplash()
        return
    }
    val currentNav by viewModel.currentNav.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val pendingDownloadPrompt by viewModel.pendingDownloadPrompt.collectAsStateWithLifecycle()

    val activeTab = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull()

    // Notification permission request for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Back handling: navigates back in browser history or returns to Home tab
    BackHandler {
        if (currentNav != MainNavigationTab.HOME) {
            viewModel.navigateTo(MainNavigationTab.HOME)
        } else if (activeTab != null && !activeTab.isHome) {
            if (activeTab.canGoBack) {
                viewModel.goBack()
            } else {
                viewModel.goHome()
            }
        }
    }

    // Keep the app navigation visible even while browsing so the mobile UI never collapses.
    val isFullWebBrowsing = false

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom navigation bar when on home dashboard or other screens
            AnimatedVisibility(
                visible = !isFullWebBrowsing,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentNav == MainNavigationTab.HOME,
                        onClick = { viewModel.navigateTo(MainNavigationTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PaliaCyan,
                            selectedTextColor = PaliaCyan,
                            indicatorColor = PaliaCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentNav == MainNavigationTab.TABS,
                        onClick = { viewModel.navigateTo(MainNavigationTab.TABS) },
                        icon = {
                            BadgedBox(badge = {
                                Badge(containerColor = PaliaCyan) {
                                    Text("${tabs.size}", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }) {
                                Icon(Icons.Default.Layers, contentDescription = "Tabs")
                            }
                        },
                        label = { Text("Tabs") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PaliaCyan,
                            selectedTextColor = PaliaCyan,
                            indicatorColor = PaliaCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tabs")
                    )

                    val activeDownloadsCount = downloads.count { it.status == DownloadStatus.DOWNLOADING }
                    NavigationBarItem(
                        selected = currentNav == MainNavigationTab.DOWNLOADS,
                        onClick = { viewModel.navigateTo(MainNavigationTab.DOWNLOADS) },
                        icon = {
                            if (activeDownloadsCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = DownloadGreen) {
                                        Text("$activeDownloadsCount", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }) {
                                    Icon(Icons.Default.Download, contentDescription = "Downloads")
                                }
                            } else {
                                Icon(Icons.Default.Download, contentDescription = "Downloads")
                            }
                        },
                        label = { Text("Downloads") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DownloadGreen,
                            selectedTextColor = DownloadGreen,
                            indicatorColor = DownloadGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_downloads")
                    )

                    NavigationBarItem(
                        selected = currentNav == MainNavigationTab.BOOKMARKS,
                        onClick = { viewModel.navigateTo(MainNavigationTab.BOOKMARKS) },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                        label = { Text("Bookmarks") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PaliaCyan,
                            selectedTextColor = PaliaCyan,
                            indicatorColor = PaliaCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_bookmarks")
                    )

                    NavigationBarItem(
                        selected = currentNav == MainNavigationTab.SETTINGS,
                        onClick = { viewModel.navigateTo(MainNavigationTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PaliaCyan,
                            selectedTextColor = PaliaCyan,
                            indicatorColor = PaliaCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullWebBrowsing) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
        ) {
            when (currentNav) {
                MainNavigationTab.HOME -> {
                    if (activeTab != null && !activeTab.isHome) {
                        BrowserScreen(tab = activeTab, viewModel = viewModel)
                    } else {
                        HomeScreen(viewModel = viewModel)
                    }
                }
                MainNavigationTab.TABS -> {
                    TabsScreen(viewModel = viewModel)
                }
                MainNavigationTab.DOWNLOADS -> {
                    DownloadsScreen(viewModel = viewModel)
                }
                MainNavigationTab.BOOKMARKS -> {
                    BookmarksScreen(viewModel = viewModel)
                }
                MainNavigationTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Download confirmation prompt dialog
    pendingDownloadPrompt?.let { prompt ->
        var editedFileName by remember(prompt.fileName) { mutableStateOf(prompt.fileName) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissDownloadPrompt() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = DownloadGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download File", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editedFileName,
                        onValueChange = { editedFileName = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (prompt.fileSize > 0) {
                        Text(
                            text = "Size: ${FileUtils.formatFileSize(prompt.fileSize)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PaliaCyan
                        )
                    }

                    Text(
                        text = "Source: ${prompt.url}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDownload(
                            url = prompt.url,
                            customFileName = editedFileName.ifBlank { prompt.fileName },
                            mimeType = prompt.mimeType
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DownloadGreen)
                ) {
                    Text("Download", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDownloadPrompt() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

