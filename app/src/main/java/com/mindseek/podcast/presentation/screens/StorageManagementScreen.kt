package com.mindseek.podcast.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.usecase.offline.StorageInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: StorageManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadStorageInfo()
        viewModel.loadDownloads()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("存储管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.cleanupOldDownloads() }
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = "清理")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage overview
            item {
                StorageOverviewCard(
                    storageInfo = uiState.storageInfo,
                    isLoading = uiState.isLoadingStorage
                )
            }
            
            // Downloads section
            item {
                Text(
                    text = "已下载内容",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (uiState.isLoadingDownloads) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(uiState.downloads) { download ->
                    DownloadItem(
                        download = download,
                        onDelete = { viewModel.deleteDownload(download.episodeId) }
                    )
                }
                
                if (uiState.downloads.isEmpty()) {
                    item {
                        EmptyDownloadsMessage()
                    }
                }
            }
        }
    }
    
    // Show cleanup result
    uiState.cleanupResult?.let { result ->
        LaunchedEffect(result) {
            // Show snackbar or dialog with cleanup result
        }
    }
}

@Composable
private fun StorageOverviewCard(
    storageInfo: StorageInfo?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "存储",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "存储使用情况",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (storageInfo != null) {
                StorageInfoContent(storageInfo)
            } else {
                Text(
                    text = "无法获取存储信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StorageInfoContent(storageInfo: StorageInfo) {
    val usedSpace = storageInfo.totalSpace - storageInfo.availableSpace
    val usedPercentage = if (storageInfo.totalSpace > 0) {
        (usedSpace.toFloat() / storageInfo.totalSpace.toFloat())
    } else 0f
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Storage progress bar
        LinearProgressIndicator(
            progress = usedPercentage,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Storage details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "已用: ${formatBytes(usedSpace)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "总计: ${formatBytes(storageInfo.totalSpace)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // App usage
        Text(
            text = "应用使用: ${formatBytes(storageInfo.usedByApp)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Downloads usage
        Text(
            text = "下载文件: ${formatBytes(storageInfo.usedByDownloads)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DownloadItem(
    download: DownloadInfo,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "大小: ${formatBytes(download.downloadedBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                download.downloadDate?.let { date ->
                    Text(
                        text = "下载时间: ${formatDate(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyDownloadsMessage() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "暂无下载内容",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "下载一些播客节目以便离线收听",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "%.1f %s".format(size, units[unitIndex])
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}