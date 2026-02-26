package com.mindseek.podcast.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindseek.podcast.data.local.entity.SearchHistory
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.presentation.components.EpisodeItem
import com.mindseek.podcast.presentation.components.LoadingState
import com.mindseek.podcast.presentation.components.PodcastCard

/**
 * 搜索屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToPodcastDetail: (String) -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Show error message if any
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You can show a snackbar here if needed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search input field
        SearchInputField(
            query = uiState.query,
            onQueryChange = viewModel::updateSearchQuery,
            focusRequester = focusRequester,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search type tabs
        SearchTypeTabs(
            selectedType = uiState.searchType,
            onTypeSelected = viewModel::selectSearchType,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on search state
        when {
            uiState.query.isBlank() -> {
                // Show search history when no query
                SearchHistorySection(
                    searchHistory = uiState.searchHistory,
                    onHistoryItemClick = { history ->
                        viewModel.selectHistoryItem(history)
                        focusManager.clearFocus()
                    },
                    onDeleteHistoryItem = viewModel::deleteHistoryItem,
                    onClearAllHistory = viewModel::clearAllHistory
                )
            }
            uiState.isLoading -> {
                LoadingState(
                    message = "搜索框..",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            uiState.errorMessage != null -> {
                val errorMessage = uiState.errorMessage ?: "未知错误"
                SimpleErrorState(
                    message = errorMessage,
                    onRetry = { 
                        viewModel.clearError()
                        if (uiState.query.isNotBlank()) {
                            viewModel.updateSearchQuery(uiState.query)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            uiState.searchResults.totalResults == 0 -> {
                NoResultsState(
                    query = uiState.query,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                SearchResultsSection(
                    searchResults = uiState.searchResults,
                    searchType = uiState.searchType,
                    onPodcastClick = onNavigateToPodcastDetail,
                    onEpisodeClick = { onNavigateToPlayer() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索播客、节目...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除"
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier.focusRequester(focusRequester)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTypeTabs(
    selectedType: SearchType,
    onTypeSelected: (SearchType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(SearchType.values()) { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = when (type) {
                            SearchType.PODCASTS -> "播客"
                            SearchType.EPISODES -> "节目"
                            SearchType.ALL -> "全部"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun SearchHistorySection(
    searchHistory: List<SearchHistory>,
    onHistoryItemClick: (SearchHistory) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "搜索历史",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAllHistory) {
                    Text("清除全部")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(searchHistory) { history ->
                    SearchHistoryItem(
                        searchHistory = history,
                        onClick = { onHistoryItemClick(history) },
                        onDelete = { onDeleteHistoryItem(history.query) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "搜索播客和节目",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "输入关键词开始搜索",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHistoryItem(
    searchHistory: SearchHistory,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = searchHistory.query,
                style = MaterialTheme.typography.bodyMedium
            )
            if (searchHistory.resultCount > 0) {
                Text(
                    text = "${searchHistory.resultCount} 个结果",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "删除",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NoResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "未找到相关结果",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "尝试使用其他关键词搜索\"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchResults: SearchResults,
    searchType: SearchType,
    onPodcastClick: (String) -> Unit,
    onEpisodeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Results summary
        item {
            Text(
                text = "找到 ${searchResults.totalResults} 个结果",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Podcast results
        if (searchResults.podcasts.isNotEmpty() && 
            (searchType == SearchType.PODCASTS || searchType == SearchType.ALL)) {
            
            if (searchType == SearchType.ALL) {
                item {
                    Text(
                        text = "播客 (${searchResults.podcasts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            items(searchResults.podcasts) { podcast ->
                PodcastCard(
                    podcast = podcast,
                    onClick = { onPodcastClick(podcast.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Episode results
        if (searchResults.episodes.isNotEmpty() && 
            (searchType == SearchType.EPISODES || searchType == SearchType.ALL)) {
            
            if (searchType == SearchType.ALL && searchResults.podcasts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            if (searchType == SearchType.ALL) {
                item {
                    Text(
                        text = "节目 (${searchResults.episodes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            items(searchResults.episodes) { episode ->
                EpisodeItem(
                    episode = episode,
                    onPlayClick = { onEpisodeClick(episode.id) },
                    onFavoriteClick = { /* TODO: Implement favorite functionality */ },
                    onDownloadClick = { /* TODO: Implement download functionality */ },
                    onClick = { onEpisodeClick(episode.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SimpleErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "搜索出错",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}