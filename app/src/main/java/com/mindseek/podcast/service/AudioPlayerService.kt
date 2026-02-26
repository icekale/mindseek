package com.mindseek.podcast.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mindseek.podcast.MainActivity
import com.mindseek.podcast.R
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import com.mindseek.podcast.domain.usecase.player.GetNextEpisodeUseCase
import com.mindseek.podcast.domain.usecase.player.GetPreviousEpisodeUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : Service() {

    @Inject
    lateinit var playHistoryRepository: PlayHistoryRepository
    
    @Inject
    lateinit var getNextEpisodeUseCase: GetNextEpisodeUseCase
    
    @Inject
    lateinit var getPreviousEpisodeUseCase: GetPreviousEpisodeUseCase

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_player_channel"
        private const val CHANNEL_NAME = "音频播放"
        
        // Service actions
        const val ACTION_PLAY = "com.mindseek.podcast.ACTION_PLAY"
        const val ACTION_PAUSE = "com.mindseek.podcast.ACTION_PAUSE"
        const val ACTION_STOP = "com.mindseek.podcast.ACTION_STOP"
        const val ACTION_NEXT = "com.mindseek.podcast.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.mindseek.podcast.ACTION_PREVIOUS"
        
        // Intent extras
        const val EXTRA_EPISODE = "extra_episode"
    }

    private val binder = AudioPlayerBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var notificationManager: NotificationManager? = null
    
    // Player state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentEpisode = MutableStateFlow<EpisodeDomain?>(null)
    val currentEpisode: StateFlow<EpisodeDomain?> = _currentEpisode.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        createNotificationChannel()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                if (_isPlaying.value) {
                    pause() // Toggle to pause if already playing
                } else {
                    handlePlayAction(intent)
                }
            }
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
            ACTION_NEXT -> skipToNext()
            ACTION_PREVIOUS -> skipToPrevious()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        exoPlayer?.release()
        serviceScope.launch {
            // Clean up any ongoing coroutines
        }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(playerListener)
        }
        
        mediaSession = MediaSession.Builder(this, exoPlayer!!)
            .build()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val isBuffering = playbackState == Player.STATE_BUFFERING
            val isPlaying = playbackState == Player.STATE_READY && (exoPlayer?.playWhenReady == true)
            
            _isBuffering.value = isBuffering
            _isPlaying.value = isPlaying
            
            // Update duration when ready
            if (playbackState == Player.STATE_READY) {
                exoPlayer?.let { player ->
                    _duration.value = if (player.duration > 0) player.duration else 0L
                }
            }
            
            // Handle playback completion
            if (playbackState == Player.STATE_ENDED) {
                handlePlaybackEnded()
            }
            
            updateNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            updateNotification()
            
            // Start position tracking when playing
            if (isPlaying) {
                startPositionTracking()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _error.value = error.message ?: "播放出错"
            _isPlaying.value = false
            updateNotification()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            val isReady = exoPlayer?.playbackState == Player.STATE_READY
            _isPlaying.value = playWhenReady && isReady
            updateNotification()
        }
    }

    // Public methods for controlling playback
    fun playEpisode(episode: EpisodeDomain) {
        serviceScope.launch {
            try {
                _currentEpisode.value = episode
                _error.value = null
                
                // Determine the audio source - prefer local file if available
                val audioSource = getAudioSource(episode)
                
                val mediaItem = MediaItem.Builder()
                    .setUri(audioSource)
                    .setMediaId(episode.id)
                    .build()

                exoPlayer?.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                }
                
                startForegroundService()
            } catch (e: Exception) {
                _error.value = "无法播放该节�? ${e.message}"
            }
        }
    }

    fun play() {
        exoPlayer?.playWhenReady = true
        startForegroundService()
    }

    fun pause() {
        exoPlayer?.playWhenReady = false
        updateNotification()
    }

    fun stop() {
        exoPlayer?.stop()
        _isPlaying.value = false
        _currentPosition.value = 0L
        _currentEpisode.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun seekTo(position: Long) {
        try {
            exoPlayer?.let { player ->
                val duration = if (player.duration > 0) player.duration else Long.MAX_VALUE
                val clampedPosition = position.coerceIn(0L, duration)
                player.seekTo(clampedPosition)
                _currentPosition.value = clampedPosition
            }
        } catch (e: Exception) {
            _error.value = "跳转失败: ${e.message}"
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        try {
            val clampedSpeed = speed.coerceIn(0.25f, 3.0f)
            exoPlayer?.setPlaybackSpeed(clampedSpeed)
        } catch (e: Exception) {
            _error.value = "设置播放速度失败: ${e.message}"
        }
    }

    fun setVolume(volume: Float) {
        try {
            val clampedVolume = volume.coerceIn(0f, 1f)
            exoPlayer?.volume = clampedVolume
        } catch (e: Exception) {
            _error.value = "设置音量失败: ${e.message}"
        }
    }

    fun skipToNext() {
        serviceScope.launch {
            try {
                _currentEpisode.value?.let { currentEpisode ->
                    // Save current progress before switching
                    val currentPos = _currentPosition.value
                    if (currentPos > 0) {
                        playHistoryRepository.savePlayHistory(currentEpisode.id, currentPos)
                    }
                    
                    // Get next episode
                    val nextEpisode = getNextEpisodeUseCase(currentEpisode)
                    if (nextEpisode != null) {
                        // Get resume position for next episode
                        val playHistory = playHistoryRepository.getPlayHistoryByEpisodeId(nextEpisode.id)
                        val resumePosition = playHistory?.playPosition ?: 0L
                        
                        val episodeWithPosition = nextEpisode.copy(playPosition = resumePosition)
                        playEpisode(episodeWithPosition)
                        
                        // Seek to resume position if needed
                        if (resumePosition > 0) {
                            kotlinx.coroutines.delay(500) // Wait for media to be prepared
                            seekTo(resumePosition)
                        }
                    } else {
                        _error.value = "没有下一集了"
                    }
                }
            } catch (e: Exception) {
                _error.value = "跳转到下一集失�? ${e.message}"
            }
        }
    }

    fun skipToPrevious() {
        serviceScope.launch {
            try {
                exoPlayer?.let { player ->
                    if (player.currentPosition > 10000) {
                        // If playing for more than 10 seconds, seek to beginning
                        seekTo(0L)
                    } else {
                        // Skip to previous episode
                        _currentEpisode.value?.let { currentEpisode ->
                            // Save current progress before switching
                            val currentPos = _currentPosition.value
                            if (currentPos > 0) {
                                playHistoryRepository.savePlayHistory(currentEpisode.id, currentPos)
                            }
                            
                            // Get previous episode
                            val previousEpisode = getPreviousEpisodeUseCase(currentEpisode)
                            if (previousEpisode != null) {
                                // Get resume position for previous episode
                                val playHistory = playHistoryRepository.getPlayHistoryByEpisodeId(previousEpisode.id)
                                val resumePosition = playHistory?.playPosition ?: 0L
                                
                                val episodeWithPosition = previousEpisode.copy(playPosition = resumePosition)
                                playEpisode(episodeWithPosition)
                                
                                // Seek to resume position if needed
                                if (resumePosition > 0) {
                                    kotlinx.coroutines.delay(500) // Wait for media to be prepared
                                    seekTo(resumePosition)
                                }
                            } else {
                                // No previous episode, just seek to beginning
                                seekTo(0L)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "跳转到上一集失�? ${e.message}"
            }
        }
    }

    private fun handlePlayAction(intent: Intent) {
        val episode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_EPISODE, EpisodeDomain::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_EPISODE)
        }
        
        if (episode != null) {
            playEpisode(episode)
        } else {
            play()
        }
    }

    private fun handlePlaybackEnded() {
        serviceScope.launch {
            try {
                _currentEpisode.value?.let { currentEpisode ->
                    // Save final progress (mark as completed)
                    val duration = _duration.value
                    if (duration > 0) {
                        playHistoryRepository.savePlayHistory(currentEpisode.id, duration)
                    }
                    
                    // Auto-play next episode
                    val nextEpisode = getNextEpisodeUseCase(currentEpisode)
                    if (nextEpisode != null) {
                        // Get resume position for next episode
                        val playHistory = playHistoryRepository.getPlayHistoryByEpisodeId(nextEpisode.id)
                        val resumePosition = playHistory?.playPosition ?: 0L
                        
                        val episodeWithPosition = nextEpisode.copy(playPosition = resumePosition)
                        
                        // Small delay before starting next episode
                        kotlinx.coroutines.delay(1000)
                        playEpisode(episodeWithPosition)
                        
                        // Seek to resume position if needed
                        if (resumePosition > 0) {
                            kotlinx.coroutines.delay(500) // Wait for media to be prepared
                            seekTo(resumePosition)
                        }
                    } else {
                        // No next episode, stop playback
                        stop()
                    }
                }
            } catch (e: Exception) {
                _error.value = "自动播放下一集失�? ${e.message}"
                stop()
            }
        }
    }

    private fun startPositionTracking() {
        serviceScope.launch {
            while (_isPlaying.value) {
                exoPlayer?.let { player ->
                    val newPosition = player.currentPosition
                    _currentPosition.value = newPosition
                    
                    // Update duration if it has changed
                    if (player.duration > 0 && player.duration != _duration.value) {
                        _duration.value = player.duration
                    }
                }
                kotlinx.coroutines.delay(500) // Update every 500ms for smoother progress
            }
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        if (_currentEpisode.value != null) {
            val notification = createNotification()
            notificationManager?.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): Notification {
        val episode = _currentEpisode.value
        val isPlaying = _isPlaying.value

        // Intent to open the app
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous action
        val previousAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous,
            "上一首",
            createActionPendingIntent(ACTION_PREVIOUS)
        )

        // Play/Pause action
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "暂停",
                createActionPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "播放",
                createActionPendingIntent(ACTION_PLAY)
            )
        }

        // Next action
        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "下一首",
            createActionPendingIntent(ACTION_NEXT)
        )

        // Stop action
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel,
            "停止",
            createActionPendingIntent(ACTION_STOP)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(episode?.title ?: "播客播放器")
            .setContentText(episode?.podcastTitle ?: "正在播放")
            .setSubText(if (isPlaying) "正在播放" else "已暂停")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(stopAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2) // Show previous, play/pause, next in compact view
                    .setMediaSession(mediaSession?.sessionCompatToken)
            )
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setColorized(true)
            .setColor(getColor(android.R.color.holo_blue_dark))
            .build()
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "播客音频播放通知"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Determine the audio source for an episode - prefer local file if available and valid
     */
    private fun getAudioSource(episode: EpisodeDomain): String {
        // Check if episode has a local path and the file exists
        episode.localPath?.let { localPath ->
            val localFile = java.io.File(localPath)
            if (localFile.exists() && localFile.canRead() && localFile.length() > 0) {
                return "file://$localPath"
            }
        }
        
        // Fall back to remote URL
        return episode.audioUrl
    }
}