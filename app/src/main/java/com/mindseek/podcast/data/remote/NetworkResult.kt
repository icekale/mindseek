package com.mindseek.podcast.data.remote

/**
 * A generic wrapper class for network responses that handles success, error, and loading states
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Exception, val message: String? = null) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

/**
 * Extension function to handle network results
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (exception: Exception) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(exception)
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action()
    return this
}