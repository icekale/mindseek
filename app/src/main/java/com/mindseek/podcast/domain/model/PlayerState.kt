package com.mindseek.podcast.domain.model

data class PlayerState(
    val currentEpisode: EpisodeDomain? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isBuffering: Boolean = false,
    val error: String? = null
)

enum class PlaybackSpeed(val value: Float, val displayName: String) {
    SPEED_0_5X(0.5f, "0.5x"),
    SPEED_0_75X(0.75f, "0.75x"),
    SPEED_1X(1.0f, "1x"),
    SPEED_1_25X(1.25f, "1.25x"),
    SPEED_1_5X(1.5f, "1.5x"),
    SPEED_2X(2.0f, "2x")
}

sealed class PlayerAction {
    data class PlayEpisode(val episode: EpisodeDomain) : PlayerAction()
    object Play : PlayerAction()
    object Pause : PlayerAction()
    object Stop : PlayerAction()
    data class SeekTo(val position: Long) : PlayerAction()
    data class SetPlaybackSpeed(val speed: Float) : PlayerAction()
    data class SetVolume(val volume: Float) : PlayerAction()
    object SkipToNext : PlayerAction()
    object SkipToPrevious : PlayerAction()
}