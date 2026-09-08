package com.example.ui.browser

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.browser.model.BrowserTab
import com.example.ui.BrowserViewModel
import com.example.ui.MainNavigationTab
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaCyan

@Composable
fun BrowserScreen(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.bookmarks.collectAsStateWithLifecycle()

    var isEditingUrl by remember { mutableStateOf(false) }
    var urlInput by remember(tab.url) { mutableStateOf(tab.url) }
    var showMenu by remember { mutableStateOf(false) }

    val currentPageBookmarked = remember(tab.url, isBookmarked) {
        isBookmarked.any { it.url == tab.url }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Omnibox / URL Bar
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Omnibox card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                isEditingUrl = true
                                urlInput = if (tab.url == "about:home") "" else tab.url
                            }
                            .testTag("browser_omnibox"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tab.isIncognito) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Incognito",
                                    tint = PaliaCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            } else if (tab.url.startsWith("https://")) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure",
                                    tint = DownloadGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            if (isEditingUrl) {
                                OutlinedTextField(
                                    value = urlInput,
                                    onValueChange = { urlInput = it },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("browser_url_input"),
                                    placeholder = { Text("Search or type URL") },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            viewModel.openUrl(urlInput)
                                            isEditingUrl = false
                                        }
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        isEditingUrl = false
                                        urlInput = tab.url
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                            } else {
                                Text(
                                    text = if (tab.title.isNotBlank()) tab.title else tab.url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Reload / Stop button
                    IconButton(
                        onClick = { viewModel.reload() },
                        modifier = Modifier.testTag("browser_reload_button")
                    ) {
                        Icon(
                            imageVector = if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = if (tab.isLoading) "Stop" else "Reload",
                            tint = PaliaCyan
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("browser_menu_button")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Tab") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = {
                                    viewModel.addNewTab()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("New Incognito Tab") },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = PaliaCyan) },
                                onClick = {
                                    viewModel.addNewTab(isIncognito = true)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (currentPageBookmarked) "Remove Bookmark" else "Add to Bookmarks") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (currentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = PaliaCyan
                                    )
                                },
                                onClick = {
                                    viewModel.toggleBookmarkCurrentPage()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Page") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, tab.url)
                                        putExtra(Intent.EXTRA_SUBJECT, tab.title)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (tab.isDesktopMode) "Mobile Site" else "Desktop Site") },
                                leadingIcon = { Icon(Icons.Default.DesktopWindows, contentDescription = null) },
                                onClick = {
                                    viewModel.toggleDesktopMode()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Find in Page") },
                                leadingIcon = { Icon(Icons.Default.FindInPage, contentDescription = null) },
                                onClick = {
                                    viewModel.setFindActive(true)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Downloads") },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = DownloadGreen) },
                                onClick = {
                                    viewModel.navigateTo(MainNavigationTab.DOWNLOADS)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }

                // Progress indicator during page load
                AnimatedVisibility(
                    visible = tab.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { tab.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = PaliaCyan,
                        trackColor = Color.Transparent
                    )
                }
            }
        }

        // Find in page bar
        if (tab.isFindActive) {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tab.findQuery,
                        onValueChange = { viewModel.searchFindInPage(it) },
                        placeholder = { Text("Find in page...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.findNext() })
                    )

                    IconButton(onClick = { viewModel.findPrevious() }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous")
                    }

                    IconButton(onClick = { viewModel.findNext() }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next")
                    }

                    IconButton(onClick = { viewModel.setFindActive(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close find")
                    }
                }
            }
        }

        // Real WebView
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BrowserWebView(tab = tab, viewModel = viewModel)
        }

        // Bottom Browser Toolbar
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier.testTag("browser_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (tab.canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = { viewModel.goForward() },
                    enabled = tab.canGoForward,
                    modifier = Modifier.testTag("browser_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (tab.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = { viewModel.goHome() },
                    modifier = Modifier.testTag("browser_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = PaliaCyan
                    )
                }

                // Tab Switcher Button with Counter Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.navigateTo(MainNavigationTab.TABS) }
                        .testTag("browser_tab_counter"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tabs.size}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = PaliaCyan
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(MainNavigationTab.DOWNLOADS) },
                    modifier = Modifier.testTag("browser_downloads_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        tint = DownloadGreen
                    )
                }
            }
        }
    }
}
