package com.mindseek.podcast.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.data.local.PodcastDatabase
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpisodeDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PodcastDatabase
    private lateinit var episodeDao: EpisodeDao
    private lateinit var podcastDao: PodcastDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        episodeDao = database.episodeDao()
        podcastDao = database.podcastDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetEpisode() = runTest {
        // Given
        val episode = createTestEpisode("1", "podcast1", "Test Episode")

        // When
        episodeDao.insertEpisode(episode)
        val retrieved = episodeDao.getEpisodeById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals(episode.id, retrieved?.id)
        assertEquals(episode.title, retrieved?.title)
        assertEquals(episode.podcastId, retrieved?.podcastId)
    }

    @Test
    fun getEpisodesByPodcastId() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Episode 1"),
            createTestEpisode("2", "podcast1", "Episode 2"),
            createTestEpisode("3", "podcast2", "Episode 3")
        )

        // When
        episodeDao.insertEpisodes(episodes)
        val podcast1Episodes = episodeDao.getEpisodesByPodcastId("podcast1").first()

        // Then
        assertEquals(2, podcast1Episodes.size)
        assertTrue(podcast1Episodes.all { it.podcastId == "podcast1" })
        assertTrue(podcast1Episodes.any { it.title == "Episode 1" })
        assertTrue(podcast1Episodes.any { it.title == "Episode 2" })
    }

    @Test
    fun getDownloadedEpisodes() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Downloaded Episode", isDownloaded = true),
            createTestEpisode("2", "podcast1", "Not Downloaded", isDownloaded = false),
            createTestEpisode("3", "podcast2", "Another Downloaded", isDownloaded = true)
        )

        // When
        episodeDao.insertEpisodes(episodes)
        val downloadedEpisodes = episodeDao.getDownloadedEpisodes().first()

        // Then
        assertEquals(2, downloadedEpisodes.size)
        assertTrue(downloadedEpisodes.all { it.isDownloaded })
        assertTrue(downloadedEpisodes.any { it.title == "Downloaded Episode" })
        assertTrue(downloadedEpisodes.any { it.title == "Another Downloaded" })
    }

    @Test
    fun getLatestEpisodesFromSubscriptions() = runTest {
        // Given
        val subscribedPodcast = createTestPodcast("podcast1", "Subscribed Podcast", isSubscribed = true)
        val unsubscribedPodcast = createTestPodcast("podcast2", "Unsubscribed Podcast", isSubscribed = false)
        
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Subscribed Episode 1"),
            createTestEpisode("2", "podcast1", "Subscribed Episode 2"),
            createTestEpisode("3", "podcast2", "Unsubscribed Episode")
        )

        // When
        podcastDao.insertPodcasts(listOf(subscribedPodcast, unsubscribedPodcast))
        episodeDao.insertEpisodes(episodes)
        val latestEpisodes = episodeDao.getLatestEpisodesFromSubscriptions(10).first()

        // Then
        assertEquals(2, latestEpisodes.size)
        assertTrue(latestEpisodes.all { it.podcastId == "podcast1" })
    }

    @Test
    fun searchEpisodes() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Tech Talk Episode"),
            createTestEpisode("2", "podcast1", "Science Discussion"),
            createTestEpisode("3", "podcast2", "Tech News Update")
        )

        // When
        episodeDao.insertEpisodes(episodes)
        val searchResults = episodeDao.searchEpisodes("Tech").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.title == "Tech Talk Episode" })
        assertTrue(searchResults.any { it.title == "Tech News Update" })
    }

    @Test
    fun updateDownloadStatus() = runTest {
        // Given
        val episode = createTestEpisode("1", "podcast1", "Test Episode", isDownloaded = false)
        val localPath = "/storage/downloads/episode1.mp3"

        // When
        episodeDao.insertEpisode(episode)
        episodeDao.updateDownloadStatus("1", true, localPath)
        val updated = episodeDao.getEpisodeById("1")

        // Then
        assertNotNull(updated)
        assertTrue(updated!!.isDownloaded)
        assertEquals(localPath, updated.localPath)
    }

    @Test
    fun getDownloadedEpisodeCount() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Downloaded 1", isDownloaded = true),
            createTestEpisode("2", "podcast1", "Not Downloaded", isDownloaded = false),
            createTestEpisode("3", "podcast2", "Downloaded 2", isDownloaded = true)
        )

        // When
        episodeDao.insertEpisodes(episodes)
        val count = episodeDao.getDownloadedEpisodeCount()

        // Then
        assertEquals(2, count)
    }

    @Test
    fun getTotalDownloadedDuration() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Episode 1", duration = 3600000L, isDownloaded = true),
            createTestEpisode("2", "podcast1", "Episode 2", duration = 1800000L, isDownloaded = false),
            createTestEpisode("3", "podcast2", "Episode 3", duration = 2700000L, isDownloaded = true)
        )

        // When
        episodeDao.insertEpisodes(episodes)
        val totalDuration = episodeDao.getTotalDownloadedDuration()

        // Then
        assertEquals(6300000L, totalDuration) // 3600000 + 2700000
    }

    @Test
    fun getNewEpisodesSince() = runTest {
        // Given
        val baseTime = System.currentTimeMillis()
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Old Episode", publishDate = baseTime - 10000),
            createTestEpisode("2", "podcast1", "New Episode 1", publishDate = baseTime + 5000),
            createTestEpisode("3", "podcast1", "New Episode 2", publishDate = baseTime + 10000)
        )

        // When
        episodeDao.insertEpisodes(episodes)
        val newEpisodes = episodeDao.getNewEpisodesSince("podcast1", baseTime).first()

        // Then
        assertEquals(2, newEpisodes.size)
        assertTrue(newEpisodes.any { it.title == "New Episode 1" })
        assertTrue(newEpisodes.any { it.title == "New Episode 2" })
    }

    @Test
    fun updateEpisode() = runTest {
        // Given
        val originalEpisode = createTestEpisode("1", "podcast1", "Original Title")
        val updatedEpisode = originalEpisode.copy(title = "Updated Title", description = "Updated Description")

        // When
        episodeDao.insertEpisode(originalEpisode)
        episodeDao.updateEpisode(updatedEpisode)
        val retrieved = episodeDao.getEpisodeById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved?.title)
        assertEquals("Updated Description", retrieved?.description)
    }

    @Test
    fun deleteEpisode() = runTest {
        // Given
        val episode = createTestEpisode("1", "podcast1", "Test Episode")

        // When
        episodeDao.insertEpisode(episode)
        episodeDao.deleteEpisode(episode)
        val retrieved = episodeDao.getEpisodeById("1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun deleteEpisodesByPodcastId() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Episode 1"),
            createTestEpisode("2", "podcast1", "Episode 2"),
            createTestEpisode("3", "podcast2", "Episode 3")
        )

        // When
        episodeDao.insertEpisodes(episodes)
        episodeDao.deleteEpisodesByPodcastId("podcast1")
        val remainingEpisodes = episodeDao.getEpisodesByPodcastId("podcast1").first()
        val podcast2Episodes = episodeDao.getEpisodesByPodcastId("podcast2").first()

        // Then
        assertEquals(0, remainingEpisodes.size)
        assertEquals(1, podcast2Episodes.size)
    }

    @Test
    fun deleteAllDownloadedEpisodes() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisode("1", "podcast1", "Downloaded 1", isDownloaded = true),
            createTestEpisode("2", "podcast1", "Not Downloaded", isDownloaded = false),
            createTestEpisode("3", "podcast2", "Downloaded 2", isDownloaded = true)
        )

        // When
        episodeDao.insertEpisodes(episodes)
        episodeDao.deleteAllDownloadedEpisodes()
        val downloadedEpisodes = episodeDao.getDownloadedEpisodes().first()

        // Then
        assertEquals(0, downloadedEpisodes.size)
    }

    private fun createTestEpisode(
        id: String,
        podcastId: String,
        title: String,
        duration: Long = 3600000L,
        publishDate: Long = System.currentTimeMillis(),
        isDownloaded: Boolean = false
    ) = Episode(
        id = id,
        podcastId = podcastId,
        title = title,
        description = "Test Episode Description",
        audioUrl = "https://example.com/audio.mp3",
        duration = duration,
        publishDate = publishDate,
        isDownloaded = isDownloaded,
        localPath = if (isDownloaded) "/storage/downloads/$id.mp3" else null
    )

    private fun createTestPodcast(
        id: String,
        title: String,
        isSubscribed: Boolean = false
    ) = Podcast(
        id = id,
        title = title,
        description = "Test Description",
        imageUrl = "https://example.com/image.jpg",
        author = "Test Author",
        category = "Technology",
        isSubscribed = isSubscribed,
        lastUpdated = System.currentTimeMillis()
    )
}