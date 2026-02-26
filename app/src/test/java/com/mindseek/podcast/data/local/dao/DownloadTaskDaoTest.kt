package com.mindseek.podcast.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.data.local.PodcastDatabase
import com.mindseek.podcast.data.local.entity.DownloadTask
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DownloadTaskDaoTest {

    private lateinit var database: PodcastDatabase
    private lateinit var downloadTaskDao: DownloadTaskDao
    private lateinit var podcastDao: PodcastDao
    private lateinit var episodeDao: EpisodeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        downloadTaskDao = database.downloadTaskDao()
        podcastDao = database.podcastDao()
        episodeDao = database.episodeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetDownloadTask() = runTest {
        // Given
        val podcast = Podcast(
            id = "podcast1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "http://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            rssUrl = "http://example.com/rss.xml"
        )
        
        val episode = Episode(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val downloadTask = DownloadTask(
            id = "task1",
            episodeId = "episode1",
            audioUrl = "http://example.com/audio.mp3",
            status = "pending",
            progress = 0f,
            createdAt = System.currentTimeMillis()
        )

        // Insert dependencies first
        podcastDao.insertPodcast(podcast)
        episodeDao.insertEpisode(episode)

        // When
        downloadTaskDao.insertDownloadTask(downloadTask)
        val retrieved = downloadTaskDao.getDownloadTaskById("task1")

        // Then
        assertNotNull(retrieved)
        assertEquals("task1", retrieved?.id)
        assertEquals("episode1", retrieved?.episodeId)
        assertEquals("pending", retrieved?.status)
    }

    @Test
    fun getAllDownloadTasks() = runTest {
        // Given
        val podcast = Podcast(
            id = "podcast1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "http://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            rssUrl = "http://example.com/rss.xml"
        )
        
        val episode1 = Episode(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode 1",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio1.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val episode2 = Episode(
            id = "episode2",
            podcastId = "podcast1",
            title = "Test Episode 2",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio2.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val task1 = DownloadTask(
            id = "task1",
            episodeId = "episode1",
            audioUrl = "http://example.com/audio1.mp3",
            status = "pending",
            progress = 0f,
            createdAt = System.currentTimeMillis() - 1000
        )
        
        val task2 = DownloadTask(
            id = "task2",
            episodeId = "episode2",
            audioUrl = "http://example.com/audio2.mp3",
            status = "completed",
            progress = 1f,
            createdAt = System.currentTimeMillis()
        )

        // Insert dependencies
        podcastDao.insertPodcast(podcast)
        episodeDao.insertEpisode(episode1)
        episodeDao.insertEpisode(episode2)

        // When
        downloadTaskDao.insertDownloadTask(task1)
        downloadTaskDao.insertDownloadTask(task2)
        val allTasks = downloadTaskDao.getAllDownloadTasks().first()

        // Then
        assertEquals(2, allTasks.size)
        // Should be ordered by createdAt DESC, so task2 should be first
        assertEquals("task2", allTasks[0].id)
        assertEquals("task1", allTasks[1].id)
    }

    @Test
    fun getDownloadTasksByStatus() = runTest {
        // Given
        val podcast = Podcast(
            id = "podcast1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "http://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            rssUrl = "http://example.com/rss.xml"
        )
        
        val episode1 = Episode(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode 1",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio1.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val episode2 = Episode(
            id = "episode2",
            podcastId = "podcast1",
            title = "Test Episode 2",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio2.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val pendingTask = DownloadTask(
            id = "task1",
            episodeId = "episode1",
            audioUrl = "http://example.com/audio1.mp3",
            status = "pending",
            progress = 0f,
            createdAt = System.currentTimeMillis()
        )
        
        val completedTask = DownloadTask(
            id = "task2",
            episodeId = "episode2",
            audioUrl = "http://example.com/audio2.mp3",
            status = "completed",
            progress = 1f,
            createdAt = System.currentTimeMillis()
        )

        // Insert dependencies
        podcastDao.insertPodcast(podcast)
        episodeDao.insertEpisode(episode1)
        episodeDao.insertEpisode(episode2)

        // When
        downloadTaskDao.insertDownloadTask(pendingTask)
        downloadTaskDao.insertDownloadTask(completedTask)
        val pendingTasks = downloadTaskDao.getDownloadTasksByStatus("pending").first()
        val completedTasks = downloadTaskDao.getDownloadTasksByStatus("completed").first()

        // Then
        assertEquals(1, pendingTasks.size)
        assertEquals("task1", pendingTasks[0].id)
        
        assertEquals(1, completedTasks.size)
        assertEquals("task2", completedTasks[0].id)
    }

    @Test
    fun updateDownloadProgress() = runTest {
        // Given
        val podcast = Podcast(
            id = "podcast1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "http://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            rssUrl = "http://example.com/rss.xml"
        )
        
        val episode = Episode(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val downloadTask = DownloadTask(
            id = "task1",
            episodeId = "episode1",
            audioUrl = "http://example.com/audio.mp3",
            status = "downloading",
            progress = 0f,
            downloadedBytes = 0L,
            createdAt = System.currentTimeMillis()
        )

        // Insert dependencies
        podcastDao.insertPodcast(podcast)
        episodeDao.insertEpisode(episode)
        downloadTaskDao.insertDownloadTask(downloadTask)

        // When
        downloadTaskDao.updateDownloadProgress("task1", 0.5f, 1024L)
        val updated = downloadTaskDao.getDownloadTaskById("task1")

        // Then
        assertNotNull(updated)
        assertEquals(0.5f, updated?.progress)
        assertEquals(1024L, updated?.downloadedBytes)
    }

    @Test
    fun deleteDownloadTaskByEpisodeId() = runTest {
        // Given
        val podcast = Podcast(
            id = "podcast1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "http://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            rssUrl = "http://example.com/rss.xml"
        )
        
        val episode = Episode(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Episode Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis()
        )
        
        val downloadTask = DownloadTask(
            id = "task1",
            episodeId = "episode1",
            audioUrl = "http://example.com/audio.mp3",
            status = "pending",
            progress = 0f,
            createdAt = System.currentTimeMillis()
        )

        // Insert dependencies
        podcastDao.insertPodcast(podcast)
        episodeDao.insertEpisode(episode)
        downloadTaskDao.insertDownloadTask(downloadTask)

        // Verify task exists
        val beforeDelete = downloadTaskDao.getDownloadTaskByEpisodeId("episode1")
        assertNotNull(beforeDelete)

        // When
        downloadTaskDao.deleteDownloadTaskByEpisodeId("episode1")
        val afterDelete = downloadTaskDao.getDownloadTaskByEpisodeId("episode1")

        // Then
        assertNull(afterDelete)
    }
}