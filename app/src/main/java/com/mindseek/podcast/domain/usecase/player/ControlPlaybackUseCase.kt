package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.repository.PlayerRepository
import javax.inject.Inject

class ControlPlaybackUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend fun pause() = playerRepository.pause()
    
    suspend fun resume() = playerRepository.resume()
    
    suspend fun stop() = playerRepository.stop()
    
    suspend fun seekTo(position: Long) = playerRepository.seekTo(position)
    
    suspend fun setPlaybackSpeed(speed: Float) = playerRepository.setPlaybackSpeed(speed)
    
    suspend fun setVolume(volume: Float) = playerRepository.setVolume(volume)
    
    suspend fun skipToNext() = playerRepository.skipToNext()
    
    suspend fun skipToPrevious() = playerRepository.skipToPrevious()
}