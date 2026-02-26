package com.mindseek.podcast.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlaybackSpeed
import com.mindseek.podcast.domain.model.PlayerState
import com.mindseek.podcast.domain.repository.PlayerRepository
import com.mindseek.podcast.domain.usecase.player.ControlPlaybackUseCase
import com.mindseek.podcast.domain.usecase.player.PlayEpisodeUseCase
import com.mindseek.podcast.presentation.player.components.PlaybackMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val playEpisodeUseCase: PlayEpisodeUseCase,
    private val controlPlaybackUseCase: ControlPlaybackUseCase
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerRepository.playerState

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _isSeekingByUser = MutableStateFlow(false)
    val isSeekingByUser: StateFlow<Boolean> = _isSeekingByUser.asStateFlow()

    // 播放列表相关状�?
    private val _playlist = MutableStateFlow<List<EpisodeDomain>>(emptyList())
    val playlist: StateFlow<List<EpisodeDomain>> = _playlist.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.SEQUENTIAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    fun playEpisode(episode: EpisodeDomain) {
        viewModelScope.launch {
            try {
                playEpisodeUseCase(episode)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setPlaylist(episodes: List<EpisodeDomain>) {
        _playlist.value = episodes
    }

    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            try {
                if (playerState.value.isPlaying) {
                    controlPlaybackUseCase.pause()
                } else {
                    controlPlaybackUseCase.resume()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun seekTo(position: Long) {
        viewModelScope.launch {
            try {
                val clampedPosition = position.coerceIn(0L, playerState.value.duration)
                controlPlaybackUseCase.seekTo(clampedPosition)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun onSeekStarted() {
        _isSeekingByUser.value = true
    }

    fun onSeekFinished() {
        _isSeekingByUser.value = false
    }

    fun setPlaybackSpeed(speed: PlaybackSpeed) {
        viewModelScope.launch {
            try {
                controlPlaybackUseCase.setPlaybackSpeed(speed.value)
                _uiState.value = _uiState.value.copy(selectedSpeed = speed)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            try {
                controlPlaybackUseCase.setVolume(volume)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            try {
                val currentEpisode = playerState.value.currentEpisode
                val currentPlaylist = _playlist.value
                
                if (currentEpisode != null && currentPlaylist.isNotEmpty()) {
                    val currentIndex = currentPlaylist.indexOfFirst { it.id == currentEpisode.id }
                    val nextEpisode = when (_playbackMode.value) {
                        PlaybackMode.SEQUENTIAL -> {
                            if (currentIndex >= 0 && currentIndex < currentPlaylist.size - 1) {
                                currentPlaylist[currentIndex + 1]
                            } else null
                        }
                        PlaybackMode.REPEAT_ONE -> currentEpisode
                        PlaybackMode.SHUFFLE -> {
                            if (currentPlaylist.size > 1) {
                                val availableEpisodes = currentPlaylist.filter { it.id != currentEpisode.id }
                                availableEpisodes.randomOrNull()
                            } else null
                        }
                    }
                    
                    nextEpisode?.let { playEpisode(it) }
                } else {
                    controlPlaybackUseCase.skipToNext()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            try {
                val currentEpisode = playerState.value.currentEpisode
                val currentPlaylist = _playlist.value
                
                if (currentEpisode != null && currentPlaylist.isNotEmpty()) {
                    val currentIndex = currentPlaylist.indexOfFirst { it.id == currentEpisode.id }
                    val previousEpisode = when (_playbackMode.value) {
                        PlaybackMode.SEQUENTIAL -> {
                            if (currentIndex > 0) {
                                currentPlaylist[currentIndex - 1]
                            } else null
                        }
                        PlaybackMode.REPEAT_ONE -> currentEpisode
                        PlaybackMode.SHUFFLE -> {
                            if (currentPlaylist.size > 1) {
                                val availableEpisodes = currentPlaylist.filter { it.id != currentEpisode.id }
                                availableEpisodes.randomOrNull()
                            } else null
                        }
                    }
                    
                    previousEpisode?.let { playEpisode(it) }
                } else {
                    controlPlaybackUseCase.skipToPrevious()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun seekToPercentage(percentage: Float) {
        viewModelScope.launch {
            try {
                val duration = playerState.value.duration
                if (duration > 0) {
                    val position = (percentage * duration).toLong()
                    val clampedPosition = position.coerceIn(0L, duration)
                    controlPlaybackUseCase.seekTo(clampedPosition)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun getProgressPercentage(): Float {
        val state = playerState.value
        return if (state.duration > 0) {
            (state.currentPosition.toFloat() / state.duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun toggleSpeedSelector() {
        _uiState.value = _uiState.value.copy(
            showSpeedSelector = !_uiState.value.showSpeedSelector,
            showVolumeControl = false // Close volume control if open
        )
    }

    fun toggleVolumeControl() {
        _uiState.value = _uiState.value.copy(
            showVolumeControl = !_uiState.value.showVolumeControl,
            showSpeedSelector = false // Close speed selector if open
        )
    }

    fun togglePlaylist() {
        _uiState.value = _uiState.value.copy(
            showPlaylist = !_uiState.value.showPlaylist
        )
    }

    fun hideControls() {
        _uiState.value = _uiState.value.copy(
            showSpeedSelector = false,
            showVolumeControl = false,
            showPlaylist = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        playerRepository.release()
    }
}

data class PlayerUiState(
    val selectedSpeed: PlaybackSpeed = PlaybackSpeed.SPEED_1X,
    val showSpeedSelector: Boolean = false,
    val showVolumeControl: Boolean = false,
    val showPlaylist: Boolean = false,
    val error: String? = null
)