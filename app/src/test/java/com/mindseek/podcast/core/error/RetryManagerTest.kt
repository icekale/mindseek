package com.mindseek.podcast.core.error

import com.mindseek.podcast.data.remote.NetworkException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RetryManagerTest {
    
    private lateinit var retryManager: RetryManager
    
    @Before
    fun setUp() {
        retryManager = RetryManager()
    }
    
    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        // Given
        var attemptCount = 0
        val expectedResult = "Success"
        
        // When
        val result = retryManager.executeWithRetry {
            attemptCount++
            expectedResult
        }
        
        // Then
        assertEquals(expectedResult, result)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should retry on retryable error and eventually succeed`() = runTest {
        // Given
        var attemptCount = 0
        val expectedResult = "Success"
        
        // When
        val result = retryManager.executeWithRetry(
            maxRetries = 3,
            initialDelayMillis = 10, // Short delay for testing
            shouldRetry = { true }
        ) {
            attemptCount++
            if (attemptCount < 3) {
                throw NetworkException.NetworkError("Network error")
            }
            expectedResult
        }
        
        // Then
        assertEquals(expectedResult, result)
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should not retry on non-retryable error`() = runTest {
        // Given
        var attemptCount = 0
        val nonRetryableError = NetworkException.ClientError(400, "Bad request")
        
        // When & Then
        try {
            retryManager.executeWithRetry(
                maxRetries = 3,
                shouldRetry = { false }
            ) {
                attemptCount++
                throw nonRetryableError
            }
            fail("Expected exception to be thrown")
        } catch (e: NetworkException.ClientError) {
            assertEquals(nonRetryableError, e)
            assertEquals(1, attemptCount)
        }
    }
    
    @Test
    fun `executeWithRetry should throw last exception after all retries fail`() = runTest {
        // Given
        var attemptCount = 0
        val persistentError = NetworkException.NetworkError("Persistent error")
        
        // When & Then
        try {
            retryManager.executeWithRetry(
                maxRetries = 3,
                initialDelayMillis = 10,
                shouldRetry = { true }
            ) {
                attemptCount++
                throw persistentError
            }
            fail("Expected exception to be thrown")
        } catch (e: NetworkException.NetworkError) {
            assertEquals(persistentError, e)
            assertEquals(3, attemptCount)
        }
    }
    
    @Test
    fun `shouldRetryError should return true for retryable NetworkExceptions`() {
        // Given
        val networkError = NetworkException.NetworkError()
        val serverError = NetworkException.ServerError(500)
        val timeoutError = NetworkException.ClientError(408, "Timeout")
        val rateLimitError = NetworkException.ClientError(429, "Too many requests")
        
        // When & Then
        assertTrue(retryManager.shouldRetryError(networkError))
        assertTrue(retryManager.shouldRetryError(serverError))
        assertTrue(retryManager.shouldRetryError(timeoutError))
        assertTrue(retryManager.shouldRetryError(rateLimitError))
    }
    
    @Test
    fun `shouldRetryError should return false for non-retryable errors`() {
        // Given
        val clientError = NetworkException.ClientError(400, "Bad request")
        val parseError = NetworkException.ParseError()
        val storageError = StorageException("Storage full")
        
        // When & Then
        assertFalse(retryManager.shouldRetryError(clientError))
        assertFalse(retryManager.shouldRetryError(parseError))
        assertFalse(retryManager.shouldRetryError(storageError))
    }
    
    @Test
    fun `shouldRetryError should return true for AudioPlaybackException`() {
        // Given
        val audioError = AudioPlaybackException("Playback failed")
        
        // When & Then
        assertTrue(retryManager.shouldRetryError(audioError))
    }
    
    @Test
    fun `shouldRetryError should return false for unknown exceptions`() {
        // Given
        val unknownError = RuntimeException("Unknown error")
        
        // When & Then
        assertFalse(retryManager.shouldRetryError(unknownError))
    }
}