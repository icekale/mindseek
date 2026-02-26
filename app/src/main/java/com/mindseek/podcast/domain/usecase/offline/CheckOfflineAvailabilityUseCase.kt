package com.mindseek.podcast.domain.usecase.offline

import com.mindseek.podcast.domain.model.EpisodeDomain
import java.io.File
import javax.inject.Inject

class CheckOfflineAvailabilityUseCase @Inject constructor() {
    
    /**
     * Check if an episode is available for offline playback
     */
    operator fun invoke(episode: EpisodeDomain): Boolean {
        return episode.isDownloaded && 
               episode.localPath != null && 
               isLocalFileValid(episode.localPath)
    }
    
    /**
     * Check if a local file path is valid and accessible
     */
    private fun isLocalFileValid(localPath: String): Boolean {
        return try {
            val file = File(localPath)
            file.exists() && file.canRead() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }
}