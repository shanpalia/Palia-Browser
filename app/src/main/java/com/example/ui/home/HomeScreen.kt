package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QuickShortcutItem
import com.example.ui.BrowserViewModel
import com.example.ui.MainNavigationTab
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaBlue
import com.example.ui.theme.PaliaCyan

private data class Trend(val text: String)

@Composable
fun HomeScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val trends = remember {
        listOf(
            Trend("railway rrb group d answer key"),
            Trend("ssc chsl vacancies"),
            Trend("rbi overnight cash withdrawal auction"),
            Trend("apple iphone 18 pro max"),
            Trend("sunset today")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(com.shanpalia.paliabrowser.R.drawable.palia_browser_icon),
                    contentDescription = "Palia Browser",
                    modifier = Modifier.size(58.dp).clip(RoundedCornerShape(16.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Palia Browser",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text("Fast  •  Safe  •  Powerful", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                IconButton(onClick = { showUpdateDialog = true }) {
                    Icon(Icons.Default.Refresh, "Check for updates", tint = PaliaBlue, modifier = Modifier.size(28.dp))
                }
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Text("1", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("home_search_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PaliaBlue.copy(alpha = .85f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                        Text("G", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = PaliaBlue)
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).testTag("home_search_input"),
                        placeholder = { Text("Search or type URL", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotBlank()) { viewModel.openUrl(searchQuery); searchQuery = "" }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, "Clear") }
                    }
                    Button(
                        onClick = { if (searchQuery.isNotBlank()) { viewModel.openUrl(searchQuery); searchQuery = "" } },
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PaliaBlue),
                        modifier = Modifier.testTag("home_go_button")
                    ) { Text("Go", fontWeight = FontWeight.Bold) }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ShortcutItemView("Google", "G", "https://www.google.com") { viewModel.openUrl("https://www.google.com") }
                ShortcutItemView("YouTube", "▶", "https://www.youtube.com") { viewModel.openUrl("https://www.youtube.com") }
                ShortcutItemView("Facebook", "f", "https://www.facebook.com") { viewModel.openUrl("https://www.facebook.com") }
                ShortcutItemView("Instagram", "◎", "https://www.instagram.com") { viewModel.openUrl("https://www.instagram.com") }
                ShortcutItemView("X", "X", "https://x.com") { viewModel.openUrl("https://x.com") }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp).clickable { showAddShortcutDialog = true }) {
                    Box(Modifier.size(50.dp).clip(CircleShape).background(PaliaBlue.copy(alpha=.10f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, "Add", tint = PaliaBlue, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.height(5.dp)); Text("Add", fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PaliaBlue.copy(alpha = .07f))
            ) {
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    QuickHubButton("Downloads", Icons.Default.Download, DownloadGreen, Modifier.weight(1f)) { viewModel.navigateTo(MainNavigationTab.DOWNLOADS) }
                    QuickHubButton("History", Icons.Default.History, Color(0xFF7C3AED), Modifier.weight(1f)) { viewModel.navigateTo(MainNavigationTab.HOME) }
                    QuickHubButton("Bookmarks", Icons.Default.Bookmark, Color(0xFF10B981), Modifier.weight(1f)) { viewModel.navigateTo(MainNavigationTab.BOOKMARKS) }
                    QuickHubButton("Settings", Icons.Default.Settings, Color(0xFFF59E0B), Modifier.weight(1f)) { viewModel.navigateTo(MainNavigationTab.SETTINGS) }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showUpdateDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PaliaBlue.copy(alpha = .06f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PaliaBlue.copy(alpha = .16f))
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painterResource(com.shanpalia.paliabrowser.R.drawable.palia_browser_icon), null, Modifier.size(58.dp).clip(RoundedCornerShape(14.dp)))
                    Column(Modifier.weight(1f)) {
                        Text("Palia Browser Update Available", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Latest features, performance improvements and bug fixes.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Button(onClick = { showUpdateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = PaliaBlue), shape = RoundedCornerShape(18.dp)) { Text("Update") }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                listOf("Discover", "News", "Sports", "Tech", "Business", "Entertainment").forEachIndexed { index, label ->
                    Text(label, color = if (index == 0) PaliaBlue else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Whatshot, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp)); Text("Trending Searches", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        }
                        Text("See More", color = PaliaBlue, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    trends.forEachIndexed { index, trend ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(34.dp).clip(CircleShape).background(PaliaBlue.copy(alpha=.08f)), contentAlignment = Alignment.Center) { Text("${index + 1}", fontWeight = FontWeight.Bold, color = PaliaBlue) }
                            Spacer(Modifier.width(12.dp))
                            Text(trend.text, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard("Safe Browsing", "Your privacy, our priority", Color(0xFF16A34A), Modifier.weight(1f))
                FeatureCard("Fast Downloads", "Powerful download manager", Color(0xFFEF4444), Modifier.weight(1f))
            }
        }

        if (shortcuts.isNotEmpty()) {
            item { Text("Your Shortcuts", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            items(shortcuts.take(8)) { shortcut -> ShortcutItemView(shortcut, { viewModel.openUrl(shortcut.url) }, { viewModel.deleteShortcut(shortcut.id) }) }
        }

        if (recentHistory.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.History, null, tint = PaliaCyan); Spacer(Modifier.width(6.dp)); Text("Recently Visited", fontWeight = FontWeight.Bold) }
                    TextButton(onClick = { viewModel.clearHistory() }) { Text("Clear") }
                }
            }
            items(recentHistory.take(5)) { historyItem ->
                Card(Modifier.fillMaxWidth().clickable { viewModel.openUrl(historyItem.url) }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.65f))) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(PaliaBlue.copy(alpha=.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Public, null, tint = PaliaCyan) }
                        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(historyItem.title, maxLines=1, overflow=TextOverflow.Ellipsis, fontWeight=FontWeight.Medium); Text(historyItem.url, maxLines=1, overflow=TextOverflow.Ellipsis, color=MaterialTheme.colorScheme.onSurfaceVariant, fontSize=12.sp) }
                        IconButton(onClick = { viewModel.deleteHistory(historyItem.id) }) { Icon(Icons.Default.Close, "Remove") }
                    }
                }
            }
        }

        item {
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Palia Browser", fontWeight = FontWeight.Bold, color = PaliaBlue)
                Text("Developer: Shanpalia  •  © Shanpalia", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showAddShortcutDialog) {
        var title by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddShortcutDialog = false },
            title = { Text("Add Shortcut", fontWeight = FontWeight.Bold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true); OutlinedTextField(url, { url = it }, label = { Text("URL") }, singleLine = true) } },
            confirmButton = { Button(onClick = { if (url.isNotBlank()) { val u = if (url.startsWith("http")) url else "https://$url"; viewModel.addShortcut(title.ifBlank { u }, u); showAddShortcutDialog = false } }, colors = ButtonDefaults.buttonColors(containerColor = PaliaBlue)) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showAddShortcutDialog = false }) { Text("Cancel") } }
        )
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            icon = { Icon(Icons.Default.SystemUpdate, null, tint = PaliaBlue, modifier = Modifier.size(34.dp)) },
            title = { Text("Palia Browser Update", fontWeight = FontWeight.Bold) },
            text = { Text("A newer version may be available. Open the official Palia Browser releases page to download the latest APK.") },
            confirmButton = {
                Button(onClick = { showUpdateDialog = false; viewModel.openUrl("https://github.com/shanpalia/palia-browser/releases/latest") }, colors = ButtonDefaults.buttonColors(containerColor = PaliaBlue)) { Text("Update Now") }
            },
            dismissButton = { TextButton(onClick = { showUpdateDialog = false }) { Text("Later") } }
        )
    }
}

