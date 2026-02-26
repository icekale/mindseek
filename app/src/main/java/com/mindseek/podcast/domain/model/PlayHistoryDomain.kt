package com.mindseek.podcast.domain.model

data class PlayHistoryDomain(
    val id: String,
    val episode: EpisodeDomain,
    val playPosition: Long,
    val playDate: Long,
    val completionPercentage: Float
)