package com.mindseek.podcast.data.remote

import com.mindseek.podcast.data.remote.api.PodcastApiService
import com.mindseek.podcast.data.remote.dto.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiServiceWrapperTest {

    @Mock
    private lateinit var podcastApiService: PodcastApiService

    private lateinit var apiServiceWrapper: ApiServiceWrapper

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apiServiceWrapper = ApiServiceWrapper(podcastApiService)
    }

    @Test
    fun `getRecommendedPodcasts returns success when API call succeeds`() = runTest {
        // Given
        val mockPodcasts = listOf(
            PodcastDto(
                id = "1",
                title = "Test Podcast",
                description = "Test Description",
                imageUrl = "https://example.com/image.jpg",
                author = "Test Author",
                category = "Technology",
                lastUpdated = System.currentTimeMillis()
            )
        )
        val mockResponse = PaginatedResponse(
            items = mockPodcasts,
            page = 1,
            limit = 20,
            totalCount = 1,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false
        )
        whenever(podcastApiService.getRecommendedPodcasts(1, 20, null))
            .thenReturn(Response.success(mockResponse))

        // When
        val result = apiServiceWrapper.getRecommendedPodcasts()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockResponse, result.data)
    }

    @Test
    fun `searchPodcasts returns success with search results`() = runTest {
        // Given
        val query = "technology"
        val mockPodcasts = listOf(
            PodcastDto(
                id = "1",
                title = "Tech Podcast",
                description = "Technology discussions",
                imageUrl = "https://example.com/tech.jpg",
                author = "Tech Author",
                category = "Technology",
                lastUpdated = System.currentTimeMillis()
            )
        )
        val mockSearchResponse = SearchResponse(
            query = query,
            results = mockPodcasts,
            totalResults = 1,
            searchTimeMs = 50,
            suggestions = emptyList(),
            page = 1,
            limit = 20
        )
        whenever(podcastApiService.searchPodcasts(query, 1, 20, null, "relevance"))
            .thenReturn(Response.success(mockSearchResponse))

        // When
        val result = apiServiceWrapper.searchPodcasts(query)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockSearchResponse, result.data)
    }

    @Test
    fun `getPodcastById returns success when podcast exists`() = runTest {
        // Given
        val podcastId = "test-id"
        val mockPodcast = PodcastDto(
            id = podcastId,
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            lastUpdated = System.currentTimeMillis()
        )
        val mockApiResponse = ApiResponse(
            success = true,
            data = mockPodcast
        )
        whenever(podcastApiService.getPodcastById(podcastId))
            .thenReturn(Response.success(mockApiResponse))

        // When
        val result = apiServiceWrapper.getPodcastById(podcastId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockPodcast, result.data)
    }
}