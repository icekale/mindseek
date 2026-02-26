package com.mindseek.podcast.presentation.player.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 播放模式枚举
 */
enum class PlaybackMode(
    val displayName: String,
    val icon: ImageVector,
    val description: String
) {
    SEQUENTIAL("顺序播放", Icons.Default.Repeat, "顺序播放"),
    REPEAT_ONE("单曲循环", Icons.Default.RepeatOne, "单曲循环"),
    SHUFFLE("随机播放", Icons.Default.Shuffle, "随机播放")
}

/**
 * 播放模式切换按钮
 */
@Composable
fun PlaybackModeButton(
    currentMode: PlaybackMode,
    onModeChanged: (PlaybackMode) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            val nextMode = when (currentMode) {
                PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ONE
                PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
                PlaybackMode.SHUFFLE -> PlaybackMode.SEQUENTIAL
            }
            onModeChanged(nextMode)
        },
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = currentMode.icon,
            contentDescription = currentMode.description,
            tint = when (currentMode) {
                PlaybackMode.SEQUENTIAL -> MaterialTheme.colorScheme.onSurfaceVariant
                PlaybackMode.REPEAT_ONE -> MaterialTheme.colorScheme.primary
                PlaybackMode.SHUFFLE -> MaterialTheme.colorScheme.secondary
            },
            modifier = Modifier.size(20.dp)
        )
    }
}