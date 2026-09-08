package com.example.data.repository

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.example.data.db.BookmarkDao
import com.example.data.db.HistoryDao
import com.example.data.db.ShortcutDao
import com.example.data.model.BookmarkItem
import com.example.data.model.HistoryItem
import com.example.data.model.QuickShortcutItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BrowserRepository(
    private val bookmarkDao: BookmarkDao,
    private val historyDao: HistoryDao,
    private val shortcutDao: ShortcutDao,
    private val context: Context
) {
    val bookmarks: Flow<List<BookmarkItem>> = bookmarkDao.getAllBookmarks()
    val history: Flow<List<HistoryItem>> = historyDao.getAllHistory()
    val recentHistory: Flow<List<HistoryItem>> = historyDao.getRecentHistory(10)
    val shortcuts: Flow<List<QuickShortcutItem>> = shortcutDao.getAllShortcuts()

    suspend fun addBookmark(title: String, url: String) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(BookmarkItem(title = title.ifBlank { url }, url = url))
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun deleteBookmarkByUrl(url: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteByUrl(url)
    }

    fun isBookmarked(url: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(url)
    }

    suspend fun addHistory(title: String, url: String) = withContext(Dispatchers.IO) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            historyDao.insertHistory(HistoryItem(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
    }

    suspend fun addShortcut(title: String, url: String) = withContext(Dispatchers.IO) {
        shortcutDao.insertShortcut(QuickShortcutItem(title = title, url = url))
    }

    suspend fun deleteShortcut(id: Long) = withContext(Dispatchers.IO) {
        shortcutDao.deleteShortcutById(id)
    }

    suspend fun clearBrowsingData(clearHist: Boolean, clearCookies: Boolean, clearCache: Boolean) = withContext(Dispatchers.Main) {
        if (clearHist) {
            withContext(Dispatchers.IO) {
                historyDao.clearAllHistory()
            }
        }
        if (clearCookies) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
        }
        if (clearCache) {
            WebStorage.getInstance().deleteAllData()
            try {
                context.cacheDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }
}
