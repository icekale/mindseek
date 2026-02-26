package com.mindseek.podcast.data.local.dao

import androidx.room.*
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.data.local.entity.PodcastWithEpisodes
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcasts ORDER BY lastUpdated DESC")
    fun getAllPodcasts(): Flow<List<Podcast>>

    @Query("SELECT * FROM podcasts WHERE isSubscribed = 1 ORDER BY lastUpdated DESC")
    fun getSubscribedPodcasts(): Flow<List<Podcast>>

    @Query("SELECT * FROM podcasts WHERE id = :id")
    suspend fun getPodcastById(id: String): Podcast?

    @Query("SELECT * FROM podcasts WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchPodcasts(query: String): Flow<List<Podcast>>

    @Query("SELECT * FROM podcasts WHERE category = :category ORDER BY lastUpdated DESC")
    fun getPodcastsByCategory(category: String): Flow<List<Podcast>>

    @Transaction
    @Query("SELECT * FROM podcasts WHERE id = :podcastId")
    suspend fun getPodcastWithEpisodes(podcastId: String): PodcastWithEpisodes?

    @Transaction
    @Query("SELECT * FROM podcasts WHERE isSubscribed = 1")
    fun getSubscribedPodcastsWithEpisodes(): Flow<List<PodcastWithEpisodes>>

    @Query("SELECT COUNT(*) FROM podcasts WHERE isSubscribed = 1")
    suspend fun getSubscribedPodcastCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcast(podcast: Podcast)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcasts(podcasts: List<Podcast>)

    @Update
    suspend fun updatePodcast(podcast: Podcast)

    @Delete
    suspend fun deletePodcast(podcast: Podcast)

    @Query("UPDATE podcasts SET isSubscribed = :isSubscribed WHERE id = :id")
    suspend fun updateSubscriptionStatus(id: String, isSubscribed: Boolean)

    @Query("UPDATE podcasts SET lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateLastUpdated(id: String, timestamp: Long)

    @Query("DELETE FROM podcasts WHERE isSubscribed = 0")
    suspend fun deleteUnsubscribedPodcasts()
}