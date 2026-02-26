package com.mindseek.podcast.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "download_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Episode::class,
            parentColumns = ["id"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["episodeId"])]
)
data class DownloadTask(
    @PrimaryKey val id: String,
    val episodeId: String,
    val audioUrl: String,
    val localPath: String? = null,
    val status: String, // "pending", "downloading", "completed", "failed", "paused"
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val createdAt: Long,
    val completedAt: Long? = null,
    val errorMessage: String? = null
)