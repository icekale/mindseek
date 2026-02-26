package com.mindseek.podcast.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PodcastWithEpisodes(
    @Embedded val podcast: Podcast,
    @Relation(
        parentColumn = "id",
        entityColumn = "podcastId"
    )
    val episodes: List<Episode>
)