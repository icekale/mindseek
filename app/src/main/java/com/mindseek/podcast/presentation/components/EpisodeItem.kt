package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindseek.podcast.domain.model.DownloadState
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.presentation.accessibility.AccessibleIconButton
import com.mindseek.podcast.presentation.accessibility.AccessibleListItem
import com.mindseek.podcast.presentation.accessibility.AccessibleProgressIndicator
import com.mindseek.podcast.presentation.accessibility.AccessibleText
import com.mindseek.podcast.presentation.i18n.LocalLocalizationManager
import com.mindseek.podcast.presentation.i18n.Strings
import com.mindseek.podcast.presentation.i18n.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EpisodeItem(
    episode: EpisodeDomain,
    isPlaying: Boolean = false,
    playProgress: Float = 0f,
    downloadState: DownloadState = DownloadState.NotDownloaded,
    isOfflineAvailable: Boolean = false,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizationManager = LocalLocalizationManager.current
    
    // Create accessible content description for the episode
    val episodeDescription = buildString {
        append("节目: ${episode.title}")
        if (episode.podcastTitle.isNotBlank()) {
            append(", 播客: ${episode.podcastTitle}")
        }
        append(", 时长: ${localizationManager.formatDuration(episode.duration)}")
        append(", 发布时间: ${localizationManager.formatDate(episode.publishDate)}")
        if (isPlaying) {
            append(", 正在播放")
        }
        if (episode.isFavorite) {
            append(", 已收藏")
        }
        if (episode.isDownloaded) {
            append(", 已下载")
        }
        if (isOfflineAvailable) {
            append(", 离线可用")
        }
    }
    
    AccessibleListItem(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentDescription = episodeDescription,
        role = Role.Button
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 标题和播放按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        AccessibleText(
                            text = episode.title,
                            style = MaterialTheme.typography.titleMedium,
                            isHeading = true,
                            headingLevel = 3
                        )
                        
                        // Offline indicator
                        if (isOfflineAvailable || downloadState !is DownloadState.NotDownloaded) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OfflineIndicator(
                                isOfflineAvailable = isOfflineAvailable,
                                downloadState = downloadState
                            )
                        }
                    }
                    
                    AccessibleIconButton(
                        onClick = onPlayClick,
                        contentDescription = if (isPlaying) {
                            stringResource(Strings.CD_PAUSE_BUTTON)
                        } else {
                            stringResource(Strings.CD_PLAY_BUTTON)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null, // Handled by AccessibleIconButton
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 播客标题（如果有）
                if (episode.podcastTitle.isNotBlank()) {
                    AccessibleText(
                        text = episode.podcastTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        role = Role.Button
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // 描述
                if (episode.description.isNotBlank()) {
                    AccessibleText(
                        text = episode.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 播放进度条（如果有播放进度）
                if (playProgress > 0f) {
                    AccessibleProgressIndicator(
                        progress = playProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        contentDescription = stringResource(Strings.A11Y_PROGRESS),
                        progressDescription = { "${(it * 100).toInt()}%" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 底部信息和操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 时长和发布日期
                    Column {
                        AccessibleText(
                            text = localizationManager.formatDuration(episode.duration),
                            style = MaterialTheme.typography.bodySmall
                        )
                        AccessibleText(
                            text = localizationManager.formatDate(episode.publishDate),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // 操作按钮
                    Row {
                        // 收藏按钮
                        AccessibleIconButton(
                            onClick = onFavoriteClick,
                            contentDescription = if (episode.isFavorite) {
                                stringResource(Strings.ACTION_UNFAVORITE)
                            } else {
                                stringResource(Strings.ACTION_FAVORITE)
                            }
                        ) {
                            Icon(
                                imageVector = if (episode.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null, // Handled by AccessibleIconButton
                                tint = if (episode.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 下载按钮
                        AccessibleIconButton(
                            onClick = onDownloadClick,
                            contentDescription = if (episode.isDownloaded) {
                                "已下载"
                            } else {
                                stringResource(Strings.ACTION_DOWNLOAD)
                            }
                        ) {
                            Icon(
                                imageVector = if (episode.isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = null, // Handled by AccessibleIconButton
                                tint = if (episode.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactEpisodeItem(
    episode: EpisodeDomain,
    isPlaying: Boolean = false,
    playProgress: Float = 0f,
    onPlayClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 播放按钮
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // 节目信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatDuration(episode.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDate(episode.publishDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 状态指示器
                Row {
                    if (episode.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "已收藏",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    if (episode.isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "已下载",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // 播放进度条
            if (playProgress > 0f) {
                LinearProgressIndicator(
                    progress = playProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}