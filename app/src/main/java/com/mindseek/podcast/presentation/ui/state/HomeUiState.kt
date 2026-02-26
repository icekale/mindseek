package com.mindseek.podcast.presentation.ui.state

import com.mindseek.podcast.domain.model.PodcastDomain

data class HomeUiState(
    val recommendedPodcasts: List<PodcastDomain> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)