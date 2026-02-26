package com.mindseek.podcast.data.remote

import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.pow

/**
 * Safe API call wrapper that handles errors and retries
 */
suspend fun <T> safeApiCall(
    maxRetries: Int = 3,
    initialDelayMillis: Long = 1000,
    maxDelayMillis: Long = 10000,
    backoffMultiplier: Double = 2.0,
    apiCall: suspend () -> T
): NetworkResult<T> {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            val result = apiCall()
            return NetworkResult.Success(result)
        } catch (e: Exception) {
            lastException = e
            
            // Don't retry for client errors (4xx) except for 408 (Request Timeout) and 429 (Too Many Requests)
            if (e is HttpException) {
                when (e.code()) {
                    in 400..499 -> {
                        if (e.code() != 408 && e.code() != 429) {
                            return NetworkResult.Error(
                                NetworkException.ClientError(e.code(), e.message()),
                                "Client error: ${e.message()}"
                            )
                        }
                    }
                }
            }
            
            // If this is the last attempt, don't delay
            if (attempt < maxRetries - 1) {
                val delayTime = minOf(
                    initialDelayMillis * backoffMultiplier.pow(attempt).toLong(),
                    maxDelayMillis
                )
                delay(delayTime)
            }
        }
    }
    
    // All retries failed, return error
    val networkException = when (val exception = lastException) {
        is HttpException -> {
            when (exception.code()) {
                in 500..599 -> NetworkException.ServerError(exception.code(), exception.message())
                408 -> NetworkException.NetworkError("Request timeout")
                429 -> NetworkException.ClientError(exception.code(), "Too many requests")
                else -> NetworkException.ClientError(exception.code(), exception.message())
            }
        }
        is UnknownHostException -> NetworkException.NetworkError("No internet connection")
        is SocketTimeoutException -> NetworkException.NetworkError("Request timeout")
        is IOException -> NetworkException.NetworkError("Network error", exception)
        else -> NetworkException.UnknownError("Unknown error", exception)
    }
    
    return NetworkResult.Error(networkException, networkException.message)
}

/**
 * Safe API call specifically for Retrofit Response objects
 */
suspend fun <T> safeApiCallWithResponse(
    maxRetries: Int = 3,
    initialDelayMillis: Long = 1000,
    maxDelayMillis: Long = 10000,
    backoffMultiplier: Double = 2.0,
    apiCall: suspend () -> Response<T>
): NetworkResult<T> {
    return safeApiCall(maxRetries, initialDelayMillis, maxDelayMillis, backoffMultiplier) {
        val response = apiCall()
        response.toNetworkResult().let { result ->
            when (result) {
                is NetworkResult.Success -> result.data
                is NetworkResult.Error -> throw result.exception
                is NetworkResult.Loading -> throw IllegalStateException("Unexpected loading state")
            }
        }
    }
}

/**
 * Extension function for Response objects
 */
fun <T> Response<T>.toNetworkResult(): NetworkResult<T> {
    return try {
        if (isSuccessful) {
            body()?.let { NetworkResult.Success(it) }
                ?: NetworkResult.Error(NetworkException.ParseError("Response body is null"))
        } else {
            val errorMessage = errorBody()?.string() ?: "HTTP ${code()}"
            val exception = when (code()) {
                in 400..499 -> NetworkException.ClientError(code(), errorMessage)
                in 500..599 -> NetworkException.ServerError(code(), errorMessage)
                else -> NetworkException.UnknownError(errorMessage)
            }
            NetworkResult.Error(exception, errorMessage)
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.toNetworkException(), e.message)
    }
}

