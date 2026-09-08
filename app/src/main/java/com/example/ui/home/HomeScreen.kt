package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shanpalia.paliabrowser.R
import com.example.data.model.QuickShortcutItem
import com.example.ui.BrowserViewModel
import com.example.ui.MainNavigationTab
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaBlue
import com.example.ui.theme.PaliaCyan

private data class FixedShortcut(val title: String, val url: String, val icon: String)
private data class Trend(val text: String)

@Composable
fun HomeScreen(viewModel: BrowserViewModel, modifier: Modifier = Modifier) {
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    val fixed = listOf(
        FixedShortcut("Google", "https://www.google.com", "G"),
        FixedShortcut("YouTube", "https://www.youtube.com", "▶"),
        FixedShortcut("Facebook", "https://www.facebook.com", "f"),
        FixedShortcut("Instagram", "https://www.instagram.com", "◎"),
        FixedShortcut("X", "https://x.com", "𝕏"),
        FixedShortcut("Add", "", "+")
    )
    val trends = listOf(
        Trend("railway rrb group d answer key"),
        Trend("ssc chsl vacancies"),
        Trend("rbi overnight cash withdrawal auction"),
        Trend("apple iphone 18 pro max"),
        Trend("sunset today")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.img_palia_logo),
                    contentDescription = "Palia Browser",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Palia Browser", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Fast  •  Safe  •  Powerful", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.navigateTo(MainNavigationTab.TABS) }) {
                    Icon(Icons.Default.Language, contentDescription = "Tabs", tint = PaliaCyan, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = { viewModel.navigateTo(MainNavigationTab.SETTINGS) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PaliaCyan, modifier = Modifier.size(28.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search or type URL", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { if (query.isNotBlank()) { viewModel.openUrl(query); query = "" } }),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                    )
                    if (query.isNotBlank()) {
                        TextButton(onClick = { viewModel.openUrl(query); query = "" }) { Text("Go", fontWeight = FontWeight.Bold, color = PaliaCyan) }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                fixed.forEach { item ->
                    Column(Modifier.weight(1f).clickable { if (item.url.isBlank()) viewModel.navigateTo(MainNavigationTab.SETTINGS) else viewModel.openUrl(item.url) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                            Text(item.icon, fontSize = if (item.title == "Google") 25.sp else 23.sp, fontWeight = FontWeight.Bold, color = if (item.title == "Add") PaliaCyan else MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(item.title, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PaliaBlue.copy(alpha = 0.09f))) {
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    HubItem("Downloads", Icons.Default.Download, DownloadGreen) { viewModel.navigateTo(MainNavigationTab.DOWNLOADS) }
                    HubItem("History", Icons.Default.History, PaliaCyan) { viewModel.navigateTo(MainNavigationTab.HOME) }
                    HubItem("Bookmarks", Icons.Default.Bookmark, PaliaCyan) { viewModel.navigateTo(MainNavigationTab.BOOKMARKS) }
                    HubItem("Settings", Icons.Default.Settings, MaterialTheme.colorScheme.primary) { viewModel.navigateTo(MainNavigationTab.SETTINGS) }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Discover", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PaliaCyan)
                Spacer(Modifier.width(22.dp)); Text("Trending", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(22.dp)); Text("News", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(22.dp)); Text("Tech", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PaliaCyan)
                        Spacer(Modifier.width(8.dp)); Text("Trending Searches", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("See More", color = PaliaCyan, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    trends.forEachIndexed { index, trend ->
                        Row(Modifier.fillMaxWidth().clickable { viewModel.openUrl(trend.text) }.padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(30.dp).clip(CircleShape).background(PaliaBlue.copy(alpha = .10f)), contentAlignment = Alignment.Center) { Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(12.dp)); Text(trend.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Text("Top News", fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        }
        item { NewsCard("Indian Railways announces new recruitment for Group D", "Times of India  •  2h ago") { viewModel.openUrl("https://www.google.com/search?q=Indian+Railways+Group+D") } }
        item { NewsCard("SSC CHSL 2025 vacancies released", "News  •  3h ago") { viewModel.openUrl("https://www.google.com/search?q=SSC+CHSL+vacancies") } }
        item { NewsCard("RBI to conduct overnight cash withdrawal auction", "Business  •  4h ago") { viewModel.openUrl("https://www.google.com/search?q=RBI+overnight+cash+withdrawal+auction") } }

        if (recentHistory.isNotEmpty()) {
            item { Text("Recently Visited", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp)) }
            items(recentHistory.take(3)) { history ->
                Row(Modifier.fillMaxWidth().clickable { viewModel.openUrl(history.url) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = PaliaCyan)
                    Spacer(Modifier.width(10.dp)); Text(history.title.ifBlank { history.url }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard("Safe Browsing", "Privacy first", "✓", Modifier.weight(1f))
                FeatureCard("Fast Downloads", "Resume anytime", "↓", Modifier.weight(1f))
            }
        }
        item {
            Text("Palia Browser  •  © Shanpalia", modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable private fun HubItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Column(Modifier.weight(1f).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(25.dp)) }
        Spacer(Modifier.height(5.dp)); Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun NewsCard(title: String, source: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(62.dp).clip(RoundedCornerShape(10.dp)).background(PaliaBlue.copy(alpha = .10f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, contentDescription = null, tint = PaliaCyan, modifier = Modifier.size(28.dp)) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(5.dp)); Text(source, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
        }
    }
}

@Composable private fun FeatureCard(title: String, subtitle: String, symbol: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PaliaBlue.copy(alpha = .07f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).clip(CircleShape).background(PaliaCyan.copy(alpha=.12f)), contentAlignment = Alignment.Center) { Text(symbol, color = PaliaCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp) }; Spacer(Modifier.width(8.dp)); Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}
