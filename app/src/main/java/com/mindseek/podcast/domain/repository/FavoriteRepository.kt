package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.domain.model.EpisodeDomain
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<EpisodeDomain>>
    fun getRecentFavorites(limit: Int = 20): Flow<List<EpisodeDomain>>
    suspend fun addToFavorites(episodeId: String): Boolean
    suspend fun removeFromFavorites(episodeId: String): Boolean
    suspend fun isFavorite(episodeId: String): Boolean
    suspend fun getFavoriteCount(): Int
    suspend fun clearAllFavorites()
}