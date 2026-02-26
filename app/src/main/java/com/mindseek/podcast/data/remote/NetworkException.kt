package com.mindseek.podcast.data.remote

import java.io.IOException

/**
 * Custom exceptions for network operations
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Network connection error (no internet, timeout, etc.)
     */
    class NetworkError(message: String = "Network connection error", cause: Throwable? = null) : NetworkException(message, cause)
    
    /**
     * Server error (5xx status codes)
     */
    class ServerError(val code: Int, message: String = "Server error", cause: Throwable? = null) : NetworkException("Server error ($code): $message", cause)
    
    /**
     * Client error (4xx status codes)
     */
    class ClientError(val code: Int, message: String = "Client error", cause: Throwable? = null) : NetworkException("Client error ($code): $message", cause)
    
    /**
     * Parsing error (JSON parsing failed)
     */
    class ParseError(message: String = "Failed to parse response", cause: Throwable? = null) : NetworkException(message, cause)
    
    /**
     * Unknown error
     */
    class UnknownError(message: String = "Unknown error occurred", cause: Throwable? = null) : NetworkException(message, cause)
    
    /**
     * Check if this error is retryable
     */
    fun isRetryable(): Boolean {
        return when (this) {
            is NetworkError -> true
            is ServerError -> true
            is ClientError -> code == 408 || code == 429 // Request Timeout or Too Many Requests
            is ParseError -> false
            is UnknownError -> true
        }
    }
}

/**
 * Extension function to convert throwables to NetworkException
 */
fun Throwable.toNetworkException(): NetworkException {
    return when (this) {
        is IOException -> NetworkException.NetworkError(cause = this)
        is NetworkException -> this
        else -> NetworkException.UnknownError(cause = this)
    }
}