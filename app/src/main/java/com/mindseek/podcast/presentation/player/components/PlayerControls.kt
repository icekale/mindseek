package com.mindseek.podcast.presentation.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindseek.podcast.domain.model.PlayerState

@Composable
fun PlayerControls(
    playerState: PlayerState,
    isSeekingByUser: Boolean,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onSeekFinished: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 进度条和时间显示
        ProgressSection(
            currentPosition = playerState.currentPosition,
            duration = playerState.duration,
            isSeekingByUser = isSeekingByUser,
            onSeekTo = onSeekTo,
            onSeekStarted = onSeekStarted,
            onSeekFinished = onSeekFinished,
            formatTime = formatTime
        )
        
        Spacer(modifier = Modifier.size(24.dp))
        
        // 播放控制按钮
        PlaybackControlButtons(
            isPlaying = playerState.isPlaying,
            isBuffering = playerState.isBuffering,
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious
        )
    }
}

@Composable
private fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    isSeekingByUser: Boolean,
    onSeekTo: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onSeekFinished: () -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0f) }
    
    // 当用户不在拖拽时，更新滑块位�?
    if (!isSeekingByUser && duration > 0) {
        sliderPosition = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // 进度�?
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue.coerceIn(0f, 1f)
                if (!isSeekingByUser) {
                    onSeekStarted()
                }
            },
            onValueChangeFinished = {
                if (duration > 0) {
                    val newPosition = (sliderPosition * duration).toLong().coerceIn(0L, duration)
                    onSeekTo(newPosition)
                }
                onSeekFinished()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = duration > 0
        )
        
        // 时间显示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isSeekingByUser && duration > 0) {
                    formatTime((sliderPosition * duration).toLong())
                } else {
                    formatTime(currentPosition)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (duration > 0) formatTime(duration) else "--:--",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackControlButtons(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一集按�?
        IconButton(
            onClick = onSkipPrevious,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 播放/暂停按钮
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp),
            enabled = !isBuffering
        ) {
            if (isBuffering) {
                // 这里可以添加一个加载指示器
                Text(
                    text = "...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 下一集按钮
        IconButton(
            onClick = onSkipNext,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}