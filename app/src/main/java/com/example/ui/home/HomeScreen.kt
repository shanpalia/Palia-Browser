package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QuickShortcutItem
import com.example.ui.BrowserViewModel
import com.example.ui.MainNavigationTab
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaCyan

@Composable
fun HomeScreen(viewModel: BrowserViewModel, modifier: Modifier = Modifier) {
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val news by viewModel.news.collectAsStateWithLifecycle()
    val newsLoading by viewModel.newsLoading.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }

    LaunchedEffect(settings.newsLanguage) { viewModel.refreshNews() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(62.dp).clip(RoundedCornerShape(16.dp)).background(PaliaCyan), contentAlignment=Alignment.Center) { Icon(Icons.Default.Public, null, tint=Color.White, modifier=Modifier.size(42.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Palia Browser", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = PaliaCyan)
                    Text("Fast  •  Safe  •  Powerful", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.refreshNews() }) { Icon(Icons.Default.Refresh, "Refresh") }
                Text("${viewModel.tabs.collectAsStateWithLifecycle().value.size}", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            }
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().testTag("home_search_input"),
                placeholder = { Text("Search or type URL", maxLines = 1) }, singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { Button(onClick = { if(query.isNotBlank()){viewModel.openUrl(query);query=""} }, shape=RoundedCornerShape(18.dp)) { Text("Go") } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { if(query.isNotBlank()){viewModel.openUrl(query);query=""} }), shape=RoundedCornerShape(26.dp)
            )
        }
        item {
            val defaults = listOf(
                Triple("Google", "https://www.google.com", "G"),
                Triple("YouTube", "https://www.youtube.com", "▶"),
                Triple("Facebook", "https://www.facebook.com", "f"),
                Triple("Instagram", "https://www.instagram.com", "◎"),
                Triple("WhatsApp", "https://web.whatsapp.com", "◉"),
                Triple("Amazon", "https://www.amazon.in", "a"),
                Triple("Flipkart", "https://www.flipkart.com", "F")
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                defaults.take(4).forEach { (title,url,mark) -> ShortcutIcon(title,url,mark,viewModel) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                defaults.drop(4).forEach { (title,url,mark) -> ShortcutIcon(title,url,mark,viewModel) }
                ShortcutCircle("Add", Icons.Default.Add) { showAdd=true }
            }
        }
        item {
            Card(shape=RoundedCornerShape(24.dp), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement=Arrangement.SpaceAround) {
                    Feature("Downloads", Icons.Default.Download) { viewModel.navigateTo(MainNavigationTab.DOWNLOADS) }
                    Feature("History", Icons.Default.History) { viewModel.navigateTo(MainNavigationTab.HOME) }
                    Feature("Bookmarks", Icons.Default.Bookmark) { viewModel.navigateTo(MainNavigationTab.BOOKMARKS) }
                    Feature("Settings", Icons.Default.Settings) { viewModel.navigateTo(MainNavigationTab.SETTINGS) }
                }
            }
        }
        item {
            Row(verticalAlignment=Alignment.CenterVertically, modifier=Modifier.fillMaxWidth()) {
                Text("News • ${settings.newsLanguage}", style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Bold, modifier=Modifier.weight(1f))
                TextButton(onClick={viewModel.navigateTo(MainNavigationTab.SETTINGS)}) { Icon(Icons.Default.Language,null); Spacer(Modifier.width(4.dp)); Text("Language") }
                IconButton(onClick={viewModel.refreshNews}) { Icon(Icons.Default.Refresh,"Refresh news") }
            }
        }
        if (newsLoading && news.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (!newsLoading && news.isEmpty()) item {
            Card(shape=RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("News unavailable", fontWeight=FontWeight.Bold); Text("Check your internet connection and tap refresh.") } }
        }
        items(news) { article ->
            Card(modifier=Modifier.fillMaxWidth().clickable{viewModel.openUrl(article.url)}, shape=RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(article.title, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.SemiBold, maxLines=3, overflow=TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text("${article.source}  •  ${article.published}", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (recentHistory.isNotEmpty()) {
            item { Text("Recently Visited", style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Bold) }
            items(recentHistory.take(4)) { item ->
                ListItem(headlineContent={Text(item.title, maxLines=1, overflow=TextOverflow.Ellipsis)}, supportingContent={Text(item.url, maxLines=1, overflow=TextOverflow.Ellipsis)}, leadingContent={Icon(Icons.Default.Public,null)}, modifier=Modifier.clickable{viewModel.openUrl(item.url)})
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }; var url by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest={showAdd=false}, title={Text("Add Shortcut")}, text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(title,{title=it},label={Text("Name")},singleLine=true);OutlinedTextField(url,{url=it},label={Text("URL")},singleLine=true)}}, confirmButton={TextButton(onClick={if(title.isNotBlank()&&url.isNotBlank()){viewModel.addShortcut(title,url);showAdd=false}}){Text("Add")}}, dismissButton={TextButton(onClick={showAdd=false}){Text("Cancel")}})
    }
}

@Composable private fun ShortcutIcon(title:String,url:String,mark:String,viewModel:BrowserViewModel){
    Column(horizontalAlignment=Alignment.CenterHorizontally, modifier=Modifier.weight(1f).clickable{viewModel.openUrl(url)}) {
        Box(Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment=Alignment.Center) { Text(mark, fontSize=24.sp, fontWeight=FontWeight.Bold, color=PaliaCyan) }
        Spacer(Modifier.height(5.dp)); Text(title, fontSize=12.sp, maxLines=1, overflow=TextOverflow.Ellipsis)
    }
}
@Composable private fun ShortcutCircle(title:String,icon:ImageVector,onClick:()->Unit){ Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.weight(1f).clickable{onClick()}){Box(Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),contentAlignment=Alignment.Center){Icon(icon,null,tint=PaliaCyan)};Spacer(Modifier.height(5.dp));Text(title,fontSize=12.sp)} }
@Composable private fun Feature(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.clickable{onClick()}.padding(4.dp)){Icon(icon,title,tint=PaliaCyan,modifier=Modifier.size(28.dp));Text(title,fontSize=12.sp,fontWeight=FontWeight.SemiBold)}}
