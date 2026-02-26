package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 评论输入组件
 */
@Composable
fun CommentInput(
    isUserLoggedIn: Boolean,
    isPosting: Boolean = false,
    replyToComment: String? = null,
    onPostComment: (String) -> Unit,
    onLoginRequired: () -> Unit,
    onCancelReply: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var showLoginDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // 字符计数
    val maxLength = 500
    val remainingChars = maxLength - commentText.length
    val isValidComment = commentText.trim().length >= 2 && commentText.length <= maxLength

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // 回复提示
        if (replyToComment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "回复: $replyToComment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        onCancelReply()
                        commentText = ""
                    }
                ) {
                    Text("取消")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 评论输入框
        OutlinedTextField(
            value = commentText,
            onValueChange = { newText ->
                if (newText.length <= maxLength) {
                    commentText = newText
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = if (replyToComment != null) "写下你的回复..." else "写下你的评论...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            enabled = isUserLoggedIn && !isPosting,
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (isValidComment && isUserLoggedIn && !isPosting) {
                        onPostComment(commentText.trim())
                        commentText = ""
                    }
                }
            ),
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isUserLoggedIn) {
                        Text(
                            text = "请先登录后发表评论",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (commentText.trim().isNotEmpty() && commentText.trim().length < 2) {
                        Text(
                            text = "评论至少需要${commentText.trim().length}个字符",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    
                    Text(
                        text = "$remainingChars",
                        color = if (remainingChars < 50) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 发布按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPosting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "发布中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 发布按钮
                Button(
                    onClick = {
                        if (!isUserLoggedIn) {
                            showLoginDialog = true
                        } else if (isValidComment) {
                            onPostComment(commentText.trim())
                            commentText = ""
                        }
                    },
                    enabled = if (isUserLoggedIn) isValidComment else true,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发布评论",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("发布")
                }
            }
        }
    }

    // 登录提示对话框
    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLoginClick = {
                showLoginDialog = false
                onLoginRequired()
            }
        )
    }

    // 自动聚焦到输入框（回复时）
    LaunchedEffect(replyToComment) {
        if (replyToComment != null && isUserLoggedIn) {
            focusRequester.requestFocus()
        }
    }
}

/**
 * 登录提示对话框
 */
@Composable
private fun LoginRequiredDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "需要登录",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "发表评论需要先登录账号，是否前往登录？",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("去登录")
                    }
                }
            }
        }
    }
}