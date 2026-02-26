package com.mindseek.podcast.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcasts")
data class Podcast(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val category: String,
    val isSubscribed: Boolean = false,
    val lastUpdated: Long
)