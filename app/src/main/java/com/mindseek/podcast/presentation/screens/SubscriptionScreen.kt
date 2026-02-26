package com.mindseek.podcast.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.presentation.components.LoadingState
import com.mindseek.podcast.presentation.components.PodcastCard

/**
 * 订阅屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateToPodcastDetail: (String) -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("我的订阅") },
            actions = {
                IconButton(onClick = { viewModel.refreshSubscriptions() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新订阅"
                    )
                }
            }
        )
        
        // Error handling
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("关闭")
                    }
                }
            }
        }
        
        // Content
        when {
            uiState.isLoading && uiState.subscribedPodcasts.isEmpty() -> {
                LoadingState(
                    message = "加载订阅列表�?..",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            uiState.subscribedPodcasts.isEmpty() && !uiState.isLoading -> {
                EmptySubscriptionState(
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            else -> {
                SubscriptionList(
                    podcasts = uiState.subscribedPodcasts,
                    hasNewUpdates = uiState.hasNewUpdates,
                    onPodcastClick = onNavigateToPodcastDetail,
                    onUnsubscribe = viewModel::unsubscribeFromPodcast,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun EmptySubscriptionState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "还没有订阅任何播客",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "去发现页面找找感兴趣的播客吧",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubscriptionList(
    podcasts: List<com.mindseek.podcast.data.local.entity.Podcast>,
    hasNewUpdates: Map<String, Boolean>,
    onPodcastClick: (String) -> Unit,
    onUnsubscribe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = podcasts,
            key = { it.id }
        ) { podcast ->
            SubscriptionPodcastCard(
                podcast = podcast,
                hasNewUpdate = hasNewUpdates[podcast.id] ?: false,
                onClick = { onPodcastClick(podcast.id) },
                onUnsubscribe = { onUnsubscribe(podcast.id) }
            )
        }
    }
}

@Composable
private fun SubscriptionPodcastCard(
    podcast: com.mindseek.podcast.data.local.entity.Podcast,
    hasNewUpdate: Boolean,
    onClick: () -> Unit,
    onUnsubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUnsubscribeDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use PodcastCard for consistent UI
            Column(
                modifier = Modifier.weight(1f)
            ) {
                PodcastCard(
                    podcast = podcast.toDomain(),
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // New update badge and unsubscribe button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasNewUpdate) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    Badge {
                        Text("新")
                    }
                }
                
                TextButton(
                    onClick = { showUnsubscribeDialog = true }
                ) {
                    Text("取消订阅")
                }
            }
        }
    }
    
    // Unsubscribe confirmation dialog
    if (showUnsubscribeDialog) {
        AlertDialog(
            onDismissRequest = { showUnsubscribeDialog = false },
            title = { Text("取消订阅") },
            text = { Text("确定要取消订阅「${podcast.title}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUnsubscribe()
                        showUnsubscribeDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnsubscribeDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}