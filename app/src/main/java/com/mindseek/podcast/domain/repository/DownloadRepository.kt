package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    
    /**
     * Get all download tasks
     */
    fun getAllDownloads(): Flow<List<DownloadInfo>>
    
    /**
     * Get downloads by status
     */
    fun getDownloadsByStatus(status: DownloadState): Flow<List<DownloadInfo>>
    
    /**
     * Get download info for a specific episode
     */
    suspend fun getDownloadInfo(episodeId: String): DownloadInfo?
    
    /**
     * Start downloading an episode
     */
    suspend fun startDownload(episodeId: String, audioUrl: String, title: String): Result<String>
    
    /**
     * Pause a download
     */
    suspend fun pauseDownload(episodeId: String): Result<Unit>
    
    /**
     * Resume a download
     */
    suspend fun resumeDownload(episodeId: String): Result<Unit>
    
    /**
     * Cancel a download
     */
    suspend fun cancelDownload(episodeId: String): Result<Unit>
    
    /**
     * Delete downloaded file
     */
    suspend fun deleteDownload(episodeId: String): Result<Unit>
    
    /**
     * Update download progress
     */
    suspend fun updateDownloadProgress(taskId: String, progress: Float, downloadedBytes: Long)
    
    /**
     * Mark download as completed
     */
    suspend fun markDownloadCompleted(taskId: String, localPath: String)
    
    /**
     * Mark download as failed
     */
    suspend fun markDownloadFailed(taskId: String, errorMessage: String)
    
    /**
     * Get total downloaded size
     */
    suspend fun getTotalDownloadedSize(): Long
    
    /**
     * Clean up old downloads
     */
    suspend fun cleanupOldDownloads(maxAgeMillis: Long): Result<Int>
}