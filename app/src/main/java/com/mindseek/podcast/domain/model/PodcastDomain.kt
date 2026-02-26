package com.mindseek.podcast.domain.model

data class PodcastDomain(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val category: String,
    val isSubscribed: Boolean = false,
    val lastUpdated: Long,
    val episodeCount: Int = 0
)