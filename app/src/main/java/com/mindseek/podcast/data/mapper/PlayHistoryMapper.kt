package com.mindseek.podcast.data.mapper

import com.mindseek.podcast.data.local.entity.PlayHistory
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlayHistoryDomain

fun PlayHistory.toDomain(episode: EpisodeDomain): PlayHistoryDomain {
    val completionPercentage = if (episode.duration > 0) {
        (playPosition.toFloat() / episode.duration.toFloat()) * 100f
    } else {
        0f
    }
    
    return PlayHistoryDomain(
        id = id,
        episode = episode,
        playPosition = playPosition,
        playDate = playDate,
        completionPercentage = completionPercentage
    )
}

fun PlayHistoryDomain.toEntity(): PlayHistory {
    return PlayHistory(
        id = id,
        episodeId = episode.id,
        playPosition = playPosition,
        playDate = playDate
    )
}