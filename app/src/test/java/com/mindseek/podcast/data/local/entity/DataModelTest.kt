package com.mindseek.podcast.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simple unit tests for data model classes that don't require Android runtime
 */
class DataModelTest {

    @Test
    fun `podcast data class should be created with correct values`() {
        // Given
        val podcast = Podcast(
            id = "1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            isSubscribed = true,
            lastUpdated = 1234567890L
        )

        // Then
        assertEquals("1", podcast.id)
        assertEquals("Test Podcast", podcast.title)
        assertEquals("Test Description", podcast.description)
        assertEquals("https://example.com/image.jpg", podcast.imageUrl)
        assertEquals("Test Author", podcast.author)
        assertEquals("Technology", podcast.category)
        assertTrue(podcast.isSubscribed)
        assertEquals(1234567890L, podcast.lastUpdated)
    }

    @Test
    fun `podcast should have default subscription status as false`() {
        // Given
        val podcast = Podcast(
            id = "1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            lastUpdated = 1234567890L
        )

        // Then
        assertFalse(podcast.isSubscribed)
    }

    @Test
    fun `episode data class should be created with correct values`() {
        // Given
        val episode = Episode(
            id = "1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Episode Description",
            audioUrl = "https://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = 1234567890L,
            isDownloaded = true,
            localPath = "/storage/audio.mp3"
        )

        // Then
        assertEquals("1", episode.id)
        assertEquals("podcast1", episode.podcastId)
        assertEquals("Test Episode", episode.title)
        assertEquals("Test Episode Description", episode.description)
        assertEquals("https://example.com/audio.mp3", episode.audioUrl)
        assertEquals(3600000L, episode.duration)
        assertEquals(1234567890L, episode.publishDate)
        assertTrue(episode.isDownloaded)
        assertEquals("/storage/audio.mp3", episode.localPath)
    }

    @Test
    fun `episode should have default download status as false`() {
        // Given
        val episode = Episode(
            id = "1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Episode Description",
            audioUrl = "https://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = 1234567890L
        )

        // Then
        assertFalse(episode.isDownloaded)
        assertNull(episode.localPath)
    }

    @Test
    fun `play history data class should be created with correct values`() {
        // Given
        val playHistory = PlayHistory(
            id = "1",
            episodeId = "episode1",
            playPosition = 1800000L,
            playDate = 1234567890L
        )

        // Then
        assertEquals("1", playHistory.id)
        assertEquals("episode1", playHistory.episodeId)
        assertEquals(1800000L, playHistory.playPosition)
        assertEquals(1234567890L, playHistory.playDate)
    }

    @Test
    fun `favorite data class should be created with correct values`() {
        // Given
        val favorite = Favorite(
            id = "1",
            episodeId = "episode1",
            addedDate = 1234567890L
        )

        // Then
        assertEquals("1", favorite.id)
        assertEquals("episode1", favorite.episodeId)
        assertEquals(1234567890L, favorite.addedDate)
    }

    @Test
    fun `podcast with episodes relationship should work correctly`() {
        // Given
        val podcast = Podcast(
            id = "1",
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            lastUpdated = 1234567890L
        )

        val episodes = listOf(
            Episode(
                id = "1",
                podcastId = "1",
                title = "Episode 1",
                description = "First episode",
                audioUrl = "https://example.com/audio1.mp3",
                duration = 1800000L,
                publishDate = 1234567890L
            ),
            Episode(
                id = "2",
                podcastId = "1",
                title = "Episode 2",
                description = "Second episode",
                audioUrl = "https://example.com/audio2.mp3",
                duration = 2100000L,
                publishDate = 1234567900L
            )
        )

        val podcastWithEpisodes = PodcastWithEpisodes(
            podcast = podcast,
            episodes = episodes
        )

        // Then
        assertEquals(podcast, podcastWithEpisodes.podcast)
        assertEquals(2, podcastWithEpisodes.episodes.size)
        assertEquals("Episode 1", podcastWithEpisodes.episodes[0].title)
        assertEquals("Episode 2", podcastWithEpisodes.episodes[1].title)
    }
}