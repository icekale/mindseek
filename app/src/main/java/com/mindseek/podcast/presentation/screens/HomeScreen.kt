package com.mindseek.podcast.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindseek.podcast.presentation.components.LoadingState
import com.mindseek.podcast.presentation.components.PodcastCard

/**
 * 首页屏幕 - 显示推荐播客列表
 */
@Composable
fun HomeScreen(
    onNavigateToPodcastDetail: (String) -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Handle pagination - load more when reaching near the end
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            totalItemsNumber > 0 && lastVisibleItemIndex > (totalItemsNumber - 3)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoading && !uiState.isRefreshing && uiState.errorMessage == null) {
            viewModel.loadMorePodcasts()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.recommendedPodcasts.isEmpty() -> {
                LoadingState(
                    message = "正在加载推荐播客...",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            uiState.errorMessage != null && uiState.recommendedPodcasts.isEmpty() -> {
                // Simple error state
                val errorMessage = uiState.errorMessage ?: "未知错误"
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加载失败",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = { 
                            viewModel.clearError()
                            viewModel.loadRecommendedPodcasts()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("重试")
                    }
                }
            }
            
            uiState.recommendedPodcasts.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无推荐播客",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "点击刷新试试看",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.refresh() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("刷新")
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with refresh button
                    item {
                        Column {
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "推荐播客",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Text(
                                        text = "为你精选的优质播客内容",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (!uiState.isRefreshing) {
                                    androidx.compose.material3.IconButton(
                                        onClick = { viewModel.refresh() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "刷新"
                                        )
                                    }
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(12.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        }
                    }

                    // Podcast list
                    items(
                        items = uiState.recommendedPodcasts,
                        key = { podcast -> podcast.id }
                    ) { podcast ->
                        PodcastCard(
                            podcast = podcast,
                            onClick = { onNavigateToPodcastDetail(podcast.id) }
                        )
                    }

                    // Loading more indicator
                    if (uiState.isLoading && uiState.recommendedPodcasts.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // Error state for pagination
                    if (uiState.errorMessage != null && uiState.recommendedPodcasts.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "加载更多失败",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { 
                                        viewModel.clearError()
                                        viewModel.loadMorePodcasts()
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}