package com.mindseek.podcast.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Episode::class,
            parentColumns = ["id"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["episodeId"]), Index(value = ["parentCommentId"])]
)
data class Comment(
    @PrimaryKey val id: String,
    val episodeId: String,
    val userId: String,
    val content: String,
    val timestamp: Long,
    val likeCount: Int = 0,
    val parentCommentId: String? = null
)