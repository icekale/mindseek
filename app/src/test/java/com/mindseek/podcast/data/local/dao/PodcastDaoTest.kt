package com.mindseek.podcast.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.data.local.PodcastDatabase
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
class PodcastDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PodcastDatabase
    private lateinit var podcastDao: PodcastDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        podcastDao = database.podcastDao()
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
        assertEquals(podcast.isSubscribed, retrieved?.isSubscribed)
    }

    @Test
    fun insertMultiplePodcasts() = runTest {
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
    fun updatePodcast() = runTest {
        // Given
        val originalPodcast = createTestPodcast("1", "Original Title")
        val updatedPodcast = originalPodcast.copy(title = "Updated Title", author = "New Author")

        // When
        podcastDao.insertPodcast(originalPodcast)
        podcastDao.updatePodcast(updatedPodcast)
        val retrieved = podcastDao.getPodcastById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved?.title)
        assertEquals("New Author", retrieved?.author)
    }

    @Test
    fun deletePodcast() = runTest {
        // Given
        val podcast = createTestPodcast("1", "Test Podcast")

        // When
        podcastDao.insertPodcast(podcast)
        podcastDao.deletePodcast(podcast)
        val retrieved = podcastDao.getPodcastById("1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun getPodcastsByCategory() = runTest {
        // Given
        val podcasts = listOf(
            createTestPodcast("1", "Tech Podcast", category = "Technology"),
            createTestPodcast("2", "Science Podcast", category = "Science"),
            createTestPodcast("3", "Another Tech Podcast", category = "Technology")
        )

        // When
        podcastDao.insertPodcasts(podcasts)
        val techPodcasts = podcastDao.getPodcastsByCategory("Technology").first()

        // Then
        assertEquals(2, techPodcasts.size)
        assertTrue(techPodcasts.all { it.category == "Technology" })
    }

    @Test
    fun getSubscribedPodcastCount() = runTest {
        // Given
        val podcasts = listOf(
            createTestPodcast("1", "Subscribed 1", isSubscribed = true),
            createTestPodcast("2", "Not Subscribed", isSubscribed = false),
            createTestPodcast("3", "Subscribed 2", isSubscribed = true)
        )

        // When
        podcastDao.insertPodcasts(podcasts)
        val count = podcastDao.getSubscribedPodcastCount()

        // Then
        assertEquals(2, count)
    }

    @Test
    fun updateLastUpdated() = runTest {
        // Given
        val podcast = createTestPodcast("1", "Test Podcast")
        val newTimestamp = System.currentTimeMillis() + 10000

        // When
        podcastDao.insertPodcast(podcast)
        podcastDao.updateLastUpdated("1", newTimestamp)
        val updated = podcastDao.getPodcastById("1")

        // Then
        assertNotNull(updated)
        assertEquals(newTimestamp, updated?.lastUpdated)
    }

    @Test
    fun deleteUnsubscribedPodcasts() = runTest {
        // Given
        val podcasts = listOf(
            createTestPodcast("1", "Subscribed", isSubscribed = true),
            createTestPodcast("2", "Not Subscribed 1", isSubscribed = false),
            createTestPodcast("3", "Not Subscribed 2", isSubscribed = false)
        )

        // When
        podcastDao.insertPodcasts(podcasts)
        podcastDao.deleteUnsubscribedPodcasts()
        val remaining = podcastDao.getAllPodcasts().first()

        // Then
        assertEquals(1, remaining.size)
        assertEquals("Subscribed", remaining[0].title)
        assertTrue(remaining[0].isSubscribed)
    }

    private fun createTestPodcast(
        id: String,
        title: String,
        author: String = "Test Author",
        category: String = "Technology",
        isSubscribed: Boolean = false
    ) = Podcast(
        id = id,
        title = title,
        description = "Test Description",
        imageUrl = "https://example.com/image.jpg",
        author = author,
        category = category,
        isSubscribed = isSubscribed,
        lastUpdated = System.currentTimeMillis()
    )
}