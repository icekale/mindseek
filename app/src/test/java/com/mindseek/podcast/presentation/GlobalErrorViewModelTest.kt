package com.mindseek.podcast.presentation

import com.mindseek.podcast.core.error.AudioPlaybackException
import com.mindseek.podcast.core.error.ErrorEvent
import com.mindseek.podcast.core.error.ErrorHandler
import com.mindseek.podcast.core.error.StorageException
import com.mindseek.podcast.data.remote.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class GlobalErrorViewModelTest {

    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: GlobalErrorViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        errorHandler = ErrorHandler()
        viewModel = GlobalErrorViewModel(errorHandler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should be initialized with error handler`() {
        // Then
        assertNotNull(viewModel.errorHandler)
        assertEquals(errorHandler, viewModel.errorHandler)
    }

    @Test
    fun `should handle network error correctly`() = runTest {
        // Given
        val networkError = NetworkException.NetworkError("Connection failed")
        val context = "Loading podcasts"

        // When
        errorHandler.handleError(networkError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.NetworkError)
        assertEquals("Connection failed", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }

    @Test
    fun `should handle server error correctly`() = runTest {
        // Given
        val serverError = NetworkException.ServerError(500, "Internal server error")
        val context = "Fetching episodes"

        // When
        errorHandler.handleError(serverError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ServerError)
        assertEquals("服务器错误(500)", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }

    @Test
    fun `should handle client error correctly`() = runTest {
        // Given
        val clientError = NetworkException.ClientError(401, "Unauthorized")
        val context = "User authentication"

        // When
        errorHandler.handleError(clientError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ClientError)
        assertEquals("请先登录", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle 403 forbidden error correctly`() = runTest {
        // Given
        val forbiddenError = NetworkException.ClientError(403, "Forbidden")
        val context = "Access restricted content"

        // When
        errorHandler.handleError(forbiddenError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ClientError)
        assertEquals("没有权限访问", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle 404 not found error correctly`() = runTest {
        // Given
        val notFoundError = NetworkException.ClientError(404, "Not found")
        val context = "Loading podcast details"

        // When
        errorHandler.handleError(notFoundError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ClientError)
        assertEquals("内容不存在", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle 429 rate limit error correctly`() = runTest {
        // Given
        val rateLimitError = NetworkException.ClientError(429, "Too many requests")
        val context = "API requests"

        // When
        errorHandler.handleError(rateLimitError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ClientError)
        assertEquals("请求过于频繁，请稍后再试", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle parse error correctly`() = runTest {
        // Given
        val parseError = NetworkException.ParseError("JSON parsing failed")
        val context = "Parsing response"

        // When
        errorHandler.handleError(parseError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.DataError)
        assertEquals("数据解析失败", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle audio playback error correctly`() = runTest {
        // Given
        val audioError = AudioPlaybackException("Failed to play audio")
        val context = "Playing episode"

        // When
        errorHandler.handleError(audioError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.AudioError)
        assertEquals("Failed to play audio", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }

    @Test
    fun `should handle storage error correctly`() = runTest {
        // Given
        val storageError = StorageException("Insufficient storage space")
        val context = "Downloading episode"

        // When
        errorHandler.handleError(storageError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.StorageError)
        assertEquals("Insufficient storage space", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle unknown error correctly`() = runTest {
        // Given
        val unknownError = RuntimeException("Unexpected error")
        val context = "Unknown operation"

        // When
        errorHandler.handleError(unknownError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.UnknownError)
        assertEquals("Unexpected error", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }

    @Test
    fun `should handle custom error message correctly`() = runTest {
        // Given
        val customMessage = "Custom error message"
        val context = "Custom operation"

        // When
        errorHandler.handleError(customMessage, context, isRetryable = false)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.CustomError)
        assertEquals(customMessage, errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }

    @Test
    fun `should handle error with empty context`() = runTest {
        // Given
        val error = RuntimeException("Test error")

        // When
        errorHandler.handleError(error)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertEquals("", errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }

    @Test
    fun `should handle error with null message`() = runTest {
        // Given
        val error = RuntimeException(null as String?)
        val context = "Test context"

        // When
        errorHandler.handleError(error, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertEquals("发生未知错误", errorEvent.message)
        assertEquals(context, errorEvent.context)
    }

    @Test
    fun `should handle audio error with null message`() = runTest {
        // Given
        val audioError = AudioPlaybackException(null as String?)
        val context = "Audio playback"

        // When
        errorHandler.handleError(audioError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.AudioError)
        assertEquals("音频播放失败", errorEvent.message)
        assertEquals(context, errorEvent.context)
    }

    @Test
    fun `should handle storage error with null message`() = runTest {
        // Given
        val storageError = StorageException(null as String?)
        val context = "Storage operation"

        // When
        errorHandler.handleError(storageError, context)
        advanceUntilIdle()

        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.StorageError)
        assertEquals("存储空间不足", errorEvent.message)
        assertEquals(context, errorEvent.context)
    }
}