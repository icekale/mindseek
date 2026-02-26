package com.mindseek.podcast.presentation.ui.state

import com.mindseek.podcast.data.local.entity.Episode

data class PlayerUiState(
    val currentEpisode: Episode? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)