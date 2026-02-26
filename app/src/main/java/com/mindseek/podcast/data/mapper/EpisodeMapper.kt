package com.mindseek.podcast.data.mapper

import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.domain.model.EpisodeDomain

fun Episode.toDomain(
    playPosition: Long = 0L,
    isFavorite: Boolean = false,
    podcastTitle: String = ""
): EpisodeDomain {
    return EpisodeDomain(
        id = id,
        podcastId = podcastId,
        title = title,
        description = description,
        audioUrl = audioUrl,
        duration = duration,
        publishDate = publishDate,
        isDownloaded = isDownloaded,
        localPath = localPath,
        playPosition = playPosition,
        isFavorite = isFavorite,
        podcastTitle = podcastTitle
    )
}

fun EpisodeDomain.toEntity(): Episode {
    return Episode(
        id = id,
        podcastId = podcastId,
        title = title,
        description = description,
        audioUrl = audioUrl,
        duration = duration,
        publishDate = publishDate,
        isDownloaded = isDownloaded,
        localPath = localPath
    )
}