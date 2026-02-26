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
class PodcastDaoIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PodcastDatabase
    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        podcastDao = database.podcastDao()
        episodeDao = database.episodeDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetPodcast() = runTest {
        // Given
        val podcast = createTestPodcast("1", "Test Podcast")

        // When
        podcastDao.insertPodcast(podcast)
        val retrieved = podcastDao.getPodcastById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals(podcast.id, retrieved?.id)
        assertEquals(podcast.title, retrieved?.title)
        assertEquals(podcast.author, retrieved?.author)
    }

    @Test
    fun getAllPodcasts() = runTest {
        // Given
        val podcasts = listOf(
            createTestPodcast("1", "Podcast 1"),
            createTestPodcast("2", "Podcast 2"),
            createTestPodcast("3", "Podcast 3")
        )

        // When
        podcastDao.insertPodcasts(podcasts)
        val allPodcasts = podcastDao.getAllPodcasts().first()

        // Then
        assertEquals(3, allPodcasts.size)
        assertTrue(allPodcasts.any { it.title == "Podcast 1" })
        assertTrue(allPodcasts.any { it.title == "Podcast 2" })
        assertTrue(allPodcasts.any { it.title == "Podcast 3" })
    }

    @Test
    fun getSubscribedPodcasts() = runTest {
        // Given
        val podcasts = listOf(
            createTestPodcast("1", "Subscribed Podcast", isSubscribed = true),
            createTestPodcast("2", "Not Subscribed Podcast", isSubscribed = false),
            createTestPodcast("3", "Another Subscribed", isSubscribed = true)
        )

        // When
        podcastDao.insertPodcasts(podcasts)
        val subscribedPodcasts = podcastDao.getSubscribedPodcasts().first()

        // Then
        assertEquals(2, subscribedPodcasts.size)
        assertTrue(subscribedPodcasts.all { it.isSubscribed })
        assertTrue(subscribedPodcasts.any { it.title == "Subscribed Podcast" })
        assertTrue(subscribedPodcasts.any { it.title == "Another Subscribed" })
    }

    @Test
    fun searchPodcasts() = runTest {
        // Given
        val podcasts = listOf(
            createTestPodcast("1", "Tech Talk", author = "John Doe"),
            createTestPodcast("2", "Science Today", author = "Jane Smith"),
            createTestPodcast("3", "Tech News", author = "Bob Johnson")
        )

        // When
        podcastDao.insertPodcasts(podcasts)
        val searchResults = podcastDao.searchPodcasts("Tech").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.title == "Tech Talk" })
        assertTrue(searchResults.any { it.title == "Tech News" })
    }

    @Test
    fun updateSubscriptionStatus() = runTest {
        // Given
        val podcast = createTestPodcast("1", "Test Podcast", isSubscribed = false)

        // When
        podcastDao.insertPodcast(podcast)
        podcastDao.updateSubscriptionStatus("1", true)
        val updated = podcastDao.getPodcastById("1")

        // Then
        assertNotNull(updated)
        assertTrue(updated!!.isSubscribed)
    }

    @Test
    fun getPodcastWithEpisodes() = runTest {
        // Given
        val podcast = createTestPodcast("1", "Test Podcast")
        val episodes = listOf(
            createTestEpisode("1", "1", "Episode 1"),
            createTestEpisode("2", "1", "Episode 2")
        )

        // When
        podcastDao.insertPodcast(podcast)
        episodeDao.insertEpisodes(episodes)
        val podcastWithEpisodes = podcastDao.getPodcastWithEpisodes("1")

        // Then
        assertNotNull(podcastWithEpisodes)
        assertEquals(podcast.title, podcastWithEpisodes!!.podcast.title)
        assertEquals(2, podcastWithEpisodes.episodes.size)
    }

    private fun createTestPodcast(
        id: String,
        title: String,
        author: String = "Test Author",
        isSubscribed: Boolean = false
    ) = Podcast(
        id = id,
        title = title,
        description = "Test Description",
        imageUrl = "https://example.com/image.jpg",
        author = author,
        category = "Technology",
        isSubscribed = isSubscribed,
        lastUpdated = System.currentTimeMillis()
    )

    private fun createTestEpisode(
        id: String,
        podcastId: String,
        title: String
    ) = Episode(
        id = id,
        podcastId = podcastId,
        title = title,
        description = "Test Episode Description",
        audioUrl = "https://example.com/audio.mp3",
        duration = 3600000L,
        publishDate = System.currentTimeMillis()
    )
}