package com.mindseek.podcast.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.presentation.components.EpisodeItem
import com.mindseek.podcast.presentation.components.LoadingState
import com.mindseek.podcast.presentation.components.CommentSection

/**
 * 播客详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailScreen(
    podcastId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    viewModel: PodcastDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(podcastId) {
        viewModel.loadPodcastDetail(podcastId)
    }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.podcast?.title ?: "播客详情",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshPodcast() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.podcast == null -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    message = "加载播客详情�?.."
                )
            }
            uiState.podcast != null -> {
                PodcastDetailContent(
                    podcast = uiState.podcast!!,
                    episodes = uiState.episodes,
                    isSubscribing = uiState.isSubscribing,
                    onSubscribeClick = { viewModel.toggleSubscription() },
                    onEpisodeClick = { episode ->
                        // TODO: Navigate to episode detail or start playing
                        onNavigateToPlayer()
                    },
                    onEpisodePlayClick = { episode ->
                        // TODO: Start playing episode
                        onNavigateToPlayer()
                    },
                    onEpisodeFavoriteClick = { episode ->
                        // TODO: Toggle episode favorite
                    },
                    onEpisodeDownloadClick = { episode ->
                        // TODO: Download episode
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            else -> {
                ErrorContent(
                    onRetry = { viewModel.loadPodcastDetail(podcastId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun PodcastDetailContent(
    podcast: PodcastDomain,
    episodes: List<EpisodeDomain>,
    isSubscribing: Boolean,
    onSubscribeClick: () -> Unit,
    onEpisodeClick: (EpisodeDomain) -> Unit,
    onEpisodePlayClick: (EpisodeDomain) -> Unit,
    onEpisodeFavoriteClick: (EpisodeDomain) -> Unit,
    onEpisodeDownloadClick: (EpisodeDomain) -> Unit,
    modifier: Modifier = Modifier,
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val commentUiState by commentViewModel.uiState.collectAsState()
    
    // Load comments for the first episode if available
    LaunchedEffect(episodes) {
        if (episodes.isNotEmpty()) {
            commentViewModel.loadComments(episodes.first().id)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Podcast header
        item {
            PodcastHeader(
                podcast = podcast,
                isSubscribing = isSubscribing,
                onSubscribeClick = onSubscribeClick
            )
        }

        // Episodes section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "节目列表",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${episodes.size} 集",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Episodes list
        if (episodes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无节目",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(episodes) { episode ->
                EpisodeItem(
                    episode = episode,
                    isPlaying = false, // TODO: Get from player state
                    playProgress = 0f, // TODO: Get from play history
                    onPlayClick = { onEpisodePlayClick(episode) },
                    onFavoriteClick = { onEpisodeFavoriteClick(episode) },
                    onDownloadClick = { onEpisodeDownloadClick(episode) },
                    onClick = { onEpisodeClick(episode) }
                )
            }
        }

        // Comment section - only show if there are episodes
        if (episodes.isNotEmpty()) {
            item {
                CommentSection(
                    comments = commentUiState.comments,
                    isLoading = commentUiState.isLoading,
                    isRefreshing = commentUiState.isRefreshing,
                    isPosting = commentUiState.isPosting,
                    isDeleting = commentUiState.isDeleting,
                    isReporting = commentUiState.isReporting,
                    errorMessage = commentUiState.errorMessage,
                    successMessage = commentUiState.successMessage,
                    commentCount = commentUiState.commentCount,
                    isUserLoggedIn = commentUiState.isUserLoggedIn,
                    replyToComment = commentUiState.replyToComment,
                    showCommentOptions = commentUiState.showCommentOptions,
                    showDeleteDialog = commentUiState.showDeleteDialog,
                    showReportDialog = commentUiState.showReportDialog,
                    onRefresh = { commentViewModel.refreshComments() },
                    onLoadMore = { commentViewModel.loadMoreComments() },
                    onLikeComment = { comment -> commentViewModel.likeComment(comment) },
                    onReplyToComment = { comment -> commentViewModel.handleReplyToComment(comment) },
                    onMoreOptions = { comment -> commentViewModel.handleMoreOptions(comment) },
                    onUserClick = { userId -> commentViewModel.handleUserClick(userId) },
                    onPostComment = { content -> commentViewModel.postComment(content) },
                    onLoginRequired = { commentViewModel.handleLoginRequired() },
                    onCancelReply = { commentViewModel.cancelReply() },
                    onHideCommentOptions = { commentViewModel.hideCommentOptions() },
                    onDeleteComment = { comment -> commentViewModel.showDeleteDialog(comment) },
                    onHideDeleteDialog = { commentViewModel.hideDeleteDialog() },
                    onShowReportDialog = { comment -> commentViewModel.showReportDialog(comment) },
                    onReportComment = { comment, reason, description -> 
                        commentViewModel.reportComment(comment, reason, description) 
                    },
                    onHideReportDialog = { commentViewModel.hideReportDialog() },
                    onShareComment = { comment -> commentViewModel.shareComment(comment) },
                    onClearSuccessMessage = { commentViewModel.clearSuccessMessage() }
                )
            }
        }
    }
}

@Composable
private fun PodcastHeader(
    podcast: PodcastDomain,
    isSubscribing: Boolean,
    onSubscribeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Podcast cover
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(podcast.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = podcast.title,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                // Podcast info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = podcast.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = podcast.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = podcast.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Subscribe button
                    Button(
                        onClick = onSubscribeClick,
                        enabled = !isSubscribing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSubscribing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (podcast.isSubscribed) "取消订阅" else "订阅"
                        )
                    }
                }
            }

            // Description
            if (podcast.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = podcast.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "加载失败",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "请检查网络连接后重试",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
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