package com.mindseek.podcast.data.local.dao

import androidx.room.*
import com.mindseek.podcast.data.local.entity.DownloadTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadTaskDao {
    
    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun getAllDownloadTasks(): Flow<List<DownloadTask>>
    
    @Query("SELECT * FROM download_tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadTasksByStatus(status: String): Flow<List<DownloadTask>>
    
    @Query("SELECT * FROM download_tasks WHERE episodeId = :episodeId")
    suspend fun getDownloadTaskByEpisodeId(episodeId: String): DownloadTask?
    
    @Query("SELECT * FROM download_tasks WHERE id = :taskId")
    suspend fun getDownloadTaskById(taskId: String): DownloadTask?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadTask(downloadTask: DownloadTask)
    
    @Update
    suspend fun updateDownloadTask(downloadTask: DownloadTask)
    
    @Delete
    suspend fun deleteDownloadTask(downloadTask: DownloadTask)
    
    @Query("DELETE FROM download_tasks WHERE episodeId = :episodeId")
    suspend fun deleteDownloadTaskByEpisodeId(episodeId: String)
    
    @Query("UPDATE download_tasks SET progress = :progress, downloadedBytes = :downloadedBytes WHERE id = :taskId")
    suspend fun updateDownloadProgress(taskId: String, progress: Float, downloadedBytes: Long)
    
    @Query("UPDATE download_tasks SET status = :status, errorMessage = :errorMessage WHERE id = :taskId")
    suspend fun updateDownloadStatus(taskId: String, status: String, errorMessage: String? = null)
    
    @Query("UPDATE download_tasks SET status = :status, localPath = :localPath, completedAt = :completedAt WHERE id = :taskId")
    suspend fun markDownloadCompleted(taskId: String, status: String, localPath: String, completedAt: Long)
}