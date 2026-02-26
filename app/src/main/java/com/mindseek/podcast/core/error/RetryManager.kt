package com.mindseek.podcast.core.error

import com.mindseek.podcast.data.remote.NetworkException
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Manages retry logic for failed operations
 */
@Singleton
class RetryManager @Inject constructor() {
    
    /**
     * Execute an operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMillis: Long = 1000,
        maxDelayMillis: Long = 10000,
        backoffMultiplier: Double = 2.0,
        shouldRetry: (Throwable) -> Boolean = { true },
        operation: suspend () -> T
    ): T {
        var lastException: Throwable? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Throwable) {
                lastException = e
                
                // Check if we should retry this error
                if (!shouldRetry(e)) {
                    throw e
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
        
        // All retries failed, throw the last exception
        throw lastException ?: RuntimeException("All retries failed")
    }
    
    /**
     * Check if an error should be retried based on its type
     */
    fun shouldRetryError(throwable: Throwable): Boolean {
        return when (throwable) {
            is NetworkException -> throwable.isRetryable()
            is AudioPlaybackException -> true
            is StorageException -> false
            else -> false
        }
    }
}