package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.model.BrowserTab
import com.example.data.db.PaliaDatabase
import com.example.data.model.BookmarkItem
import com.example.data.model.DownloadItem
import com.example.data.model.HistoryItem
import com.example.data.model.QuickShortcutItem
import com.example.data.repository.BrowserRepository
import com.example.data.repository.NewsArticle
import com.example.data.repository.NewsRepository
import com.example.data.repository.SearchEngine
import com.example.data.repository.SettingsRepository
import com.example.data.repository.ThemeMode
import com.example.data.repository.UserSettings
import com.example.download.manager.PaliaDownloadManager
import com.example.download.utils.FileUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavigationTab {
    HOME,
    TABS,
    DOWNLOADS,
    BOOKMARKS,
    SETTINGS
}

sealed class WebCommand {
    data class LoadUrl(val url: String) : WebCommand()
    object GoBack : WebCommand()
    object GoForward : WebCommand()
    object Reload : WebCommand()
    object Stop : WebCommand()
    data class FindInPage(val query: String, val forward: Boolean = true) : WebCommand()
    object ClearMatches : WebCommand()
}

data class DownloadPromptData(
    val url: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PaliaDatabase.getDatabase(application)
    private val browserRepo = BrowserRepository(
        db.bookmarkDao(),
        db.historyDao(),
        db.shortcutDao(),
        application
    )
    val settingsRepo = SettingsRepository(application)
    val downloadManager = PaliaDownloadManager.getInstance(application)
    private val newsRepository = NewsRepository()

    private val _news = MutableStateFlow<List<NewsArticle>>(emptyList())
    val news: StateFlow<List<NewsArticle>> = _news.asStateFlow()
    private val _newsLoading = MutableStateFlow(false)
    val newsLoading: StateFlow<Boolean> = _newsLoading.asStateFlow()

    val settings: StateFlow<UserSettings> = settingsRepo.settings
    val bookmarks: StateFlow<List<BookmarkItem>> = browserRepo.bookmarks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val history: StateFlow<List<HistoryItem>> = browserRepo.history.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentHistory: StateFlow<List<HistoryItem>> = browserRepo.recentHistory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val shortcuts: StateFlow<List<QuickShortcutItem>> = browserRepo.shortcuts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val downloads: StateFlow<List<DownloadItem>> = downloadManager.allDownloads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val initialTab = BrowserTab()
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _currentNav = MutableStateFlow(MainNavigationTab.HOME)
    val currentNav: StateFlow<MainNavigationTab> = _currentNav.asStateFlow()

    private val _webCommands = MutableSharedFlow<WebCommand>()
    val webCommands: SharedFlow<WebCommand> = _webCommands.asSharedFlow()

    private val _pendingDownloadPrompt = MutableStateFlow<DownloadPromptData?>(null)
    val pendingDownloadPrompt: StateFlow<DownloadPromptData?> = _pendingDownloadPrompt.asStateFlow()

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    fun refreshNews() {
        if (_newsLoading.value) return
        viewModelScope.launch {
            _newsLoading.value = true
            runCatching { newsRepository.fetch(settings.value.newsLanguage) }
                .onSuccess { _news.value = it }
            _newsLoading.value = false
        }
    }

    fun setNewsLanguage(language: String) {
        settingsRepo.updateNewsLanguage(language)
        refreshNews()
    }

    fun navigateTo(nav: MainNavigationTab) {
        _currentNav.value = nav
    }

    fun openUrl(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return

        val finalUrl = resolveInputToUrl(trimmed)
        updateActiveTab { it.copy(url = finalUrl, isLoading = true) }
        _currentNav.value = MainNavigationTab.HOME

        viewModelScope.launch {
            _webCommands.emit(WebCommand.LoadUrl(finalUrl))
        }
    }

    private fun resolveInputToUrl(input: String): String {
        return if (input.startsWith("http://") || input.startsWith("https://")) {
            input
        } else if (!input.contains(" ") && (input.contains(".") || input.startsWith("localhost"))) {
            "https://$input"
        } else {
            val searchUrl = settings.value.searchEngine.searchUrl
            "$searchUrl${android.net.Uri.encode(input)}"
        }
    }

    fun goBack() {
        viewModelScope.launch { _webCommands.emit(WebCommand.GoBack) }
    }

    fun goForward() {
        viewModelScope.launch { _webCommands.emit(WebCommand.GoForward) }
    }

    fun reload() {
        viewModelScope.launch { _webCommands.emit(WebCommand.Reload) }
    }

    fun goHome() {
        updateActiveTab { it.copy(url = "about:home", title = "Home", isLoading = false) }
        _currentNav.value = MainNavigationTab.HOME
    }

    fun addNewTab(url: String = "about:home", isIncognito: Boolean = false) {
        val newTab = BrowserTab(url = url, isIncognito = isIncognito)
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        _currentNav.value = MainNavigationTab.HOME
        if (url != "about:home") {
            viewModelScope.launch { _webCommands.emit(WebCommand.LoadUrl(url)) }
        }
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            // Keep at least one tab
            val resetTab = BrowserTab()
            _tabs.value = listOf(resetTab)
            _activeTabId.value = resetTab.id
            return
        }

        val index = currentList.indexOfFirst { it.id == tabId }
        val updated = currentList.filterNot { it.id == tabId }
        _tabs.value = updated

        if (_activeTabId.value == tabId) {
            val newActiveIndex = if (index >= updated.size) updated.size - 1 else index
            _activeTabId.value = updated[newActiveIndex].id
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
        _currentNav.value = MainNavigationTab.HOME
    }

    fun closeAllTabs() {
        val newTab = BrowserTab()
        _tabs.value = listOf(newTab)
        _activeTabId.value = newTab.id
    }

    fun toggleDesktopMode() {
        updateActiveTab { it.copy(isDesktopMode = !it.isDesktopMode) }
        reload()
    }

    fun updateActiveTabState(
        title: String? = null,
        url: String? = null,
        progress: Int? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        isLoading: Boolean? = null
    ) {
        updateActiveTab { tab ->
            val updatedUrl = url ?: tab.url
            val updatedTitle = title ?: tab.title
            // Save to history if page loaded and not incognito
            if (url != null && !tab.isIncognito && url != "about:home" && !tab.isLoading) {
                viewModelScope.launch {
                    browserRepo.addHistory(updatedTitle, updatedUrl)
                }
            }
            tab.copy(
                title = updatedTitle,
                url = updatedUrl,
                progress = progress ?: tab.progress,
                canGoBack = canGoBack ?: tab.canGoBack,
                canGoForward = canGoForward ?: tab.canGoForward,
                isLoading = isLoading ?: tab.isLoading
            )
        }
    }

    private fun updateActiveTab(block: (BrowserTab) -> BrowserTab) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map {
            if (it.id == currentId) block(it) else it
        }
    }

    // Find in Page
    fun setFindActive(active: Boolean) {
        updateActiveTab { it.copy(isFindActive = active, findQuery = if (!active) "" else it.findQuery) }
        if (!active) {
            viewModelScope.launch { _webCommands.emit(WebCommand.ClearMatches) }
        }
    }

    fun searchFindInPage(query: String) {
        updateActiveTab { it.copy(findQuery = query) }
        viewModelScope.launch {
            _webCommands.emit(WebCommand.FindInPage(query, forward = true))
        }
    }

    fun findNext() {
        val query = activeTab?.findQuery ?: return
        viewModelScope.launch {
            _webCommands.emit(WebCommand.FindInPage(query, forward = true))
        }
    }

    fun findPrevious() {
        val query = activeTab?.findQuery ?: return
        viewModelScope.launch {
            _webCommands.emit(WebCommand.FindInPage(query, forward = false))
        }
    }

    // Bookmarks
    fun toggleBookmarkCurrentPage() {
        val current = activeTab ?: return
        if (current.isHome) return

        viewModelScope.launch {
            browserRepo.addBookmark(current.title, current.url)
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { browserRepo.deleteBookmark(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { browserRepo.clearHistory() }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch { browserRepo.deleteHistory(id) }
    }

    fun addShortcut(title: String, url: String) {
        viewModelScope.launch { browserRepo.addShortcut(title, url) }
    }

    fun deleteShortcut(id: Long) {
        viewModelScope.launch { browserRepo.deleteShortcut(id) }
    }

    fun clearBrowsingData(clearHist: Boolean, clearCookies: Boolean, clearCache: Boolean) {
        viewModelScope.launch {
            browserRepo.clearBrowsingData(clearHist, clearCookies, clearCache)
        }
    }

    // Download Management
    fun onDownloadDetected(
        url: String,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        val detectedFileName = FileUtils.extractFileName(url, contentDisposition, mimeType)
        if (settings.value.askBeforeDownload) {
            _pendingDownloadPrompt.value = DownloadPromptData(
                url = url,
                fileName = detectedFileName,
                mimeType = mimeType ?: FileUtils.getMimeType(detectedFileName),
                fileSize = contentLength
            )
        } else {
            confirmDownload(url, detectedFileName, mimeType)
        }
    }

    fun confirmDownload(url: String, customFileName: String? = null, mimeType: String? = null) {
        _pendingDownloadPrompt.value = null
        viewModelScope.launch {
            downloadManager.enqueueDownload(
                url = url,
                mimeType = mimeType,
                customFileName = customFileName
            )
        }
    }

    fun dismissDownloadPrompt() {
        _pendingDownloadPrompt.value = null
    }

    fun pauseDownload(id: Long) = downloadManager.pauseDownload(id)
    fun resumeDownload(id: Long) = downloadManager.resumeDownload(id)
    fun cancelDownload(id: Long) = downloadManager.cancelDownload(id)
    fun retryDownload(id: Long) = downloadManager.retryDownload(id)
    fun deleteDownload(id: Long, deleteFile: Boolean = true) = downloadManager.deleteDownload(id, deleteFile)
    fun clearCompletedDownloads() = downloadManager.clearCompleted()
}
