package com.mindseek.podcast.data.local.dao

import androidx.room.*
import com.mindseek.podcast.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedDate DESC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE episodeId = :episodeId")
    suspend fun getFavoriteByEpisodeId(episodeId: String): Favorite?

    @Query("SELECT * FROM favorites ORDER BY addedDate DESC LIMIT :limit")
    fun getRecentFavorites(limit: Int = 20): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE addedDate >= :startDate AND addedDate <= :endDate ORDER BY addedDate DESC")
    fun getFavoritesByDateRange(startDate: Long, endDate: Long): Flow<List<Favorite>>

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE episodeId = :episodeId)")
    suspend fun isFavorite(episodeId: String): Boolean

    @Query("SELECT episodeId FROM favorites ORDER BY addedDate DESC")
    fun getFavoriteEpisodeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<Favorite>)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE episodeId = :episodeId")
    suspend fun deleteFavoriteByEpisodeId(episodeId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    @Query("DELETE FROM favorites WHERE addedDate < :cutoffDate")
    suspend fun deleteOldFavorites(cutoffDate: Long)
}