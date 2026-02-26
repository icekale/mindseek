package com.mindseek.podcast.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EpisodeWithComments(
    @Embedded val episode: Episode,
    @Relation(
        parentColumn = "id",
        entityColumn = "episodeId"
    )
    val comments: List<Comment>
)