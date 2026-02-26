package com.mindseek.podcast.service

import android.content.Context
import com.mindseek.podcast.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = ConcurrentHashMap<String, Job>()
    private val downloadQueue = mutableListOf<String>()
    private val maxConcurrentDownloads = 3
    
    private val _downloadStates = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadProgress>> = _downloadStates.asStateFlow()
    
    data class DownloadProgress(
        val taskId: String,
        val episodeId: String,
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val isActive: Boolean
    )
    
    /**
     * Start downloading an episode
     */
    suspend fun startDownload(episodeId: String, audioUrl: String, title: String): Result<String> {
        return try {
            // Check if already downloading
            if (activeDownloads.containsKey(episodeId)) {
                return Result.failure(IllegalStateException("Episode is already being downloaded"))
            }
            
            // Create download task in repository
            val result = downloadRepository.startDownload(episodeId, audioUrl, title)
            
            result.fold(
                onSuccess = { taskId ->
                    // Add to queue if we're at max concurrent downloads
                    if (activeDownloads.size >= maxConcurrentDownloads) {
                        synchronized(downloadQueue) {
                            downloadQueue.add(taskId)
                        }
                    } else {
                        startDownloadJob(taskId, episodeId, audioUrl)
                    }
                    Result.success(taskId)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Pause a download
     */
    suspend fun pauseDownload(episodeId: String): Result<Unit> {
        return try {
            activeDownloads[episodeId]?.cancel()
            activeDownloads.remove(episodeId)
            downloadRepository.pauseDownload(episodeId)
            
            // Start next download in queue
            processQueue()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resume a download
     */
    suspend fun resumeDownload(episodeId: String): Result<Unit> {
        return try {
            val downloadInfo = downloadRepository.getDownloadInfo(episodeId)
            if (downloadInfo != null) {
                downloadRepository.resumeDownload(episodeId)
                
                if (activeDownloads.size < maxConcurrentDownloads) {
                    startDownloadJob(downloadInfo.episodeId, episodeId, downloadInfo.audioUrl)
                } else {
                    synchronized(downloadQueue) {
                        downloadQueue.add(downloadInfo.episodeId)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancel a download
     */
    suspend fun cancelDownload(episodeId: String): Result<Unit> {
        return try {
            activeDownloads[episodeId]?.cancel()
            activeDownloads.remove(episodeId)
            
            // Remove from queue if present
            synchronized(downloadQueue) {
                downloadQueue.removeAll { it == episodeId }
            }
            
            downloadRepository.cancelDownload(episodeId)
            
            // Start next download in queue
            processQueue()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a completed download
     */
    suspend fun deleteDownload(episodeId: String): Result<Unit> {
        return downloadRepository.deleteDownload(episodeId)
    }
    
    /**
     * Get total downloaded size
     */
    suspend fun getTotalDownloadedSize(): Long {
        return downloadRepository.getTotalDownloadedSize()
    }
    
    /**
     * Clean up old downloads
     */
    suspend fun cleanupOldDownloads(maxAgeMillis: Long): Result<Int> {
        return downloadRepository.cleanupOldDownloads(maxAgeMillis)
    }
    
    private fun startDownloadJob(taskId: String, episodeId: String, audioUrl: String) {
        val job = scope.launch {
            try {
                downloadFile(taskId, episodeId, audioUrl)
            } catch (e: CancellationException) {
                // Download was cancelled
                downloadRepository.markDownloadFailed(taskId, "Download cancelled")
            } catch (e: Exception) {
                // Download failed
                downloadRepository.markDownloadFailed(taskId, e.message ?: "Unknown error")
            } finally {
                activeDownloads.remove(episodeId)
                updateDownloadState(episodeId, null)
                processQueue()
            }
        }
        
        activeDownloads[episodeId] = job
    }
    
    private suspend fun downloadFile(taskId: String, episodeId: String, audioUrl: String) {
        val downloadDir = File(context.getExternalFilesDir(null), "downloads")
        val fileName = "${episodeId}.mp3"
        val file = File(downloadDir, fileName)
        
        // Check if file already exists (resume download)
        val existingLength = if (file.exists()) file.length() else 0L
        
        val request = Request.Builder()
            .url(audioUrl)
            .apply {
                if (existingLength > 0) {
                    addHeader("Range", "bytes=$existingLength-")
                }
            }
            .build()
        
        val response: Response = okHttpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("Failed to download file: ${response.code}")
        }
        
        val contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
        val totalBytes = contentLength + existingLength
        
        response.body?.let { responseBody ->
            val inputStream: InputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file, existingLength > 0) // Append if resuming
            
            val buffer = ByteArray(8192)
            var downloadedBytes = existingLength
            var bytesRead: Int
            
            // Update initial progress
            updateDownloadProgress(taskId, episodeId, downloadedBytes, totalBytes)
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                // Check if download was cancelled
                currentCoroutineContext().ensureActive()
                
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                
                // Update progress
                updateDownloadProgress(taskId, episodeId, downloadedBytes, totalBytes)
            }
            
            outputStream.close()
            inputStream.close()
            
            // Mark download as completed
            downloadRepository.markDownloadCompleted(taskId, file.absolutePath)
        } ?: throw IOException("Response body is null")
    }
    
    private suspend fun updateDownloadProgress(taskId: String, episodeId: String, downloadedBytes: Long, totalBytes: Long) {
        val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
        
        // Update repository
        downloadRepository.updateDownloadProgress(taskId, progress, downloadedBytes)
        
        // Update local state
        updateDownloadState(episodeId, DownloadProgress(
            taskId = taskId,
            episodeId = episodeId,
            progress = progress,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            isActive = true
        ))
    }
    
    private fun updateDownloadState(episodeId: String, progress: DownloadProgress?) {
        val currentStates = _downloadStates.value.toMutableMap()
        if (progress != null) {
            currentStates[episodeId] = progress
        } else {
            currentStates.remove(episodeId)
        }
        _downloadStates.value = currentStates
    }
    
    private suspend fun processQueue() {
        val nextTaskId = synchronized(downloadQueue) {
            if (downloadQueue.isNotEmpty() && activeDownloads.size < maxConcurrentDownloads) {
                downloadQueue.removeAt(0)
            } else {
                null
            }
        }
        
        nextTaskId?.let { taskId ->
            val downloadInfo = downloadRepository.getDownloadInfo(taskId)
            if (downloadInfo != null) {
                startDownloadJob(taskId, downloadInfo.episodeId, downloadInfo.audioUrl)
            }
        }
    }
    
    /**
     * Clean up resources when the manager is destroyed
     */
    fun cleanup() {
        scope.cancel()
        activeDownloads.clear()
        downloadQueue.clear()
    }
}