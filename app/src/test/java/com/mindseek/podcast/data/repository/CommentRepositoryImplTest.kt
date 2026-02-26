package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.CommentDao
import com.mindseek.podcast.data.local.entity.Comment
import com.mindseek.podcast.data.remote.api.CommentApiService
import com.mindseek.podcast.data.remote.dto.CommentDto
import com.mindseek.podcast.domain.model.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommentRepositoryImplTest {

    @Mock
    private lateinit var commentDao: CommentDao

    @Mock
    private lateinit var commentApiService: CommentApiService

    private lateinit var repository: CommentRepositoryImpl

    private val sampleComment = Comment(
        id = "1",
        episodeId = "episode1",
        userId = "user1",
        content = "Great episode!",
        timestamp = System.currentTimeMillis(),
        likeCount = 5,
        parentCommentId = null
    )

    private val sampleCommentDto = CommentDto(
        id = "1",
        episodeId = "episode1",
        userId = "user1",
        content = "Great episode!",
        timestamp = System.currentTimeMillis(),
        likeCount = 5,
        parentCommentId = null
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = CommentRepositoryImpl(commentDao, commentApiService)
    }

    @Test
    fun `getCommentsByEpisodeId returns comments with replies`() = runTest {
        // Given
        val comments = listOf(sampleComment)
        val replies = emptyList<Comment>()
        whenever(commentDao.getCommentsByEpisodeId("episode1")).thenReturn(flowOf(comments))
        whenever(commentDao.getRepliesByParentId("1")).thenReturn(flowOf(replies))

        // When
        val result = repository.getCommentsByEpisodeId("episode1").first()

        // Then
        assertEquals(1, result.size)
        assertEquals(sampleComment.content, result[0].content)
        verify(commentDao).getCommentsByEpisodeId("episode1")
    }

    @Test
    fun `getTopLevelCommentsByEpisodeId returns top level comments only`() = runTest {
        // Given
        val comments = listOf(sampleComment)
        val replies = emptyList<Comment>()
        whenever(commentDao.getTopLevelCommentsByEpisodeId("episode1")).thenReturn(flowOf(comments))
        whenever(commentDao.getRepliesByParentId("1")).thenReturn(flowOf(replies))

        // When
        val result = repository.getTopLevelCommentsByEpisodeId("episode1").first()

        // Then
        assertEquals(1, result.size)
        assertEquals(sampleComment.content, result[0].content)
        verify(commentDao).getTopLevelCommentsByEpisodeId("episode1")
    }

    @Test
    fun `postComment posts to API and saves locally on success`() = runTest {
        // Given
        whenever(commentApiService.postComment(eq("episode1"), any())).thenReturn(sampleCommentDto)

        // When
        val result = repository.postComment("episode1", "Great episode!")

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("Great episode!", result.data?.content)
        verify(commentApiService).postComment(eq("episode1"), any())
        verify(commentDao).insertComment(any())
    }

    @Test
    fun `postComment saves locally when API fails`() = runTest {
        // Given
        whenever(commentApiService.postComment(eq("episode1"), any())).thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.postComment("episode1", "Great episode!")

        // Then
        assertTrue(result is Resource.Success)
        assertEquals("Great episode!", result.data?.content)
        verify(commentApiService).postComment(eq("episode1"), any())
        verify(commentDao).insertComment(any())
    }

    @Test
    fun `likeComment updates API and local database`() = runTest {
        // Given
        val updatedCommentDto = sampleCommentDto.copy(likeCount = 6)
        whenever(commentApiService.likeComment("1")).thenReturn(updatedCommentDto)
        whenever(commentDao.getCommentById("1")).thenReturn(sampleComment.copy(likeCount = 6))

        // When
        val result = repository.likeComment("1")

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(6, result.data?.likeCount)
        verify(commentApiService).likeComment("1")
        verify(commentDao).updateLikeCount("1", 6)
    }

    @Test
    fun `deleteComment removes from API and local database`() = runTest {
        // Given
        whenever(commentDao.getCommentById("1")).thenReturn(sampleComment)

        // When
        val result = repository.deleteComment("1")

        // Then
        assertTrue(result is Resource.Success)
        verify(commentApiService).deleteComment("1")
        verify(commentDao).deleteComment(sampleComment)
    }

    @Test
    fun `refreshComments fetches from API and updates local database`() = runTest {
        // Given
        val remoteComments = listOf(sampleCommentDto)
        whenever(commentApiService.getCommentsByEpisodeId("episode1")).thenReturn(remoteComments)

        // When
        val result = repository.refreshComments("episode1")

        // Then
        assertTrue(result is Resource.Success)
        verify(commentApiService).getCommentsByEpisodeId("episode1")
        verify(commentDao).deleteCommentsByEpisodeId("episode1")
        verify(commentDao).insertComments(any())
    }

    @Test
    fun `getCommentCount returns count from dao`() = runTest {
        // Given
        whenever(commentDao.getCommentCountByEpisodeId("episode1")).thenReturn(10)

        // When
        val result = repository.getCommentCount("episode1")

        // Then
        assertEquals(10, result)
        verify(commentDao).getCommentCountByEpisodeId("episode1")
    }
}