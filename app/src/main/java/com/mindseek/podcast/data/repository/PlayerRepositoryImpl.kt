package com.mindseek.podcast.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlayerState
import com.mindseek.podcast.domain.repository.PlayerRepository
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import com.mindseek.podcast.domain.usecase.player.GetNextEpisodeUseCase
import com.mindseek.podcast.domain.usecase.player.GetPreviousEpisodeUseCase
import com.mindseek.podcast.service.AudioPlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Type-safe extension function for safe casting of StateFlow types
 * Prevents ClassCastException by checking type compatibility before casting
 */
inline fun <reified T> StateFlow<*>.safeCast(): T? {
    return try {
        if (this is T) this else null
    } catch (e: Exception) {
        android.util.Log.w("TypeSafeCasting", "Failed to cast ${this::class.simpleName} to ${T::class.simpleName}: ${e.message}")
        null
    }
}

/**
 * Type-safe extension function for safe casting of MutableStateFlow types
 * Prevents ClassCastException by checking type compatibility before casting
 */
inline fun <reified T> MutableStateFlow<*>.safeCast(): T? {
    return try {
        if (this is T) this else null
    } catch (e: Exception) {
        android.util.Log.w("TypeSafeCasting", "Failed to cast ${this::class.simpleName} to ${T::class.simpleName}: ${e.message}")
        null
    }
}

/**
 * Safe type checking utility for Flow types
 * Returns true if the flow is of the expected type
 */
inline fun <reified T> Any?.isFlowOfType(): Boolean {
    return try {
        this is T
    } catch (e: Exception) {
        android.util.Log.w("TypeSafeCasting", "Type check failed for ${this?.javaClass?.simpleName}: ${e.message}")
        false
    }
}

/**
 * Safe casting utility with fallback value
 * Returns the fallback value if casting fails
 */
inline fun <reified T> Any?.safeCastWithFallback(fallback: T): T {
    return try {
        if (this is T) this else fallback
    } catch (e: Exception) {
        android.util.Log.w("TypeSafeCasting", "Safe cast with fallback failed, using fallback value: ${e.message}")
        fallback
    }
}

/**
 * Validates StateFlow type safety before operations
 * Returns true if the StateFlow can be safely used
 */
fun StateFlow<*>.validateType(): Boolean {
    return try {
        // Check if it's a valid StateFlow instance
        this.value // This will throw if there's a type issue
        true
    } catch (e: Exception) {
        android.util.Log.w("TypeSafeCasting", "StateFlow type validation failed: ${e.message}")
        false
    }
}

/**
 * Safe StateFlow value extraction with error handling
 * Returns the current value if accessible, otherwise returns fallback
 */
fun <T> StateFlow<T>.safeValue(fallback: T): T {
    return try {
        this.value
    } catch (e: Exception) {
        android.util.Log.e("TypeSafeCasting", "Error extracting StateFlow value, using fallback", e)
        fallback
    }
}

/**
 * Safe MutableStateFlow update with error handling
 * Only updates the value if the operation is safe
 */
