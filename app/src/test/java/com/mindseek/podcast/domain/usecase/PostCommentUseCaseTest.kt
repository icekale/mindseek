package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.any

class PostCommentUseCaseTest {

    @Mock
    private lateinit var commentRepository: CommentRepository
    private lateinit var postCommentUseCase: PostCommentUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        postCommentUseCase = PostCommentUseCase(commentRepository)
    }

    @Test
    fun `invoke with valid content should post comment successfully`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "This is a valid comment"
        val expectedComment = CommentDomain(
            id = "comment123",
            episodeId = episodeId,
            userId = "user123",
            userName = "Test User",
            content = content,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            isLiked = false,
            replies = emptyList()
        )

        whenever(commentRepository.postComment(episodeId, content, null))
            .thenReturn(Resource.Success(expectedComment))

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedComment, (result as Resource.Success).data)
        verify(commentRepository).postComment(episodeId, content, null)
    }

    @Test
    fun `invoke with reply should post reply comment successfully`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "This is a reply"
        val parentCommentId = "parent123"
        val expectedComment = CommentDomain(
            id = "comment123",
            episodeId = episodeId,
            userId = "user123",
            userName = "Test User",
            content = content,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            isLiked = false,
            replies = emptyList()
        )

        whenever(commentRepository.postComment(episodeId, content, parentCommentId))
            .thenReturn(Resource.Success(expectedComment))

        // When
        val result = postCommentUseCase(episodeId, content, parentCommentId)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedComment, (result as Resource.Success).data)
        verify(commentRepository).postComment(episodeId, content, parentCommentId)
    }

    @Test
    fun `invoke with empty content should return validation error`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = ""

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("评论内容不能为空", (result as Resource.Error).message)
        verify(commentRepository, never()).postComment(any(), any(), any())
    }

    @Test
    fun `invoke with blank content should return validation error`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "   "

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("评论内容不能为空", (result as Resource.Error).message)
        verify(commentRepository, never()).postComment(any(), any(), any())
    }

    @Test
    fun `invoke with too short content should return validation error`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "a"

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("评论内容至少需要5个字符", (result as Resource.Error).message)
        verify(commentRepository, never()).postComment(any(), any(), any())
    }

    @Test
    fun `invoke with too long content should return validation error`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "a".repeat(501) // 501 characters

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("评论内容不能超过500个字符", (result as Resource.Error).message)
        verify(commentRepository, never()).postComment(any(), any(), any())
    }

    @Test
    fun `invoke with content containing control characters should return validation error`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "Valid content\u0001with control character"

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("评论内容包含非法字符", (result as Resource.Error).message)
        verify(commentRepository, never()).postComment(any(), any(), any())
    }

    @Test
    fun `invoke with valid content containing newlines should succeed`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "This is a valid comment\nwith newlines\nand tabs\t"
        val expectedComment = CommentDomain(
            id = "comment123",
            episodeId = episodeId,
            userId = "user123",
            userName = "Test User",
            content = content,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            isLiked = false,
            replies = emptyList()
        )

        whenever(commentRepository.postComment(episodeId, content, null))
            .thenReturn(Resource.Success(expectedComment))

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedComment, (result as Resource.Success).data)
        verify(commentRepository).postComment(episodeId, content, null)
    }

    @Test
    fun `invoke when repository returns error should return error`() = runTest {
        // Given
        val episodeId = "episode123"
        val content = "Valid comment"
        val errorMessage = "Network error"

        whenever(commentRepository.postComment(episodeId, content, null))
            .thenReturn(Resource.Error(errorMessage))

        // When
        val result = postCommentUseCase(episodeId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
        verify(commentRepository).postComment(episodeId, content, null)
    }
}