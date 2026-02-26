package com.mindseek.podcast.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.data.local.PodcastDatabase
import com.mindseek.podcast.data.local.entity.Favorite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PodcastDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        favoriteDao = database.favoriteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetFavorite() = runTest {
        // Given
        val favorite = createTestFavorite("1", "episode1")

        // When
        favoriteDao.insertFavorite(favorite)
        val retrieved = favoriteDao.getFavoriteByEpisodeId("episode1")

        // Then
        assertNotNull(retrieved)
        assertEquals(favorite.id, retrieved?.id)
        assertEquals(favorite.episodeId, retrieved?.episodeId)
        assertEquals(favorite.addedDate, retrieved?.addedDate)
    }

    @Test
    fun getAllFavorites() = runTest {
        // Given
        val favorites = listOf(
            createTestFavorite("1", "episode1", System.currentTimeMillis() - 20000),
            createTestFavorite("2", "episode2", System.currentTimeMillis() - 10000),
            createTestFavorite("3", "episode3", System.currentTimeMillis())
        )

        // When
        favoriteDao.insertFavorites(favorites)
        val allFavorites = favoriteDao.getAllFavorites().first()

        // Then
        assertEquals(3, allFavorites.size)
        // Should be ordered by addedDate DESC (most recent first)
        assertTrue(allFavorites[0].addedDate >= allFavorites[1].addedDate)
        assertTrue(allFavorites[1].addedDate >= allFavorites[2].addedDate)
    }

    @Test
    fun getRecentFavorites() = runTest {
        // Given
        val favorites = (1..30).map { i ->
            createTestFavorite(
                id = i.toString(),
                episodeId = "episode$i",
                addedDate = System.currentTimeMillis() - (i * 1000L)
            )
        }

        // When
        favoriteDao.insertFavorites(favorites)
        val recentFavorites = favoriteDao.getRecentFavorites(10).first()

        // Then
        assertEquals(10, recentFavorites.size)
        // Should be ordered by addedDate DESC (most recent first)
        for (i in 0 until recentFavorites.size - 1) {
            assertTrue(recentFavorites[i].addedDate >= recentFavorites[i + 1].addedDate)
        }
    }

    @Test
    fun getFavoritesByDateRange() = runTest {
        // Given
        val baseTime = System.currentTimeMillis()
        val favorites = listOf(
            createTestFavorite("1", "episode1", baseTime - 20000), // Before range
            createTestFavorite("2", "episode2", baseTime - 5000),  // In range
            createTestFavorite("3", "episode3", baseTime),         // In range
            createTestFavorite("4", "episode4", baseTime + 10000)  // After range
        )

        // When
        favoriteDao.insertFavorites(favorites)
        val rangeFavorites = favoriteDao.getFavoritesByDateRange(
            startDate = baseTime - 10000,
            endDate = baseTime + 5000
        ).first()

        // Then
        assertEquals(2, rangeFavorites.size)
        assertTrue(rangeFavorites.any { it.episodeId == "episode2" })
        assertTrue(rangeFavorites.any { it.episodeId == "episode3" })
    }

    @Test
    fun getFavoriteCount() = runTest {
        // Given
        val favorites = listOf(
            createTestFavorite("1", "episode1"),
            createTestFavorite("2", "episode2"),
            createTestFavorite("3", "episode3")
        )

        // When
        favoriteDao.insertFavorites(favorites)
        val count = favoriteDao.getFavoriteCount()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun isFavorite() = runTest {
        // Given
        val favorite = createTestFavorite("1", "episode1")

        // When
        favoriteDao.insertFavorite(favorite)
        val isFavorite = favoriteDao.isFavorite("episode1")
        val isNotFavorite = favoriteDao.isFavorite("episode2")

        // Then
        assertTrue(isFavorite)
        assertFalse(isNotFavorite)
    }

    @Test
    fun getFavoriteEpisodeIds() = runTest {
        // Given
        val favorites = listOf(
            createTestFavorite("1", "episode1", System.currentTimeMillis() - 20000),
            createTestFavorite("2", "episode2", System.currentTimeMillis() - 10000),
            createTestFavorite("3", "episode3", System.currentTimeMillis())
        )

        // When
        favoriteDao.insertFavorites(favorites)
        val episodeIds = favoriteDao.getFavoriteEpisodeIds().first()

        // Then
        assertEquals(3, episodeIds.size)
        assertTrue(episodeIds.contains("episode1"))
        assertTrue(episodeIds.contains("episode2"))
        assertTrue(episodeIds.contains("episode3"))
        // Should be ordered by addedDate DESC
        assertEquals("episode3", episodeIds[0]) // Most recent
        assertEquals("episode1", episodeIds[2]) // Oldest
    }

    @Test
    fun insertMultipleFavorites() = runTest {
        // Given
        val favorites = listOf(
            createTestFavorite("1", "episode1"),
            createTestFavorite("2", "episode2"),
            createTestFavorite("3", "episode3")
        )

        // When
        favoriteDao.insertFavorites(favorites)
        val allFavorites = favoriteDao.getAllFavorites().first()

        // Then
        assertEquals(3, allFavorites.size)
        assertTrue(allFavorites.any { it.episodeId == "episode1" })
        assertTrue(allFavorites.any { it.episodeId == "episode2" })
        assertTrue(allFavorites.any { it.episodeId == "episode3" })
    }

    @Test
    fun deleteFavorite() = runTest {
        // Given
        val favorite = createTestFavorite("1", "episode1")

        // When
        favoriteDao.insertFavorite(favorite)
        favoriteDao.deleteFavorite(favorite)
        val retrieved = favoriteDao.getFavoriteByEpisodeId("episode1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun deleteFavoriteByEpisodeId() = runTest {
        // Given
        val favorites = listOf(
            createTestFavorite("1", "episode1"),
            createTestFavorite("2", "episode2"),
            createTestFavorite("3", "episode3")
        )

        // When
        favoriteDao.insertFavorites(favorites)
        favoriteDao.deleteFavoriteByEpisodeId("episode2")
        val allFavorites = favoriteDao.getAllFavorites().first()

        // Then
        assertEquals(2, allFavorites.size)
        assertFalse(allFavorites.any { it.episodeId == "episode2" })
        assertTrue(allFavorites.any { it.episodeId == "episode1" })
        assertTrue(allFavorites.any { it.episodeId == "episode3" })
    }

    @Test
    fun clearAllFavorites() = runTest {
        // Given
        val favorites = listOf(
            createTestFavorite("1", "episode1"),
            createTestFavorite("2", "episode2"),
            createTestFavorite("3", "episode3")
        )

        // When
        favoriteDao.insertFavorites(favorites)
        favoriteDao.clearAllFavorites()
        val allFavorites = favoriteDao.getAllFavorites().first()

        // Then
        assertEquals(0, allFavorites.size)
    }

    @Test
    fun deleteOldFavorites() = runTest {
        // Given
        val baseTime = System.currentTimeMillis()
        val cutoffTime = baseTime - 10000
        val favorites = listOf(
            createTestFavorite("1", "episode1", baseTime - 20000), // Old
            createTestFavorite("2", "episode2", baseTime - 5000),  // Recent
            createTestFavorite("3", "episode3", baseTime)          // Recent
        )

        // When
        favoriteDao.insertFavorites(favorites)
        favoriteDao.deleteOldFavorites(cutoffTime)
        val remainingFavorites = favoriteDao.getAllFavorites().first()

        // Then
        assertEquals(2, remainingFavorites.size)
        assertTrue(remainingFavorites.all { it.addedDate >= cutoffTime })
        assertTrue(remainingFavorites.any { it.episodeId == "episode2" })
        assertTrue(remainingFavorites.any { it.episodeId == "episode3" })
    }

    @Test
    fun insertFavoriteWithConflictReplace() = runTest {
        // Given
        val originalFavorite = createTestFavorite("1", "episode1", System.currentTimeMillis() - 10000)
        val updatedFavorite = createTestFavorite("1", "episode1", System.currentTimeMillis())

        // When
        favoriteDao.insertFavorite(originalFavorite)
        favoriteDao.insertFavorite(updatedFavorite) // Should replace due to OnConflictStrategy.REPLACE
        val retrieved = favoriteDao.getFavoriteByEpisodeId("episode1")

        // Then
        assertNotNull(retrieved)
        assertEquals(updatedFavorite.addedDate, retrieved?.addedDate)
    }

    @Test
    fun getFavoriteByNonExistentEpisodeId() = runTest {
        // When
        val retrieved = favoriteDao.getFavoriteByEpisodeId("nonexistent")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun isFavoriteForNonExistentEpisode() = runTest {
        // When
        val isFavorite = favoriteDao.isFavorite("nonexistent")

        // Then
        assertFalse(isFavorite)
    }

    private fun createTestFavorite(
        id: String,
        episodeId: String,
        addedDate: Long = System.currentTimeMillis()
    ) = Favorite(
        id = id,
        episodeId = episodeId,
        addedDate = addedDate
    )
}