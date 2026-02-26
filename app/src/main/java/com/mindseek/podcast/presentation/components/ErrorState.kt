package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindseek.podcast.domain.model.AppError
import com.mindseek.podcast.core.error.ErrorEvent

/**
 * Error state component for displaying errors with retry functionality
 */
@Composable
fun ErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val errorInfo = when (error) {
        is AppError.NetworkError -> ErrorInfo(
            icon = Icons.Default.WifiOff,
            title = "网络连接失败",
            description = "请检查网络连接后重试",
            showRetry = true
        )
        is AppError.ServerError -> ErrorInfo(
            icon = Icons.Default.CloudOff,
            title = "服务器错误",
            description = "服务器暂时无法响应，请稍后重试",
            showRetry = true
        )
        is AppError.DatabaseError -> ErrorInfo(
            icon = Icons.Default.Storage,
            title = "数据错误",
            description = "数据加载失败，请重试",
            showRetry = true
        )
        is AppError.AudioPlaybackError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "播放失败",
            description = "音频播放出现问题，请重试",
            showRetry = true
        )
        is AppError.DownloadError -> ErrorInfo(
            icon = Icons.Default.Download,
            title = "下载失败",
            description = "文件下载失败，请检查网络后重试",
            showRetry = true
        )
        is AppError.StorageError -> ErrorInfo(
            icon = Icons.Default.Storage,
            title = "存储空间不足",
            description = "请清理存储空间后重试",
            showRetry = false
        )
        is AppError.UnknownError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "未知错误",
            description = error.message,
            showRetry = true
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = errorInfo.icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = errorInfo.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorInfo.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (errorInfo.showRetry) {
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("重试")
                }
                
                if (onDismiss != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
            } else if (onDismiss != null) {
                Button(onClick = onDismiss) {
                    Text("确定")
                }
            }
        }
    }
}

/**
 * Overloaded version that works with ErrorEvent
 */
@Composable
fun ErrorState(
    errorEvent: ErrorEvent,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val errorInfo = when (errorEvent) {
        is ErrorEvent.NetworkError -> ErrorInfo(
            icon = Icons.Default.WifiOff,
            title = "网络连接失败",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.ServerError -> ErrorInfo(
            icon = Icons.Default.CloudOff,
            title = "服务器错误",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.ClientError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "请求错误",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.AudioError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "播放失败",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.StorageError -> ErrorInfo(
            icon = Icons.Default.Storage,
            title = "存储错误",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.DataError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "数据错误",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.UnknownError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "未知错误",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
        is ErrorEvent.CustomError -> ErrorInfo(
            icon = Icons.Default.Error,
            title = "错误",
            description = errorEvent.message,
            showRetry = errorEvent.isRetryable
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = errorInfo.icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = errorInfo.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorInfo.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (errorInfo.showRetry && onRetry != null) {
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("重试")
                }
                
                if (onDismiss != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
            } else if (onDismiss != null) {
                Button(onClick = onDismiss) {
                    Text("确定")
                }
            }
        }
    }
}

/**
 * Data class for error information
 */
private data class ErrorInfo(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val showRetry: Boolean
)