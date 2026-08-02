package com.mindseek.podcast.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = Podcast::class,
            parentColumns = ["id"],
            childColumns = ["podcastId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["podcastId"])]
)
data class Episode(
    @PrimaryKey val id: String,
    val podcastId: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val imageUrl: String? = null,
    val duration: Long,
    val publishDate: Long,
    val source: String = "",
    val author: String = "",
    val fileSize: Long? = null,
    val playCount: Int = 0,
    val averageRating: Float = 0f,
    val isDownloaded: Boolean = false,
    val localPath: String? = null
)