package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindseek.podcast.domain.model.CommentDomain

/**
 * 评论区域组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSection(
    comments: List<CommentDomain>,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    isPosting: Boolean = false,
    isDeleting: Boolean = false,
    isReporting: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    commentCount: Int = 0,
    isUserLoggedIn: Boolean = false,
    replyToComment: CommentDomain? = null,
    showCommentOptions: CommentDomain? = null,
    showDeleteDialog: CommentDomain? = null,
    showReportDialog: CommentDomain? = null,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onLikeComment: (CommentDomain) -> Unit = {},
    onReplyToComment: (CommentDomain) -> Unit = {},
    onMoreOptions: (CommentDomain) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onPostComment: (String) -> Unit = {},
    onLoginRequired: () -> Unit = {},
    onCancelReply: () -> Unit = {},
    onHideCommentOptions: () -> Unit = {},
    onDeleteComment: (CommentDomain) -> Unit = {},
    onHideDeleteDialog: () -> Unit = {},
    onShowReportDialog: (CommentDomain) -> Unit = {},
    onReportComment: (CommentDomain, String, String) -> Unit = { _, _, _ -> },
    onHideReportDialog: () -> Unit = {},
    onShareComment: (CommentDomain) -> Unit = {},
    onClearSuccessMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState()
    
    // Show success/error messages
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSuccessMessage()
        }
    }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    // Detect when user scrolls to bottom for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            
            lastVisibleItemIndex > (totalItemsNumber - 3) && !isLoading
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && comments.isNotEmpty()) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Comment section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "评论",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "评论",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (commentCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($commentCount)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Refresh button
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新评论",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Comment list
        when {
            isLoading && comments.isEmpty() -> {
                // Initial loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "加载评论�?..",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            errorMessage != null && comments.isEmpty() -> {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "加载评论失败",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(onClick = onRefresh) {
                            Text("重试")
                        }
                    }
                }
            }
            
            comments.isEmpty() -> {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "暂无评论",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "成为第一个评论的人吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            else -> {
                // Comment list
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = comments,
                        key = { comment -> comment.id }
                    ) { comment ->
                        CommentItem(
                            comment = comment,
                            onLikeClick = onLikeComment,
                            onReplyClick = onReplyToComment,
                            onMoreClick = onMoreOptions,
                            onUserClick = onUserClick
                        )
                    }
                    
                    // Loading more indicator
                    if (isLoading && comments.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "加载更多评论...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Comment input section
        CommentInput(
            isUserLoggedIn = isUserLoggedIn,
            isPosting = isPosting,
            replyToComment = replyToComment?.let { "${it.userName}: ${it.content.take(30)}${if (it.content.length > 30) "..." else ""}" },
            onPostComment = onPostComment,
            onLoginRequired = onLoginRequired,
            onCancelReply = onCancelReply
        )
    }

    // Comment options bottom sheet
    showCommentOptions?.let { comment ->
        CommentOptionsBottomSheet(
            comment = comment,
            sheetState = bottomSheetState,
            onDismiss = onHideCommentOptions,
            onDeleteComment = { onDeleteComment(it) },
            onReportComment = { onShowReportDialog(it) },
            onShareComment = onShareComment
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { comment ->
        DeleteCommentDialog(
            comment = comment,
            onDismiss = onHideDeleteDialog,
            onConfirm = { onDeleteComment(comment) }
        )
    }

    // Report comment dialog
    showReportDialog?.let { comment ->
        ReportCommentDialog(
            comment = comment,
            onDismiss = onHideReportDialog,
            onConfirm = { reason, description ->
                onReportComment(comment, reason, description)
            }
        )
    }

    // Snackbar host for messages
    SnackbarHost(hostState = snackbarHostState)
}