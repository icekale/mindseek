package com.mindseek.podcast.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.presentation.components.LoadingState
import java.text.SimpleDateFormat
import java.util.*

/**
 * 收藏屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onNavigateToEpisode: (String) -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.isSelectionMode) 
                            "已选择 ${uiState.selectedEpisodes.size} 项" 
                        else 
                            "我的收藏"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isSelectionMode) {
                            viewModel.clearSelection()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = if (uiState.isSelectionMode) "取消选择" else "返回"
                        )
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.selectAllEpisodes() }) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "全选"
                            )
                        }
                        IconButton(onClick = { viewModel.removeSelectedFromFavorites() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除选中"
                            )
                        }
                    } else {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多选项"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("批量管理") },
                                onClick = {
                                    showMenu = false
                                    if (uiState.favorites.isNotEmpty()) {
                                        viewModel.toggleEpisodeSelection(uiState.favorites.first().id)
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar (only show when not in selection mode)
            if (!uiState.isSelectionMode) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::searchFavorites,
                    onClearQuery = viewModel::clearSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            
            // Error handling
            uiState.errorMessage?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Content
            when {
                uiState.isLoading && uiState.favorites.isEmpty() -> {
                    LoadingState(
                        message = "加载收藏列表�?..",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.filteredFavorites.isEmpty() && !uiState.isLoading -> {
                    EmptyFavoritesState(
                        isSearching = uiState.isSearching,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    FavoritesList(
                        favorites = uiState.filteredFavorites,
                        selectedEpisodes = uiState.selectedEpisodes,
                        isSelectionMode = uiState.isSelectionMode,
                        onEpisodeClick = { episodeId ->
                            if (uiState.isSelectionMode) {
                                viewModel.toggleEpisodeSelection(episodeId)
                            } else {
                                onNavigateToEpisode(episodeId)
                            }
                        },
                        onEpisodeLongClick = { episodeId ->
                            if (!uiState.isSelectionMode) {
                                viewModel.toggleEpisodeSelection(episodeId)
                            }
                        },
                        onRemoveFromFavorites = viewModel::removeFromFavorites,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索收藏的节目...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除搜索"
                    )
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun EmptyFavoritesState(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearching) "没有找到相关收藏" else "还没有收藏任何节目",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearching) "尝试使用其他关键词搜索" else "收藏喜欢的节目，它们会在这里显示",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FavoritesList(
    favorites: List<EpisodeDomain>,
    selectedEpisodes: Set<String>,
    isSelectionMode: Boolean,
    onEpisodeClick: (String) -> Unit,
    onEpisodeLongClick: (String) -> Unit,
    onRemoveFromFavorites: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = favorites,
            key = { it.id }
        ) { episode ->
            FavoriteEpisodeItem(
                episode = episode,
                isSelected = selectedEpisodes.contains(episode.id),
                isSelectionMode = isSelectionMode,
                onClick = { onEpisodeClick(episode.id) },
                onLongClick = { onEpisodeLongClick(episode.id) },
                onRemoveFromFavorites = { onRemoveFromFavorites(episode.id) }
            )
        }
    }
}

@Composable
private fun FavoriteEpisodeItem(
    episode: EpisodeDomain,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveFromFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected,
                onValueChange = { onClick() }
            )
            .clickable { 
                if (isSelectionMode) {
                    onClick()
                } else {
                    onClick()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            if (isSelectionMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                    contentDescription = if (isSelected) "已选择" else "未选择",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            // Episode content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = episode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Episode info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDuration(episode.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formatDate(episode.publishDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Remove button (only show when not in selection mode)
            if (!isSelectionMode) {
                IconButton(onClick = { showRemoveDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "取消收藏",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Remove confirmation dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("取消收藏") },
            text = { Text("确定要取消收藏「${episode.title}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFromFavorites()
                        showRemoveDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / 60000
    val hours = minutes / 60
    return if (hours > 0) {
        "${hours}小时${minutes % 60}分钟"
    } else {
        "${minutes}分钟"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}