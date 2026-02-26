package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindseek.podcast.core.error.ErrorEvent

/**
 * Dialog for displaying error messages with retry option
 */
@Composable
fun ErrorDialog(
    errorEvent: ErrorEvent,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = getErrorIcon(errorEvent),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = getErrorTitle(errorEvent),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = errorEvent.message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            if (errorEvent.isRetryable && onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("重试")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * Snackbar for displaying error messages
 */
@Composable
fun ErrorSnackbar(
    errorEvent: ErrorEvent,
    snackbarHostState: SnackbarHostState,
    onRetry: (() -> Unit)? = null
) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { snackbarData ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    if (errorEvent.isRetryable && onRetry != null) {
                        TextButton(onClick = onRetry) {
                            Text("重试")
                        }
                    }
                },
                dismissAction = {
                    IconButton(onClick = { snackbarHostState.currentSnackbarData?.dismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "关闭"
                        )
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getErrorIcon(errorEvent),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = errorEvent.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

/**
 * Full screen error state with retry option
 */
@Composable
fun ErrorScreen(
    errorEvent: ErrorEvent,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = getErrorIcon(errorEvent),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = getErrorTitle(errorEvent),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorEvent.message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (errorEvent.isRetryable && onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

/**
 * Get appropriate icon for error type
 */
private fun getErrorIcon(errorEvent: ErrorEvent): ImageVector {
    return when (errorEvent) {
        is ErrorEvent.NetworkError -> Icons.Default.WifiOff
        is ErrorEvent.ServerError -> Icons.Default.Error
        is ErrorEvent.ClientError -> Icons.Default.Warning
        is ErrorEvent.AudioError -> Icons.Default.Error
        is ErrorEvent.StorageError -> Icons.Default.Warning
        is ErrorEvent.DataError -> Icons.Default.Error
        is ErrorEvent.UnknownError -> Icons.Default.Error
        is ErrorEvent.CustomError -> Icons.Default.Error
    }
}

/**
 * Get appropriate title for error type
 */
private fun getErrorTitle(errorEvent: ErrorEvent): String {
    return when (errorEvent) {
        is ErrorEvent.NetworkError -> "网络连接失败"
        is ErrorEvent.ServerError -> "服务器错误"
        is ErrorEvent.ClientError -> "请求错误"
        is ErrorEvent.AudioError -> "播放失败"
        is ErrorEvent.StorageError -> "存储错误"
        is ErrorEvent.DataError -> "数据错误"
        is ErrorEvent.UnknownError -> "未知错误"
        is ErrorEvent.CustomError -> "错误"
    }
}