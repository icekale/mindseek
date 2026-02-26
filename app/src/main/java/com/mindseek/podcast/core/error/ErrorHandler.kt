package com.mindseek.podcast.core.error

import com.mindseek.podcast.data.remote.NetworkException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global error handler that manages application-wide error handling
 */
@Singleton
class ErrorHandler @Inject constructor() {
    
    private val _errorEvents = MutableSharedFlow<ErrorEvent>()
    val errorEvents: SharedFlow<ErrorEvent> = _errorEvents.asSharedFlow()
    
    /**
     * Handle an error and emit appropriate error event
     */
    suspend fun handleError(
        throwable: Throwable,
        context: String = "",
        isRetryable: Boolean = true
    ) {
        val errorEvent = when (throwable) {
            is NetworkException -> {
                when (throwable) {
                    is NetworkException.NetworkError -> ErrorEvent.NetworkError(
                        message = throwable.message ?: "网络连接失败",
                        context = context,
                        isRetryable = throwable.isRetryable()
                    )
                    is NetworkException.ServerError -> ErrorEvent.ServerError(
                        message = "服务器错误 (${throwable.code})",
                        context = context,
                        isRetryable = throwable.isRetryable()
                    )
                    is NetworkException.ClientError -> ErrorEvent.ClientError(
                        message = when (throwable.code) {
                            401 -> "请先登录"
                            403 -> "没有权限访问"
                            404 -> "内容不存在"
                            429 -> "请求过于频繁，请稍后再试"
                            else -> "请求错误 (${throwable.code})"
                        },
                        context = context,
                        isRetryable = throwable.isRetryable()
                    )
                    is NetworkException.ParseError -> ErrorEvent.DataError(
                        message = "数据解析失败",
                        context = context,
                        isRetryable = false
                    )
                    is NetworkException.UnknownError -> ErrorEvent.UnknownError(
                        message = "未知错误",
                        context = context,
                        isRetryable = true
                    )
                }
            }
            is AudioPlaybackException -> ErrorEvent.AudioError(
                message = throwable.message ?: "音频播放失败",
                context = context,
                isRetryable = true
            )
            is StorageException -> ErrorEvent.StorageError(
                message = throwable.message ?: "存储空间不足",
                context = context,
                isRetryable = false
            )
            else -> ErrorEvent.UnknownError(
                message = throwable.message ?: "发生未知错误",
                context = context,
                isRetryable = isRetryable
            )
        }
        
        _errorEvents.emit(errorEvent)
    }
    
    /**
     * Handle error with custom message
     */
    suspend fun handleError(
        message: String,
        context: String = "",
        isRetryable: Boolean = false
    ) {
        _errorEvents.emit(
            ErrorEvent.CustomError(
                message = message,
                context = context,
                isRetryable = isRetryable
            )
        )
    }
}

/**
 * Sealed class representing different types of error events
 */
sealed class ErrorEvent(
    open val message: String,
    open val context: String,
    open val isRetryable: Boolean
) {
    data class NetworkError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class ServerError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class ClientError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class AudioError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class StorageError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class DataError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class UnknownError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
    
    data class CustomError(
        override val message: String,
        override val context: String,
        override val isRetryable: Boolean
    ) : ErrorEvent(message, context, isRetryable)
}

/**
 * Custom exceptions for different error types
 */
class AudioPlaybackException(message: String, cause: Throwable? = null) : Exception(message, cause)
class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause)