package com.mindseek.podcast.core.error

import com.mindseek.podcast.data.remote.NetworkException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ErrorHandlerTest {
    
    private lateinit var errorHandler: ErrorHandler
    
    @Before
    fun setUp() {
        errorHandler = ErrorHandler()
    }
    
    @Test
    fun `handleError with NetworkException should emit NetworkError event`() = runTest {
        // Given
        val networkException = NetworkException.NetworkError("Connection failed")
        val context = "Loading podcasts"
        
        // When
        errorHandler.handleError(networkException, context)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.NetworkError)
        assertEquals("Connection failed", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }
    
    @Test
    fun `handleError with ServerError should emit ServerError event`() = runTest {
        // Given
        val serverException = NetworkException.ServerError(500, "Internal server error")
        val context = "Fetching episodes"
        
        // When
        errorHandler.handleError(serverException, context)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ServerError)
        assertEquals("服务器错误(500)", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }
    
    @Test
    fun `handleError with ClientError should emit ClientError event with appropriate message`() = runTest {
        // Given
        val clientException = NetworkException.ClientError(401, "Unauthorized")
        val context = "User authentication"
        
        // When
        errorHandler.handleError(clientException, context)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.ClientError)
        assertEquals("请先登录", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }
    
    @Test
    fun `handleError with AudioPlaybackException should emit AudioError event`() = runTest {
        // Given
        val audioException = AudioPlaybackException("Failed to play audio")
        val context = "Playing episode"
        
        // When
        errorHandler.handleError(audioException, context)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.AudioError)
        assertEquals("Failed to play audio", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }
    
    @Test
    fun `handleError with StorageException should emit StorageError event`() = runTest {
        // Given
        val storageException = StorageException("Insufficient storage space")
        val context = "Downloading episode"
        
        // When
        errorHandler.handleError(storageException, context)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.StorageError)
        assertEquals("Insufficient storage space", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertFalse(errorEvent.isRetryable)
    }
    
    @Test
    fun `handleError with custom message should emit CustomError event`() = runTest {
        // Given
        val message = "Custom error message"
        val context = "Custom operation"
        val isRetryable = true
        
        // When
        errorHandler.handleError(message, context, isRetryable)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.CustomError)
        assertEquals(message, errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertEquals(isRetryable, errorEvent.isRetryable)
    }
    
    @Test
    fun `handleError with unknown exception should emit UnknownError event`() = runTest {
        // Given
        val unknownException = RuntimeException("Unknown error")
        val context = "Unknown operation"
        
        // When
        errorHandler.handleError(unknownException, context)
        
        // Then
        val errorEvent = errorHandler.errorEvents.first()
        assertTrue(errorEvent is ErrorEvent.UnknownError)
        assertEquals("Unknown error", errorEvent.message)
        assertEquals(context, errorEvent.context)
        assertTrue(errorEvent.isRetryable)
    }
}