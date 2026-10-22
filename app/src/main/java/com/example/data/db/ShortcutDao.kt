package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuickShortcutItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY id ASC")
    fun getAllShortcuts(): Flow<List<QuickShortcutItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(item: QuickShortcutItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcuts(items: List<QuickShortcutItem>)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Long)

    @Query("SELECT COUNT(*) FROM shortcuts")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM shortcuts WHERE url = :url")
    suspend fun countByUrl(url: String): Int

    @Query("DELETE FROM shortcuts WHERE isDefault = 1 AND title IN ('Wikipedia', 'DuckDuckGo', 'Reddit', 'Archive.org')")
    suspend fun removeUnwantedDefaultShortcuts()

    @Query("DELETE FROM shortcuts WHERE isDefault = 1 AND id NOT IN (SELECT MIN(id) FROM shortcuts WHERE isDefault = 1 GROUP BY url)")
    suspend fun removeDuplicateDefaultShortcuts()
}