@Composable
private fun QuickHubButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clickable(onClick = onClick).padding(horizontal = 2.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(accent.copy(alpha=.14f)), contentAlignment = Alignment.Center) { Icon(icon, title, tint = accent, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(5.dp)); Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun ShortcutItemView(title: String, symbol: String, url: String, onClick: () -> Unit) {
    Column(Modifier.width(64.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).border(1.dp, PaliaBlue.copy(alpha=.12f), CircleShape), contentAlignment = Alignment.Center) { Text(symbol, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PaliaBlue) }
        Spacer(Modifier.height(5.dp)); Text(title, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ShortcutItemView(shortcut: QuickShortcutItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(PaliaBlue.copy(alpha=.1f)), contentAlignment = Alignment.Center) { Text(shortcut.title.take(1).uppercase(), color = PaliaBlue, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(shortcut.title, fontWeight = FontWeight.Medium); Text(shortcut.url, maxLines=1, overflow=TextOverflow.Ellipsis, fontSize=11.sp, color=MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onLongClick) { Icon(Icons.Default.Close, "Remove") }
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, accent: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = accent.copy(alpha=.07f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(accent.copy(alpha=.13f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Public, null, tint=accent, modifier=Modifier.size(22.dp)) }
            Spacer(Modifier.width(8.dp)); Column { Text(title, fontWeight=FontWeight.Bold, fontSize=13.sp); Text(subtitle, fontSize=10.sp, color=MaterialTheme.colorScheme.onSurfaceVariant, maxLines=2) }
        }
    }
}
