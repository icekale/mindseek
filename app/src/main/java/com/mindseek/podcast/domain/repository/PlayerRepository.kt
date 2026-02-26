package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    // Main player state - combines all individual states
    val playerState: StateFlow<PlayerState>
    
    // Individual state flows - all declared as StateFlow for type safety and consistency
    val currentEpisode: StateFlow<EpisodeDomain?>
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val playbackSpeed: StateFlow<Float>
    val volume: StateFlow<Float>
    val isBuffering: StateFlow<Boolean>

    // Playback control methods
    suspend fun playEpisode(episode: EpisodeDomain)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun seekTo(position: Long)
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun setVolume(volume: Float)
    suspend fun skipToNext()
    suspend fun skipToPrevious()
    
    // Resource management
    fun release()
}