package com.mindseek.podcast.core.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extension functions for easier error handling in ViewModels and other components
 */

/**
 * Execute a suspending function with error handling
 */
fun ViewModel.executeWithErrorHandling(
    errorHandler: ErrorHandler,
    context: String = "",
    onError: ((Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: Throwable) {
            onError?.invoke(e)
            errorHandler.handleError(e, context)
        }
    }
}

/**
 * Execute a suspending function with retry capability
 */
fun ViewModel.executeWithRetry(
    errorHandler: ErrorHandler,
    retryManager: RetryManager,
    context: String = "",
    maxRetries: Int = 3,
    onError: ((Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch {
        try {
            retryManager.executeWithRetry(
                maxRetries = maxRetries,
                shouldRetry = retryManager::shouldRetryError
            ) {
                block()
            }
        } catch (e: Throwable) {
            onError?.invoke(e)
            errorHandler.handleError(e, context)
        }
    }
}

/**
 * Handle network result with error handling
 */
suspend fun <T> handleNetworkResult(
    result: com.mindseek.podcast.data.remote.NetworkResult<T>,
    errorHandler: ErrorHandler,
    context: String = "",
    onSuccess: (T) -> Unit,
    onError: ((Throwable) -> Unit)? = null
) {
    when (result) {
        is com.mindseek.podcast.data.remote.NetworkResult.Success -> {
            onSuccess(result.data)
        }
        is com.mindseek.podcast.data.remote.NetworkResult.Error -> {
            onError?.invoke(result.exception)
            errorHandler.handleError(result.exception, context)
        }
        is com.mindseek.podcast.data.remote.NetworkResult.Loading -> {
            // Handle loading state if needed
        }
    }
}

/**
 * Extension function for CoroutineScope to handle errors
 */
fun CoroutineScope.launchWithErrorHandling(
    errorHandler: ErrorHandler,
    context: String = "",
    onError: ((Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        try {
            block()
        } catch (e: Throwable) {
            onError?.invoke(e)
            errorHandler.handleError(e, context)
        }
    }
}