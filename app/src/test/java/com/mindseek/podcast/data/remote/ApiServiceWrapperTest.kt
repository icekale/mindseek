package com.mindseek.podcast.data.remote

import com.mindseek.podcast.data.remote.api.CommentApiService
import com.mindseek.podcast.data.remote.api.PodcastApiService
import com.mindseek.podcast.data.remote.api.PostCommentRequest
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

    @Mock
    private lateinit var commentApiService: CommentApiService

    private lateinit var apiServiceWrapper: ApiServiceWrapper

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apiServiceWrapper = ApiServiceWrapper(podcastApiService, commentApiService)
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

    @Test
    fun `postComment returns success when comment is posted`() = runTest {
        // Given
        val episodeId = "episode-1"
        val content = "Great episode!"
        val mockComment = CommentDto(
            id = "comment-1",
            episodeId = episodeId,
            userId = "user-1",
            userName = "Test User",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val mockApiResponse = ApiResponse(
            success = true,
            data = mockComment
        )
        whenever(commentApiService.postComment(episodeId, PostCommentRequest(content, null)))
            .thenReturn(Response.success(mockApiResponse))

        // When
        val result = apiServiceWrapper.postComment(episodeId, content)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockComment, result.data)
    }

    @Test
    fun `getCommentsByEpisodeId returns paginated comments`() = runTest {
        // Given
        val episodeId = "episode-1"
        val mockComments = listOf(
            CommentDto(
                id = "comment-1",
                episodeId = episodeId,
                userId = "user-1",
                userName = "User 1",
                content = "Great episode!",
                timestamp = System.currentTimeMillis()
            ),
            CommentDto(
                id = "comment-2",
                episodeId = episodeId,
                userId = "user-2",
                userName = "User 2",
                content = "Interesting discussion",
                timestamp = System.currentTimeMillis()
            )
        )
        val mockResponse = PaginatedResponse(
            items = mockComments,
            page = 1,
            limit = 20,
            totalCount = 2,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false
        )
        whenever(commentApiService.getCommentsByEpisodeId(episodeId, 1, 20, "recent"))
            .thenReturn(Response.success(mockResponse))

        // When
        val result = apiServiceWrapper.getCommentsByEpisodeId(episodeId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockResponse, result.data)
        assertEquals(2, result.data.items.size)
    }

    @Test
    fun `subscribeToPodcast returns success when subscription succeeds`() = runTest {
        // Given
        val podcastId = "podcast-1"
        val mockApiResponse = ApiResponse<Unit>(success = true)
        whenever(podcastApiService.subscribeToPodcast(podcastId))
            .thenReturn(Response.success(mockApiResponse))

        // When
        val result = apiServiceWrapper.subscribeToPodcast(podcastId)

        // Then
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `likeComment returns updated comment with increased like count`() = runTest {
        // Given
        val commentId = "comment-1"
        val mockComment = CommentDto(
            id = commentId,
            episodeId = "episode-1",
            userId = "user-1",
            userName = "Test User",
            content = "Great episode!",
            timestamp = System.currentTimeMillis(),
            likeCount = 5,
            isLiked = true
        )
        val mockApiResponse = ApiResponse(
            success = true,
            data = mockComment
        )
        whenever(commentApiService.likeComment(commentId))
            .thenReturn(Response.success(mockApiResponse))

        // When
        val result = apiServiceWrapper.likeComment(commentId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockComment, result.data)
        assertEquals(5, result.data.likeCount)
        assertTrue(result.data.isLiked)
    }
}