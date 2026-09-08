package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SearchEngine(val displayName: String, val searchUrl: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
    BING("Bing", "https://www.bing.com/search?q=", "https://www.bing.com"),
    YAHOO("Yahoo", "https://search.yahoo.com/search?p=", "https://search.yahoo.com"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=", "https://www.ecosia.org")
}

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

data class UserSettings(
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val homePageUrl: String = "about:home",
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val maxSimultaneousDownloads: Int = 3,
    val wifiOnlyDownloads: Boolean = false,
    val autoStartDownloads: Boolean = true,
    val askBeforeDownload: Boolean = true,
    val showDownloadNotifications: Boolean = true,
    val defaultDesktopMode: Boolean = false
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("palia_browser_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val seName = prefs.getString("search_engine", SearchEngine.GOOGLE.name) ?: SearchEngine.GOOGLE.name
        val searchEngine = try { SearchEngine.valueOf(seName) } catch (_: Exception) { SearchEngine.GOOGLE }

        val themeName = prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.DARK.name
        val themeMode = try { ThemeMode.valueOf(themeName) } catch (_: Exception) { ThemeMode.LIGHT }

        return UserSettings(
            searchEngine = searchEngine,
            homePageUrl = prefs.getString("home_page", "about:home") ?: "about:home",
            themeMode = themeMode,
            maxSimultaneousDownloads = prefs.getInt("max_downloads", 3).coerceIn(1, 5),
            wifiOnlyDownloads = prefs.getBoolean("wifi_only", false),
            autoStartDownloads = prefs.getBoolean("auto_start", true),
            askBeforeDownload = prefs.getBoolean("ask_before_download", true),
            showDownloadNotifications = prefs.getBoolean("download_notifications", true),
            defaultDesktopMode = prefs.getBoolean("desktop_mode", false)
        )
    }

    fun updateSearchEngine(engine: SearchEngine) {
        prefs.edit().putString("search_engine", engine.name).apply()
        _settings.value = _settings.value.copy(searchEngine = engine)
    }

    fun updateThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updateMaxSimultaneousDownloads(count: Int) {
        val safeCount = count.coerceIn(1, 5)
        prefs.edit().putInt("max_downloads", safeCount).apply()
        _settings.value = _settings.value.copy(maxSimultaneousDownloads = safeCount)
    }

    fun updateWifiOnlyDownloads(wifiOnly: Boolean) {
        prefs.edit().putBoolean("wifi_only", wifiOnly).apply()
        _settings.value = _settings.value.copy(wifiOnlyDownloads = wifiOnly)
    }

    fun updateAutoStartDownloads(autoStart: Boolean) {
        prefs.edit().putBoolean("auto_start", autoStart).apply()
        _settings.value = _settings.value.copy(autoStartDownloads = autoStart)
    }

    fun updateAskBeforeDownload(ask: Boolean) {
        prefs.edit().putBoolean("ask_before_download", ask).apply()
        _settings.value = _settings.value.copy(askBeforeDownload = ask)
    }

    fun updateDownloadNotifications(show: Boolean) {
        prefs.edit().putBoolean("download_notifications", show).apply()
        _settings.value = _settings.value.copy(showDownloadNotifications = show)
    }

    fun updateDesktopMode(desktop: Boolean) {
        prefs.edit().putBoolean("desktop_mode", desktop).apply()
        _settings.value = _settings.value.copy(defaultDesktopMode = desktop)
    }

    fun updateHomePage(url: String) {
        prefs.edit().putString("home_page", url).apply()
        _settings.value = _settings.value.copy(homePageUrl = url)
    }
}
