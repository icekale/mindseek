package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.FavoriteDao
import com.mindseek.podcast.data.local.entity.Favorite
import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val episodeDao: EpisodeDao
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<EpisodeDomain>> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.mapNotNull { favorite ->
                episodeDao.getEpisodeById(favorite.episodeId)?.toDomain()
            }
        }
    }

    override fun getRecentFavorites(limit: Int): Flow<List<EpisodeDomain>> {
        return favoriteDao.getRecentFavorites(limit).map { favorites ->
            favorites.mapNotNull { favorite ->
                episodeDao.getEpisodeById(favorite.episodeId)?.toDomain()
            }
        }
    }

    override suspend fun addToFavorites(episodeId: String): Boolean {
        return try {
            val favorite = Favorite(
                id = UUID.randomUUID().toString(),
                episodeId = episodeId,
                addedDate = System.currentTimeMillis()
            )
            favoriteDao.insertFavorite(favorite)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeFromFavorites(episodeId: String): Boolean {
        return try {
            favoriteDao.deleteFavoriteByEpisodeId(episodeId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isFavorite(episodeId: String): Boolean {
        return favoriteDao.isFavorite(episodeId)
    }

    override suspend fun getFavoriteCount(): Int {
        return favoriteDao.getFavoriteCount()
    }

    override suspend fun clearAllFavorites() {
        favoriteDao.clearAllFavorites()
    }
}