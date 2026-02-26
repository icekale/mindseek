package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.FavoriteDao
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Favorite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoriteRepositoryImplTest {

    @Mock
    private lateinit var favoriteDao: FavoriteDao

    @Mock
    private lateinit var episodeDao: EpisodeDao

    private lateinit var repository: FavoriteRepositoryImpl

    private val sampleEpisode = Episode(
        id = "episode1",
        podcastId = "podcast1",
        title = "Test Episode",
        description = "Test Description",
        audioUrl = "https://example.com/audio.mp3",
        duration = 3600000L,
        publishDate = System.currentTimeMillis(),
        isDownloaded = false,
        localPath = null
    )

    private val sampleFavorite = Favorite(
        id = "favorite1",
        episodeId = "episode1",
        addedDate = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = FavoriteRepositoryImpl(favoriteDao, episodeDao)
    }

    @Test
    fun `getAllFavorites returns episodes from favorites`() = runTest {
        // Given
        val favorites = listOf(sampleFavorite)
        whenever(favoriteDao.getAllFavorites()).thenReturn(flowOf(favorites))
        whenever(episodeDao.getEpisodeById("episode1")).thenReturn(sampleEpisode)

        // When
        val result = repository.getAllFavorites().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(sampleEpisode.title, result[0].title)
        verify(favoriteDao).getAllFavorites()
        verify(episodeDao).getEpisodeById("episode1")
    }

    @Test
    fun `getRecentFavorites returns limited episodes from favorites`() = runTest {
        // Given
        val favorites = listOf(sampleFavorite)
        whenever(favoriteDao.getRecentFavorites(10)).thenReturn(flowOf(favorites))
        whenever(episodeDao.getEpisodeById("episode1")).thenReturn(sampleEpisode)

        // When
        val result = repository.getRecentFavorites(10).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(sampleEpisode.title, result[0].title)
        verify(favoriteDao).getRecentFavorites(10)
        verify(episodeDao).getEpisodeById("episode1")
    }

    @Test
    fun `addToFavorites inserts favorite successfully`() = runTest {
        // When
        val result = repository.addToFavorites("episode1")

        // Then
        assertTrue(result)
        verify(favoriteDao).insertFavorite(any())
    }

    @Test
    fun `addToFavorites returns false on error`() = runTest {
        // Given
        whenever(favoriteDao.insertFavorite(any())).thenThrow(RuntimeException("Database error"))

        // When
        val result = repository.addToFavorites("episode1")

        // Then
        assertFalse(result)
        verify(favoriteDao).insertFavorite(any())
    }

    @Test
    fun `removeFromFavorites deletes favorite successfully`() = runTest {
        // When
        val result = repository.removeFromFavorites("episode1")

        // Then
        assertTrue(result)
        verify(favoriteDao).deleteFavoriteByEpisodeId("episode1")
    }

    @Test
    fun `removeFromFavorites returns false on error`() = runTest {
        // Given
        whenever(favoriteDao.deleteFavoriteByEpisodeId("episode1")).thenThrow(RuntimeException("Database error"))

        // When
        val result = repository.removeFromFavorites("episode1")

        // Then
        assertFalse(result)
        verify(favoriteDao).deleteFavoriteByEpisodeId("episode1")
    }

    @Test
    fun `isFavorite returns true when episode is favorite`() = runTest {
        // Given
        whenever(favoriteDao.isFavorite("episode1")).thenReturn(true)

        // When
        val result = repository.isFavorite("episode1")

        // Then
        assertTrue(result)
        verify(favoriteDao).isFavorite("episode1")
    }

    @Test
    fun `isFavorite returns false when episode is not favorite`() = runTest {
        // Given
        whenever(favoriteDao.isFavorite("episode1")).thenReturn(false)

        // When
        val result = repository.isFavorite("episode1")

        // Then
        assertFalse(result)
        verify(favoriteDao).isFavorite("episode1")
    }

    @Test
    fun `getFavoriteCount returns count from dao`() = runTest {
        // Given
        whenever(favoriteDao.getFavoriteCount()).thenReturn(5)

        // When
        val result = repository.getFavoriteCount()

        // Then
        assertEquals(5, result)
        verify(favoriteDao).getFavoriteCount()
    }

    @Test
    fun `clearAllFavorites clears all favorites`() = runTest {
        // When
        repository.clearAllFavorites()

        // Then
        verify(favoriteDao).clearAllFavorites()
    }
}