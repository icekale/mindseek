package com.mindseek.podcast.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mindseek.podcast.presentation.player.components.PlaybackModeButton
import com.mindseek.podcast.presentation.player.components.PlaybackSpeedButton
import com.mindseek.podcast.presentation.player.components.PlaybackSpeedDialog
import com.mindseek.podcast.presentation.player.components.PlayerControls
import com.mindseek.podcast.presentation.player.components.PlaylistBottomSheet
import com.mindseek.podcast.presentation.player.components.PlaylistButton
import com.mindseek.podcast.presentation.player.components.VolumeButton
import com.mindseek.podcast.presentation.player.components.VolumeControlDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isSeekingByUser by viewModel.isSeekingByUser.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showVolumeDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val playlistSheetState = rememberModalBottomSheetState()
    
    // 自动播放逻辑
    LaunchedEffect(autoPlay) {
        if (autoPlay && playerState.currentEpisode != null && !playerState.isPlaying) {
            viewModel.togglePlayPause()
        }
    }
    
    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("正在播放") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            playerState.currentEpisode?.let { episode ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 播客封面
                    Card(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = "https://via.placeholder.com/280x280", // 这里应该是实际的封面URL
                            contentDescription = "播客封面",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 节目信息
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "播客名称", // 这里应该显示实际的播客名�?
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // 播放控制�?
                    PlayerControls(
                        playerState = playerState,
                        isSeekingByUser = isSeekingByUser,
                        onPlayPause = viewModel::togglePlayPause,
                        onSeekTo = viewModel::seekTo,
                        onSeekStarted = viewModel::onSeekStarted,
                        onSeekFinished = viewModel::onSeekFinished,
                        onSkipNext = viewModel::skipToNext,
                        onSkipPrevious = viewModel::skipToPrevious,
                        formatTime = viewModel::formatTime
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 额外控制按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 播放模式控制
                        PlaybackModeButton(
                            currentMode = playbackMode,
                            onModeChanged = viewModel::setPlaybackMode
                        )
                        
                        // 播放速度控制
                        PlaybackSpeedButton(
                            currentSpeed = uiState.selectedSpeed,
                            onClick = { showSpeedDialog = true }
                        )
                        
                        // 音量控制
                        VolumeButton(
                            volume = playerState.volume,
                            onClick = { showVolumeDialog = true }
                        )
                        
                        // 播放列表
                        PlaylistButton(
                            episodeCount = playlist.size,
                            onClick = viewModel::togglePlaylist
                        )
                    }
                }
            } ?: run {
                // 没有正在播放的节�?
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有正在播放的节目",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // 播放速度选择对话�?
    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = uiState.selectedSpeed,
            onSpeedSelected = { speed ->
                viewModel.setPlaybackSpeed(speed)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }
    
    // 音量控制对话�?
    if (showVolumeDialog) {
        VolumeControlDialog(
            volume = playerState.volume,
            onVolumeChanged = viewModel::setVolume,
            onDismiss = { showVolumeDialog = false }
        )
    }
    
    // 播放列表底部弹窗
    if (uiState.showPlaylist) {
        PlaylistBottomSheet(
            episodes = playlist,
            currentEpisodeId = playerState.currentEpisode?.id,
            onEpisodeSelected = { episode ->
                viewModel.playEpisode(episode)
                viewModel.togglePlaylist()
            },
            onDismiss = viewModel::togglePlaylist,
            sheetState = playlistSheetState
        )
    }
}