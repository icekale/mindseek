package com.mindseek.podcast.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Search history entity for Room database
 */
@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey
    val id: String,
    val query: String,
    val timestamp: Long,
    val resultCount: Int = 0
)