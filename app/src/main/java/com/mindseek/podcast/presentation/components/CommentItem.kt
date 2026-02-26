package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mindseek.podcast.domain.model.CommentDomain
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 评论条目组件
 */
@Composable
fun CommentItem(
    comment: CommentDomain,
    onLikeClick: (CommentDomain) -> Unit = {},
    onReplyClick: (CommentDomain) -> Unit = {},
    onMoreClick: (CommentDomain) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showReplies: Boolean = true,
    isReply: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isReply) 2.dp else 4.dp
        ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReply) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Comment header with user info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserClick(comment.userId) }
                ) {
                    // User avatar
                    if (comment.userAvatar != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(comment.userAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Default avatar
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comment.userName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = comment.userName ?: "匿名用户",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatTimestamp(comment.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // More options button
                IconButton(
                    onClick = { onMoreClick(comment) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多选项",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick(comment) }
                ) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (comment.isLiked) "取消点赞" else "点赞",
                        tint = if (comment.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    if (comment.likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = comment.likeCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (comment.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Reply button (only for top-level comments)
                if (!isReply) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onReplyClick(comment) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = "回复",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        if (comment.replies.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = comment.replies.size.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Replies section
            if (showReplies && comment.replies.isNotEmpty() && !isReply) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comment.replies.forEach { reply ->
                        CommentItem(
                            comment = reply,
                            onLikeClick = onLikeClick,
                            onReplyClick = onReplyClick,
                            onMoreClick = onMoreClick,
                            onUserClick = onUserClick,
                            showReplies = false,
                            isReply = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * 格式化时间戳为可读格�?
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "刚刚" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}分钟前" // Less than 1 hour
        diff < 86400_000 -> "${diff / 3600_000}小时前" // Less than 1 day
        diff < 604800_000 -> "${diff / 86400_000}天前" // Less than 1 week
        else -> {
            val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}