package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchPodcastsUseCaseTest {

    @Mock
    private lateinit var podcastRepository: PodcastRepository

    private lateinit var searchPodcastsUseCase: SearchPodcastsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        searchPodcastsUseCase = SearchPodcastsUseCase(podcastRepository)
    }

    @Test
    fun `invoke with valid query should return success with podcasts`() = runTest {
        val query = "test"
        val mockEntityPodcasts = listOf(
            Podcast(
                id = "1",
                title = "Test Podcast",
                description = "Description",
                imageUrl = "url",
                author = "Author",
                category = "Category",
                isSubscribed = false,
                lastUpdated = System.currentTimeMillis()
            )
        )

        whenever(podcastRepository.searchPodcasts(query, 1))
            .thenReturn(mockEntityPodcasts)

        val result = searchPodcastsUseCase(query).first()

        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.size)
        assertEquals("Test Podcast", result.data?.first()?.title)
    }

    @Test
    fun `invoke with blank query should return success with empty list`() = runTest {
        val result = searchPodcastsUseCase("").first()

        assertTrue(result is Resource.Success)
        assertEquals(emptyList(), result.data)
    }

    @Test
    fun `invoke with whitespace query should return success with empty list`() = runTest {
        val result = searchPodcastsUseCase("   ").first()

        assertTrue(result is Resource.Success)
        assertEquals(emptyList(), result.data)
    }

    @Test
    fun `invoke should handle repository exception`() = runTest {
        val query = "test"
        val errorMessage = "Network error"

        whenever(podcastRepository.searchPodcasts(query, 1))
            .thenThrow(RuntimeException(errorMessage))

        val result = searchPodcastsUseCase(query).first()

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `invoke should trim query before searching`() = runTest {
        val query = "  test  "
        val trimmedQuery = "test"
        val mockEntityPodcasts = listOf(
            Podcast(
                id = "1",
                title = "Test Podcast",
                description = "Description",
                imageUrl = "url",
                author = "Author",
                category = "Category",
                isSubscribed = false,
                lastUpdated = System.currentTimeMillis()
            )
        )

        whenever(podcastRepository.searchPodcasts(trimmedQuery, 1))
            .thenReturn(mockEntityPodcasts)

        val result = searchPodcastsUseCase(query).first()

        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.size)
    }
}