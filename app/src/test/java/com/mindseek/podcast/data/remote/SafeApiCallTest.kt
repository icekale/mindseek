package com.mindseek.podcast.data.remote

import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafeApiCallTest {

    @Test
    fun `safeApiCall returns success when API call succeeds`() = runTest {
        // Given
        val expectedData = "Success"

        // When
        val result = safeApiCall {
            expectedData
        }

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedData, result.data)
    }

    @Test
    fun `safeApiCall returns error for client error without retry`() = runTest {
        // Given
        val httpException = HttpException(
            Response.error<String>(404, okhttp3.ResponseBody.create(null, "Not Found"))
        )

        // When
        val result = safeApiCall {
            throw httpException
        }

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.ClientError)
        assertEquals(404, (result.exception as NetworkException.ClientError).code)
    }

    @Test
    fun `safeApiCall retries on server error and returns error after max retries`() = runTest {
        // Given
        val httpException = HttpException(
            Response.error<String>(500, okhttp3.ResponseBody.create(null, "Internal Server Error"))
        )
        var callCount = 0

        // When
        val result = safeApiCall(maxRetries = 3, initialDelayMillis = 1) {
            callCount++
            throw httpException
        }

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.ServerError)
        assertEquals(500, (result.exception as NetworkException.ServerError).code)
        assertEquals(3, callCount) // Should have retried 3 times
    }

    @Test
    fun `safeApiCall retries on network error`() = runTest {
        // Given
        val networkException = UnknownHostException("No internet connection")
        var callCount = 0

        // When
        val result = safeApiCall(maxRetries = 2, initialDelayMillis = 1) {
            callCount++
            throw networkException
        }

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.NetworkError)
        assertEquals(2, callCount) // Should have retried 2 times
    }

    @Test
    fun `safeApiCall succeeds on retry after initial failure`() = runTest {
        // Given
        val expectedData = "Success"
        var callCount = 0

        // When
        val result = safeApiCall(maxRetries = 3, initialDelayMillis = 1) {
            callCount++
            if (callCount < 2) {
                throw IOException("Network error")
            }
            expectedData
        }

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedData, result.data)
        assertEquals(2, callCount) // Should have succeeded on second attempt
    }

    @Test
    fun `safeApiCall retries on timeout error`() = runTest {
        // Given
        val timeoutException = SocketTimeoutException("Request timeout")
        var callCount = 0

        // When
        val result = safeApiCall(maxRetries = 2, initialDelayMillis = 1) {
            callCount++
            throw timeoutException
        }

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.NetworkError)
        assertEquals("Request timeout", result.exception.message)
        assertEquals(2, callCount)
    }

    @Test
    fun `safeApiCall retries on 429 Too Many Requests`() = runTest {
        // Given
        val httpException = HttpException(
            Response.error<String>(429, okhttp3.ResponseBody.create(null, "Too Many Requests"))
        )
        var callCount = 0

        // When
        val result = safeApiCall(maxRetries = 2, initialDelayMillis = 1) {
            callCount++
            throw httpException
        }

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.ClientError)
        assertEquals(429, (result.exception as NetworkException.ClientError).code)
        assertEquals(2, callCount) // Should have retried even though it's a 4xx error
    }

    @Test
    fun `safeApiCall retries on 408 Request Timeout`() = runTest {
        // Given
        val httpException = HttpException(
            Response.error<String>(408, okhttp3.ResponseBody.create(null, "Request Timeout"))
        )
        var callCount = 0

        // When
        val result = safeApiCall(maxRetries = 2, initialDelayMillis = 1) {
            callCount++
            throw httpException
        }

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.NetworkError)
        assertEquals(2, callCount) // Should have retried even though it's a 4xx error
    }

    @Test
    fun `Response toNetworkResult returns success for successful response`() = runTest {
        // Given
        val data = "Success"
        val response = Response.success(data)

        // When
        val result = response.toNetworkResult()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(data, result.data)
    }

    @Test
    fun `Response toNetworkResult returns error for unsuccessful response`() = runTest {
        // Given
        val response = Response.error<String>(
            404,
            okhttp3.ResponseBody.create(null, "Not Found")
        )

        // When
        val result = response.toNetworkResult()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.ClientError)
        assertEquals(404, (result.exception as NetworkException.ClientError).code)
    }

    @Test
    fun `NetworkException isRetryable returns correct values`() {
        // Network errors should be retryable
        assertTrue(NetworkException.NetworkError().isRetryable())
        
        // Server errors should be retryable
        assertTrue(NetworkException.ServerError(500).isRetryable())
        
        // Parse errors should not be retryable
        assertTrue(!NetworkException.ParseError().isRetryable())
        
        // Unknown errors should be retryable
        assertTrue(NetworkException.UnknownError().isRetryable())
        
        // Most client errors should not be retryable
        assertTrue(!NetworkException.ClientError(404).isRetryable())
        
        // But 408 and 429 should be retryable
        assertTrue(NetworkException.ClientError(408).isRetryable())
        assertTrue(NetworkException.ClientError(429).isRetryable())
    }
}