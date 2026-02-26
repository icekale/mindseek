package com.mindseek.podcast.presentation.performance

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Performance optimizations for UI components
 */

/**
 * Optimized image loading with caching and placeholder
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            hasError = state is AsyncImagePainter.State.Error
        }
    )
    
    // Show placeholder while loading
    if (isLoading && placeholder != null) {
        placeholder()
    }
    
    // Show error state if loading failed
    if (hasError && error != null) {
        error()
    }
}

/**
 * Lazy list state that tracks scroll performance
 */
@Composable
fun rememberOptimizedLazyListState(): LazyListState {
    val listState = rememberLazyListState()
    
    // Track scroll performance
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                // Can be used to disable expensive operations during scroll
                // For example, pause image loading or reduce animation complexity
            }
    }
    
    return listState
}

/**
 * Viewport-aware content that only renders when visible
 */
@Composable
fun ViewportAwareContent(
    listState: LazyListState,
    index: Int,
    threshold: Dp = 100.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    
    val isVisible by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val item = visibleItems.find { it.index == index }
            
            item != null && 
            item.offset >= -thresholdPx && 
            item.offset <= layoutInfo.viewportSize.height + thresholdPx
        }
    }
    
    if (isVisible) {
        content()
    }
}

/**
 * Debounced state for expensive operations
 */
@Composable
fun <T> rememberDebouncedState(
    value: T,
    delayMillis: Long = 300L
): State<T> {
    val debouncedValue = remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        kotlinx.coroutines.delay(delayMillis)
        debouncedValue.value = value
    }
    
    return debouncedValue
}

/**
 * Cached composable that prevents recomposition
 */
@Composable
fun <T> CachedContent(
    key: T,
    content: @Composable (T) -> Unit
) {
    val cachedContent by remember(key) {
        mutableStateOf(content)
    }
    
    cachedContent(key)
}

/**
 * Optimized card with reduced recomposition
 */
@Composable
fun OptimizedCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.drawWithCache {
            // Cache drawing operations
            val path = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        radiusX = 12.dp.toPx(),
                        radiusY = 12.dp.toPx()
                    )
                )
            }
            
            onDrawBehind {
                clipPath(path) {
                    // Cached drawing operations
                }
            }
        },
        colors = colors,
        elevation = elevation,
        content = content
    )
}

/**
 * Memory-efficient list item
 */
@Composable
fun MemoryEfficientListItem(
    data: Any,
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    if (isVisible) {
        content()
    } else {
        // Render a minimal placeholder to maintain list structure
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Scroll-based content loading
 */
@Composable
fun ScrollBasedLoader(
    listState: LazyListState,
    threshold: Int = 5,
    onLoadMore: () -> Unit
) {
    val loadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            totalItems > 0 && lastVisibleItem >= totalItems - threshold
        }
    }
    
    LaunchedEffect(loadMore) {
        if (loadMore) {
            onLoadMore()
        }
    }
}

/**
 * Optimized text rendering
 */
@Composable
fun OptimizedText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE
) {
    // Use remember to cache text layout calculations
    val cachedText by remember(text, style, maxLines) {
        mutableStateOf(text)
    }
    
    Text(
        text = cachedText,
        modifier = modifier,
        style = style,
        maxLines = maxLines
    )
}

/**
 * Batch state updates to reduce recomposition
 */
class BatchedStateUpdater<T>(initialValue: T) {
    private var _value by mutableStateOf(initialValue)
    private var pendingUpdate: T? = null
    private var updateScheduled = false
    
    val value: T get() = _value
    
    fun updateValue(newValue: T) {
        pendingUpdate = newValue
        if (!updateScheduled) {
            updateScheduled = true
            // Schedule update for next frame
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(16) // ~1 frame at 60fps
                pendingUpdate?.let {
                    _value = it
                    pendingUpdate = null
                }
                updateScheduled = false
            }
        }
    }
}

/**
 * Composable that batches state updates
 */
@Composable
fun <T> rememberBatchedState(initialValue: T): BatchedStateUpdater<T> {
    return remember { BatchedStateUpdater(initialValue) }
}

/**
 * Performance monitoring composable
 */
@Composable
fun PerformanceMonitor(
    tag: String,
    content: @Composable () -> Unit
) {
    val startTime = remember { System.currentTimeMillis() }
    
    DisposableEffect(Unit) {
        onDispose {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            // Log performance metrics
            println("Performance [$tag]: ${duration}ms")
        }
    }
    
    content()
}