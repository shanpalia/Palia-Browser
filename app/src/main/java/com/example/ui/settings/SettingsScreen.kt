package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.SearchEngine
import com.example.data.repository.ThemeMode
import com.example.ui.BrowserViewModel
import com.example.ui.theme.DownloadGreen
import com.example.ui.theme.PaliaCyan
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSearchEngineMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Settings Card
            SettingsSectionHeader("General & Appearance")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Search Engine Dropdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSearchEngineMenu = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = PaliaCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Search Engine", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text(settings.searchEngine.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        DropdownMenu(
                            expanded = showSearchEngineMenu,
                            onDismissRequest = { showSearchEngineMenu = false }
                        ) {
                            SearchEngine.values().forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine.displayName) },
                                    onClick = {
                                        viewModel.settingsRepo.updateSearchEngine(engine)
                                        showSearchEngineMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Theme Selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeMenu = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brightness4, contentDescription = null, tint = PaliaCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Theme", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text(settings.themeMode.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            ThemeMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.name) },
                                    onClick = {
                                        viewModel.settingsRepo.updateThemeMode(mode)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Download Manager Settings Card
            SettingsSectionHeader("Download Manager")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Max simultaneous downloads slider
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = DownloadGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Max Simultaneous Downloads", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Text(
                                text = "${settings.maxSimultaneousDownloads}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = DownloadGreen
                            )
                        }

                        Slider(
                            value = settings.maxSimultaneousDownloads.toFloat(),
                            onValueChange = { viewModel.settingsRepo.updateMaxSimultaneousDownloads(it.roundToInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(thumbColor = DownloadGreen, activeTrackColor = DownloadGreen)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Wi-Fi only toggle
                    SettingsToggleRow(
                        icon = Icons.Default.Wifi,
                        title = "Download over Wi-Fi only",
                        subtitle = "Pause downloads when using cellular data",
                        checked = settings.wifiOnlyDownloads,
                        onCheckedChange = { viewModel.settingsRepo.updateWifiOnlyDownloads(it) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Ask before downloading toggle
                    SettingsToggleRow(
                        icon = Icons.Default.Download,
                        title = "Ask before downloading",
                        subtitle = "Confirm file name and location for every file",
                        checked = settings.askBeforeDownload,
                        onCheckedChange = { viewModel.settingsRepo.updateAskBeforeDownload(it) }
                    )

                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Download Notifications toggle
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Download Notifications",
                        subtitle = "Show ongoing speed & progress in notifications",
                        checked = settings.showDownloadNotifications,
                        onCheckedChange = { viewModel.settingsRepo.updateDownloadNotifications(it) }
                    )
                }
            }

            // Privacy & Data Card
            SettingsSectionHeader("Privacy & Storage")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearDataDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Clear Browsing Data", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text("History, cookies, and cached data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            SettingsSectionHeader("Personalized News")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("News Language", fontWeight = FontWeight.Bold)
                    Text("Home news will refresh in your selected language", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    val languages = listOf("Hindi","English","Bengali","Telugu","Tamil","Marathi","Gujarati","Kannada","Malayalam","Punjabi")
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Row(Modifier.fillMaxWidth().clickable{expanded=true}.padding(vertical=10.dp), verticalAlignment=Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, null, tint=PaliaCyan)
                            Spacer(Modifier.width(12.dp))
                            Text(settings.newsLanguage, Modifier.weight(1f), fontWeight=FontWeight.SemiBold)
                            Text("▾", fontSize=20.sp)
                        }
                        DropdownMenu(expanded=expanded, onDismissRequest={expanded=false}) {
                            languages.forEach { language -> DropdownMenuItem(text={Text(language)}, onClick={expanded=false; viewModel.setNewsLanguage(language)}) }
                        }
                    }
                }
            }

            // App Update — kept inside Settings, UC-style simple row
            SettingsSectionHeader("Updates")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openUrl("https://github.com/Shanpalia/palia-browser/releases") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "App Update",
                        tint = PaliaCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "App Update",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Check for the latest Palia Browser version",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("›", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // About Card with mandatory developer branding
            SettingsSectionHeader("About Palia Browser")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Palia Browser",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = PaliaCyan
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A modern, lightweight browser with real-time multi-threaded resumable download engine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "© Shanpalia",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        var clearHistoryChecked by remember { mutableStateOf(true) }
        var clearCookiesChecked by remember { mutableStateOf(true) }
        var clearCacheChecked by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Browsing Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClearDataCheckboxRow("Browsing history", clearHistoryChecked) { clearHistoryChecked = it }
                    ClearDataCheckboxRow("Cookies and site data", clearCookiesChecked) { clearCookiesChecked = it }
                    ClearDataCheckboxRow("Cached images and files", clearCacheChecked) { clearCacheChecked = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearBrowsingData(clearHistoryChecked, clearCookiesChecked, clearCacheChecked)
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = PaliaCyan,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PaliaCyan)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = PaliaCyan
            )
        )
    }
}

@Composable
private fun ClearDataCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = PaliaCyan)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
