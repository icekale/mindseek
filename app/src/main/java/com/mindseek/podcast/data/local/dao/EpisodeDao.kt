package com.mindseek.podcast.data.local.dao

import androidx.room.*
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.EpisodeWithComments
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId ORDER BY publishDate DESC")
    fun getEpisodesByPodcastId(podcastId: String): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getEpisodeById(id: String): Episode?

    @Query("SELECT * FROM episodes WHERE isDownloaded = 1 ORDER BY publishDate DESC")
    fun getDownloadedEpisodes(): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE podcastId IN (SELECT id FROM podcasts WHERE isSubscribed = 1) ORDER BY publishDate DESC LIMIT :limit")
    fun getLatestEpisodesFromSubscriptions(limit: Int = 20): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchEpisodes(query: String): Flow<List<Episode>>

    @Transaction
    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeWithComments(episodeId: String): EpisodeWithComments?

    @Query("SELECT COUNT(*) FROM episodes WHERE isDownloaded = 1")
    suspend fun getDownloadedEpisodeCount(): Int

    @Query("SELECT SUM(duration) FROM episodes WHERE isDownloaded = 1")
    suspend fun getTotalDownloadedDuration(): Long?

    @Query("SELECT * FROM episodes WHERE podcastId = :podcastId AND publishDate > :timestamp ORDER BY publishDate DESC")
    fun getNewEpisodesSince(podcastId: String, timestamp: Long): Flow<List<Episode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: Episode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Update
    suspend fun updateEpisode(episode: Episode)

    @Delete
    suspend fun deleteEpisode(episode: Episode)

    @Query("UPDATE episodes SET isDownloaded = :isDownloaded, localPath = :localPath WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, localPath: String?)

    @Query("DELETE FROM episodes WHERE isDownloaded = 1")
    suspend fun deleteAllDownloadedEpisodes()

    @Query("DELETE FROM episodes WHERE podcastId = :podcastId")
    suspend fun deleteEpisodesByPodcastId(podcastId: String)
}