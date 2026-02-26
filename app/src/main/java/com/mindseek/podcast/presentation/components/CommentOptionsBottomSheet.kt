package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindseek.podcast.domain.model.CommentDomain

/**
 * 评论选项底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentOptionsBottomSheet(
    comment: CommentDomain,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDeleteComment: (CommentDomain) -> Unit = {},
    onReportComment: (CommentDomain) -> Unit = {},
    onShareComment: (CommentDomain) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "评论选项",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Comment preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = comment.userName ?: "匿名用户",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Share option (always available)
                CommentOptionItem(
                    icon = Icons.Default.Share,
                    text = "分享评论",
                    onClick = {
                        onShareComment(comment)
                        onDismiss()
                    }
                )

                // Delete option (only for author)
                if (comment.canDelete) {
                    CommentOptionItem(
                        icon = Icons.Default.Delete,
                        text = "删除评论",
                        onClick = {
                            onDeleteComment(comment)
                            onDismiss()
                        },
                        isDestructive = true
                    )
                }

                // Report option (only for non-authors)
                if (comment.canReport) {
                    CommentOptionItem(
                        icon = Icons.Default.Report,
                        text = "举报评论",
                        onClick = {
                            onReportComment(comment)
                            onDismiss()
                        },
                        isDestructive = true
                    )
                }
            }

            // Bottom spacing for safe area
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 评论选项条目
 */
@Composable
private fun CommentOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}