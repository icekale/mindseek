package com.mindseek.podcast.data.mapper

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.model.PodcastDomain

fun Podcast.toDomain(episodeCount: Int = 0): PodcastDomain {
    return PodcastDomain(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        author = author,
        category = category,
        isSubscribed = isSubscribed,
        lastUpdated = lastUpdated,
        episodeCount = episodeCount
    )
}

fun PodcastDomain.toEntity(): Podcast {
    return Podcast(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        author = author,
        category = category,
        isSubscribed = isSubscribed,
        lastUpdated = lastUpdated
    )
}