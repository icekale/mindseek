package com.mindseek.podcast.domain.usecase.offline

import com.mindseek.podcast.domain.repository.DownloadRepository
import com.mindseek.podcast.service.DownloadManager
import javax.inject.Inject

class CleanupStorageUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadManager: DownloadManager
) {
    
    /**
     * Clean up old downloads to free up storage space
     * @param maxAgeMillis Maximum age of downloads to keep (older ones will be deleted)
     * @return Number of downloads cleaned up
     */
    suspend operator fun invoke(maxAgeMillis: Long = DEFAULT_MAX_AGE): Result<Int> {
        return try {
            // Clean up old downloads from repository
            val repositoryResult = downloadRepository.cleanupOldDownloads(maxAgeMillis)
            
            // Also clean up from download manager
            val managerResult = downloadManager.cleanupOldDownloads(maxAgeMillis)
            
            // Return the result from repository (which should be more comprehensive)
            repositoryResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        // Default: clean up downloads older than 30 days
        private const val DEFAULT_MAX_AGE = 30L * 24L * 60L * 60L * 1000L
    }
}