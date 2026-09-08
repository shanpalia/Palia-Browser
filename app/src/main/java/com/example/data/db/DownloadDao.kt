package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun getDownloadById(id: Long): Flow<DownloadItem?>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadByIdSync(id: Long): DownloadItem?

    @Query("SELECT * FROM downloads WHERE status IN ('DOWNLOADING', 'WAITING')")
    suspend fun getActiveDownloads(): List<DownloadItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItem): Long

    @Update
    suspend fun updateDownload(item: DownloadItem)

    @Query("""
        UPDATE downloads 
        SET downloadedBytes = :downloadedBytes, 
            totalBytes = :totalBytes, 
            speedBytesPerSec = :speedBytesPerSec, 
            etaSeconds = :etaSeconds, 
            status = :status 
        WHERE id = :id
    """)
    suspend fun updateProgress(
        id: Long,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBytesPerSec: Long,
        etaSeconds: Long,
        status: DownloadStatus
    )

    @Query("UPDATE downloads SET status = :status, errorMessage = :errorMessage, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: DownloadStatus,
        errorMessage: String? = null,
        completedAt: Long? = null
    )

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("DELETE FROM downloads")
    suspend fun deleteAllDownloads()

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}