fun <T> MutableStateFlow<T>.safeUpdate(newValue: T): Boolean {
    return try {
        this.value = newValue
        true
    } catch (e: Exception) {
        android.util.Log.e("TypeSafeCasting", "Error updating MutableStateFlow value", e)
        false
    }
}

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playHistoryRepository: PlayHistoryRepository,
    private val getNextEpisodeUseCase: GetNextEpisodeUseCase,
    private val getPreviousEpisodeUseCase: GetPreviousEpisodeUseCase
) : PlayerRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var audioPlayerService: AudioPlayerService? = null
    private var isServiceBound = false

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Private mutable state flows
    private val _currentEpisode = MutableStateFlow<EpisodeDomain?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _playbackSpeed = MutableStateFlow(1.0f)
    private val _volume = MutableStateFlow(1.0f)
    private val _isBuffering = MutableStateFlow(false)

    // Public readonly state flows
    override val currentEpisode: StateFlow<EpisodeDomain?> = _currentEpisode.asStateFlow()
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    override val duration: StateFlow<Long> = _duration.asStateFlow()
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    override val volume: StateFlow<Float> = _volume.asStateFlow()
    override val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.AudioPlayerBinder
            audioPlayerService = binder.getService()
            isServiceBound = true
            
            // Start observing service state
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioPlayerService = null
            isServiceBound = false
        }
    }

    init {
        bindToService()
    }

    private fun bindToService() {
        val intent = Intent(context, AudioPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceState() {
        audioPlayerService?.let { service ->
            repositoryScope.launch {
                try {
                    // Combine all service state flows into player state
                    combine(
                        service.currentEpisode,
                        service.isPlaying,
                        service.currentPosition,
                        service.duration,
                        service.isBuffering,
                        service.error
                    ) { flows ->
                        try {
                            // Use type-safe helper methods for casting with proper error handling and fallbacks
                            val episode = flows[0].safeCastWithFallback<EpisodeDomain?>(null)
                            val playing = flows[1].safeCastWithFallback(false)
                            val position = flows[2].safeCastWithFallback(0L)
                            val duration = flows[3].safeCastWithFallback(0L)
                            val buffering = flows[4].safeCastWithFallback(false)
                            val error = flows[5].safeCastWithFallback<String?>(null)
                            
                            PlayerState(
                                currentEpisode = episode,
                                isPlaying = playing,
                                currentPosition = position,
                                duration = duration,
                                isBuffering = buffering,
                                error = error,
                                playbackSpeed = _playerState.value.playbackSpeed,
                                volume = _playerState.value.volume
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("PlayerRepository", "Error creating PlayerState from service flows", e)
                            // Return current state as fallback
                            _playerState.value
                        }
                    }.collect { newState ->
                        try {
                            _playerState.value = newState
                            updateIndividualFlows(newState)
                            
                            // Auto-save progress every 10 seconds when playing
                            if (newState.isPlaying && newState.currentEpisode != null) {
                                autoSaveProgress(newState.currentEpisode!!, newState.currentPosition)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PlayerRepository", "Error updating player state", e)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PlayerRepository", "Error observing service state", e)
                }
            }
        }
    }

    override suspend fun playEpisode(episode: EpisodeDomain) {
        ensureServiceBound()
        
        // Save current play position to history before switching episodes
        _playerState.value.currentEpisode?.let { currentEp ->
            val currentPos = _playerState.value.currentPosition
            if (currentPos > 0) {
                playHistoryRepository.savePlayHistory(currentEp.id, currentPos)
            }
        }
        
        // Get resume position from play history
        val playHistory = playHistoryRepository.getPlayHistoryByEpisodeId(episode.id)
        val resumePosition = playHistory?.playPosition ?: episode.playPosition
        
        // Create episode with resume position
        val episodeWithPosition = episode.copy(playPosition = resumePosition)
        
        // Start service with episode
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
            putExtra(AudioPlayerService.EXTRA_EPISODE, episodeWithPosition)
        }
        context.startService(intent)
        
        audioPlayerService?.playEpisode(episodeWithPosition)
        
        // Seek to resume position if needed
        if (resumePosition > 0) {
            delay(500) // Wait a bit for the media to be prepared
            seekTo(resumePosition)
        }
    }

    override suspend fun pause() {
        ensureServiceBound()
        
        // Save current progress before pausing
        saveCurrentProgress()
        
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PAUSE
        }
        context.startService(intent)
        audioPlayerService?.pause()
    }

    override suspend fun resume() {
        ensureServiceBound()
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
        }
        context.startService(intent)
        audioPlayerService?.play()
    }

    override suspend fun stop() {
        ensureServiceBound()
        
        // Save current progress before stopping
        saveCurrentProgress()
        
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_STOP
        }
        context.startService(intent)
        audioPlayerService?.stop()
    }

    override suspend fun seekTo(position: Long) {
        ensureServiceBound()
        audioPlayerService?.seekTo(position)
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        ensureServiceBound()
        audioPlayerService?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        _playbackSpeed.value = speed
    }

    override suspend fun setVolume(volume: Float) {
        ensureServiceBound()
        val clampedVolume = volume.coerceIn(0f, 1f)
        audioPlayerService?.setVolume(clampedVolume)
        _playerState.value = _playerState.value.copy(volume = clampedVolume)
        _volume.value = clampedVolume
    }

    override suspend fun skipToNext() {
        ensureServiceBound()
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_NEXT
        }
        context.startService(intent)
        audioPlayerService?.skipToNext()
    }

    override suspend fun skipToPrevious() {
        ensureServiceBound()
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PREVIOUS
        }
        context.startService(intent)
        audioPlayerService?.skipToPrevious()
    }

    override fun release() {
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
        }
        audioPlayerService = null
    }

    private suspend fun ensureServiceBound() {
        if (!isServiceBound || audioPlayerService == null) {
            bindToService()
            // Wait a bit for service to bind
            var attempts = 0
            while ((!isServiceBound || audioPlayerService == null) && attempts < 50) {
                delay(100)
                attempts++
            }
        }
    }

    private fun updateIndividualFlows(state: PlayerState) {
        try {
            // Use type-safe helper methods for additional safety when updating flows
            val episodeUpdateSuccess = _currentEpisode.safeUpdate(state.currentEpisode)
            val playingUpdateSuccess = _isPlaying.safeUpdate(state.isPlaying)
            val positionUpdateSuccess = _currentPosition.safeUpdate(state.currentPosition)
            val durationUpdateSuccess = _duration.safeUpdate(state.duration)
            val speedUpdateSuccess = _playbackSpeed.safeUpdate(state.playbackSpeed)
            val volumeUpdateSuccess = _volume.safeUpdate(state.volume)
            val bufferingUpdateSuccess = _isBuffering.safeUpdate(state.isBuffering)
            
            // Log any failed updates for debugging
            if (!episodeUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update currentEpisode flow")
            if (!playingUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update isPlaying flow")
            if (!positionUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update currentPosition flow")
            if (!durationUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update duration flow")
            if (!speedUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update playbackSpeed flow")
            if (!volumeUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update volume flow")
            if (!bufferingUpdateSuccess) android.util.Log.w("PlayerRepository", "Failed to update isBuffering flow")
            
            // Log successful update with details
            val successfulUpdates = listOf(
                episodeUpdateSuccess, playingUpdateSuccess, positionUpdateSuccess,
                durationUpdateSuccess, speedUpdateSuccess, volumeUpdateSuccess, bufferingUpdateSuccess
            ).count { it }
            
            android.util.Log.d("PlayerRepository", "Updated individual flows ($successfulUpdates/7 successful) - " +
                "Episode: ${state.currentEpisode?.title ?: "None"}, " +
                "Playing: ${state.isPlaying}, " +
                "Position: ${state.currentPosition}, " +
                "Duration: ${state.duration}")
                
        } catch (e: Exception) {
            android.util.Log.e("PlayerRepository", "Failed to update individual flows", e)
            // Fallback to direct assignment if type-safe methods fail
            try {
                _currentEpisode.value = state.currentEpisode
                _isPlaying.value = state.isPlaying
                _currentPosition.value = state.currentPosition
                _duration.value = state.duration
                _playbackSpeed.value = state.playbackSpeed
                _volume.value = state.volume
                _isBuffering.value = state.isBuffering
                android.util.Log.d("PlayerRepository", "Fallback direct assignment successful")
            } catch (fallbackException: Exception) {
                android.util.Log.e("PlayerRepository", "Even fallback assignment failed", fallbackException)
                // Don't rethrow - we want to continue operation even if state update fails
                // The main playerState will still be updated, so UI can still function
            }
        }
    }

    private var lastSavedPosition = 0L
    private var lastSaveTime = 0L
    private var currentEpisodeId: String? = null
    
    private fun autoSaveProgress(episode: EpisodeDomain, currentPosition: Long) {
        repositoryScope.launch {
            val currentTime = System.currentTimeMillis()
            val positionDiff = kotlin.math.abs(currentPosition - lastSavedPosition)
            
            // Reset tracking if episode changed
            if (currentEpisodeId != episode.id) {
                currentEpisodeId = episode.id
                lastSavedPosition = 0L
                lastSaveTime = 0L
            }
            
            // Save progress every 10 seconds or when position changes significantly (30 seconds)
            // Also save if position is near the end (within last 30 seconds)
            val duration = _playerState.value.duration
            val isNearEnd = duration > 0 && (duration - currentPosition) < 30_000
            
            if (currentTime - lastSaveTime > 10_000 || 
                positionDiff > 30_000 || 
                isNearEnd ||
                currentPosition < lastSavedPosition) { // Handle seeking backwards
                
                try {
                    playHistoryRepository.savePlayHistory(episode.id, currentPosition)
                    lastSavedPosition = currentPosition
                    lastSaveTime = currentTime
                } catch (e: Exception) {
                    // Log error but don't interrupt playback
                    android.util.Log.w("PlayerRepository", "Failed to save progress: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Save progress when pausing or stopping playback
     */
    private suspend fun saveCurrentProgress() {
        _playerState.value.currentEpisode?.let { episode ->
            val currentPos = _playerState.value.currentPosition
            if (currentPos > 0) {
                try {
                    playHistoryRepository.savePlayHistory(episode.id, currentPos)
                    lastSavedPosition = currentPos
                    lastSaveTime = System.currentTimeMillis()
                } catch (e: Exception) {
                    android.util.Log.w("PlayerRepository", "Failed to save current progress: ${e.message}")
                }
            }
        }
    }
}