package com.mindseek.podcast.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = Episode::class,
            parentColumns = ["id"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["episodeId"]), Index(value = ["playDate"])]
)
data class PlayHistory(
    @PrimaryKey val id: String,
    val episodeId: String,
    val playPosition: Long,
    val playDate: Long
)