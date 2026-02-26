package com.mindseek.podcast.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindseek.podcast.domain.model.CommentDomain

/**
 * 举报评论对话框
 */
@Composable
fun ReportCommentDialog(
    comment: CommentDomain,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val reportReasons = listOf(
        "垃圾信息" to "spam",
        "骚扰或恶意言" to "harassment",
        "仇恨言论" to "hate_speech",
        "虚假信息" to "misinformation",
        "侵犯版权" to "copyright",
        "不当内容" to "inappropriate",
        "其他" to "other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "举报评论",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Comment preview
                Text(
                    text = "举报的评论：",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${comment.content}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reason selection
                Text(
                    text = "举报原因",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    reportReasons.forEach { (displayName, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedReason == value,
                                    onClick = { selectedReason = value },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == value,
                                onClick = null
                            )
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Additional description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("补充说明（可选）") },
                    placeholder = { Text("请详细描述举报原因...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedReason.isNotEmpty()) {
                        onConfirm(selectedReason, description.trim())
                    }
                },
                enabled = selectedReason.isNotEmpty()
            ) {
                Text("提交举报")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}