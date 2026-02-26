package com.mindseek.podcast.domain.model

/**
 * Sealed class representing different types of application errors
 */
sealed class AppError(open val message: String) {
    data class NetworkError(override val message: String = "网络连接失败") : AppError(message)
    data class ServerError(override val message: String = "服务器错误") : AppError(message)
    data class DatabaseError(override val message: String = "数据库错误") : AppError(message)
    data class AudioPlaybackError(override val message: String = "音频播放失败") : AppError(message)
    data class DownloadError(override val message: String = "下载失败") : AppError(message)
    data class StorageError(override val message: String = "存储空间不足") : AppError(message)
    data class UnknownError(override val message: String = "未知错误") : AppError(message)
}