package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindseek.podcast.domain.model.DownloadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineIndicator(
    isOfflineAvailable: Boolean,
    downloadState: DownloadState = DownloadState.NotDownloaded,
    modifier: Modifier = Modifier
) {
    when {
        isOfflineAvailable -> {
            OfflineAvailableBadge(modifier = modifier)
        }
        downloadState is DownloadState.Downloading -> {
            DownloadingBadge(
                progress = downloadState.progress,
                modifier = modifier
            )
        }
        downloadState is DownloadState.Failed -> {
            DownloadFailedBadge(modifier = modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineAvailableBadge(
    modifier: Modifier = Modifier
) {
    Badge(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DownloadDone,
                contentDescription = "已下载",
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "离线",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadingBadge(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Badge(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "下载",
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadFailedBadge(
    modifier: Modifier = Modifier
) {
    Badge(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "下载失败",
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "失败",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OfflineModeIndicator(
    isOfflineMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (isOfflineMode) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "离线模式",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "离线模式 - 仅显示已下载内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}