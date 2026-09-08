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
}
