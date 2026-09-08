package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookmarkItem
import com.example.data.model.DownloadItem
import com.example.data.model.HistoryItem
import com.example.data.model.QuickShortcutItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DownloadItem::class,
        BookmarkItem::class,
        HistoryItem::class,
        QuickShortcutItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PaliaDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: PaliaDatabase? = null

        fun getDatabase(context: Context): PaliaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaliaDatabase::class.java,
                    "palia_browser.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialShortcuts(getDatabase(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialShortcuts(db: PaliaDatabase) {
            val defaults = listOf(
                QuickShortcutItem(title = "Google", url = "https://www.google.com", isDefault = true, iconName = "search"),
                QuickShortcutItem(title = "Wikipedia", url = "https://www.wikipedia.org", isDefault = true, iconName = "book"),
                QuickShortcutItem(title = "DuckDuckGo", url = "https://duckduckgo.com", isDefault = true, iconName = "shield"),
                QuickShortcutItem(title = "GitHub", url = "https://github.com", isDefault = true, iconName = "code"),
                QuickShortcutItem(title = "Reddit", url = "https://www.reddit.com", isDefault = true, iconName = "forum"),
                QuickShortcutItem(title = "Archive.org", url = "https://archive.org", isDefault = true, iconName = "public")
            )
            db.shortcutDao().insertShortcuts(defaults)
        }
    }
}
