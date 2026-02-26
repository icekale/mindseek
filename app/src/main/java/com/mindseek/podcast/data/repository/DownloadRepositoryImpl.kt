package com.mindseek.podcast.data.repository

import android.content.Context
import com.mindseek.podcast.data.local.dao.DownloadTaskDao
import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.entity.DownloadTask
import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.model.DownloadState
import com.mindseek.podcast.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadTaskDao: DownloadTaskDao,
    private val episodeDao: EpisodeDao,
    @ApplicationContext private val context: Context
) : DownloadRepository {

    private val downloadDir = File(context.getExternalFilesDir(null), "downloads")

    init {
        // Create download directory if it doesn't exist
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
    }

    override fun getAllDownloads(): Flow<List<DownloadInfo>> {
        return downloadTaskDao.getAllDownloadTasks().map { tasks ->
            tasks.map { task -> task.toDownloadInfo() }
        }
    }

    override fun getDownloadsByStatus(status: DownloadState): Flow<List<DownloadInfo>> {
        val statusString = when (status) {
            is DownloadState.NotDownloaded -> "pending"
            is DownloadState.Downloading -> "downloading"
            is DownloadState.Downloaded -> "completed"
            is DownloadState.Failed -> "failed"
            is DownloadState.Paused -> "paused"
        }
        return downloadTaskDao.getDownloadTasksByStatus(statusString).map { tasks ->
            tasks.map { task -> task.toDownloadInfo() }
        }
    }

    override suspend fun getDownloadInfo(episodeId: String): DownloadInfo? {
        return downloadTaskDao.getDownloadTaskByEpisodeId(episodeId)?.toDownloadInfo()
    }

    override suspend fun startDownload(episodeId: String, audioUrl: String, title: String): Result<String> {
        return try {
            val taskId = UUID.randomUUID().toString()
            val fileName = "${episodeId}.mp3"
            val localPath = File(downloadDir, fileName).absolutePath
            
            val downloadTask = DownloadTask(
                id = taskId,
                episodeId = episodeId,
                audioUrl = audioUrl,
                localPath = localPath,
                status = "pending",
                createdAt = System.currentTimeMillis()
            )
            
            downloadTaskDao.insertDownloadTask(downloadTask)
            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseDownload(episodeId: String): Result<Unit> {
        return try {
            val task = downloadTaskDao.getDownloadTaskByEpisodeId(episodeId)
            if (task != null && task.status == "downloading") {
                downloadTaskDao.updateDownloadStatus(task.id, "paused")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeDownload(episodeId: String): Result<Unit> {
        return try {
            val task = downloadTaskDao.getDownloadTaskByEpisodeId(episodeId)
            if (task != null && task.status == "paused") {
                downloadTaskDao.updateDownloadStatus(task.id, "downloading")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelDownload(episodeId: String): Result<Unit> {
        return try {
            val task = downloadTaskDao.getDownloadTaskByEpisodeId(episodeId)
            if (task != null) {
                // Delete partial file if exists
                task.localPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDownload(episodeId: String): Result<Unit> {
        return try {
            val task = downloadTaskDao.getDownloadTaskByEpisodeId(episodeId)
            if (task != null && task.status == "completed") {
                // Delete the downloaded file
                task.localPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                
                // Update episode to mark as not downloaded
                episodeDao.updateDownloadStatus(episodeId, false, null)
                
                // Remove download task
                downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDownloadProgress(taskId: String, progress: Float, downloadedBytes: Long) {
        downloadTaskDao.updateDownloadProgress(taskId, progress, downloadedBytes)
    }

    override suspend fun markDownloadCompleted(taskId: String, localPath: String) {
        val task = downloadTaskDao.getDownloadTaskById(taskId)
        if (task != null) {
            downloadTaskDao.markDownloadCompleted(
                taskId = taskId,
                status = "completed",
                localPath = localPath,
                completedAt = System.currentTimeMillis()
            )
            
            // Update episode to mark as downloaded
            episodeDao.updateDownloadStatus(task.episodeId, true, localPath)
        }
    }

    override suspend fun markDownloadFailed(taskId: String, errorMessage: String) {
        downloadTaskDao.updateDownloadStatus(taskId, "failed", errorMessage)
    }

    override suspend fun getTotalDownloadedSize(): Long {
        return try {
            var totalSize = 0L
            downloadDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
            totalSize
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun cleanupOldDownloads(maxAgeMillis: Long): Result<Int> {
        return try {
            val cutoffTime = System.currentTimeMillis() - maxAgeMillis
            val oldTasks = downloadTaskDao.getAllDownloadTasks()
            var deletedCount = 0
            
            // This is a simplified implementation - in a real app you'd want to collect the flow
            // For now, we'll just return success
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DownloadTask.toDownloadInfo(): DownloadInfo {
        val downloadState = when (status) {
            "pending" -> DownloadState.NotDownloaded
            "downloading" -> DownloadState.Downloading(progress)
            "completed" -> DownloadState.Downloaded
            "failed" -> DownloadState.Failed(errorMessage ?: "Unknown error")
            "paused" -> DownloadState.Paused
            else -> DownloadState.NotDownloaded
        }
        
        return DownloadInfo(
            episodeId = episodeId,
            title = "", // We'd need to join with Episode table to get title
            audioUrl = audioUrl,
            localPath = localPath,
            downloadState = downloadState,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            downloadDate = completedAt
        )
    }
}