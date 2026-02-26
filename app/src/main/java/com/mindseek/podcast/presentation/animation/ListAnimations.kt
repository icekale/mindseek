package com.mindseek.podcast.presentation.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animated LazyColumn with item animations
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AnimatedLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    itemKey: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(
            items = items,
            key = itemKey
        ) { item ->
            Box(
                modifier = Modifier.animateItemPlacement(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {
                itemContent(item)
            }
        }
    }
}

/**
 * Staggered list animation
 */
@Composable
fun <T> StaggeredAnimatedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    staggerDelayMs: Long = 50L,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(index) {
                delay(index * staggerDelayMs)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(300, easing = EaseOutCubic)
                )
            ) {
                itemContent(item, index)
            }
        }
    }
}

/**
 * Parallax scroll effect for list items
 */
@Composable
fun LazyListState.calculateParallaxOffset(
    index: Int,
    parallaxRatio: Float = 0.5f
): Float {
    val density = LocalDensity.current
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    
    val item = visibleItems.find { it.index == index }
    return if (item != null) {
        val itemCenter = item.offset + item.size / 2f
        val listCenter = layoutInfo.viewportSize.height / 2f
        val distanceFromCenter = itemCenter - listCenter
        with(density) { distanceFromCenter * parallaxRatio }
    } else {
        0f
    }
}

/**
 * Scroll-based fade animation
 */
@Composable
fun LazyListState.calculateScrollFade(
    index: Int,
    fadeDistance: Float = 200f
): Float {
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    
    val item = visibleItems.find { it.index == index }
    return if (item != null) {
        val itemTop = item.offset.toFloat()
        val itemBottom = itemTop + item.size
        val viewportHeight = layoutInfo.viewportSize.height.toFloat()
        
        when {
            itemTop > viewportHeight -> 0f
            itemBottom < 0 -> 0f
            itemTop < 0 -> {
                val visibleHeight = itemBottom
                (visibleHeight / fadeDistance).coerceIn(0f, 1f)
            }
            itemBottom > viewportHeight -> {
                val visibleHeight = viewportHeight - itemTop
                (visibleHeight / fadeDistance).coerceIn(0f, 1f)
            }
            else -> 1f
        }
    } else {
        0f
    }
}

/**
 * Scale animation based on scroll position
 */
@Composable
fun LazyListState.calculateScrollScale(
    index: Int,
    maxScale: Float = 1.1f,
    minScale: Float = 0.9f
): Float {
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    
    val item = visibleItems.find { it.index == index }
    return if (item != null) {
        val itemCenter = item.offset + item.size / 2f
        val listCenter = layoutInfo.viewportSize.height / 2f
        val distanceFromCenter = kotlin.math.abs(itemCenter - listCenter)
        val maxDistance = layoutInfo.viewportSize.height / 2f
        
        val normalizedDistance = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
        maxScale - (normalizedDistance * (maxScale - minScale))
    } else {
        minScale
    }
}

/**
 * Animated list item with scroll effects
 */
@Composable
fun AnimatedListItem(
    index: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    enableParallax: Boolean = false,
    enableFade: Boolean = false,
    enableScale: Boolean = false,
    content: @Composable () -> Unit
) {
    val parallaxOffset = if (enableParallax) {
        listState.calculateParallaxOffset(index)
    } else 0f
    
    val fadeAlpha = if (enableFade) {
        listState.calculateScrollFade(index)
    } else 1f
    
    val scale = if (enableScale) {
        listState.calculateScrollScale(index)
    } else 1f
    
    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = parallaxOffset
                alpha = fadeAlpha
                scaleX = scale
                scaleY = scale
            }
    ) {
        content()
    }
}

/**
 * Pull to refresh animation
 */
@Composable
fun PullToRefreshAnimation(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var refreshTrigger by remember { mutableStateOf(false) }
    
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            onRefresh()
            refreshTrigger = false
        }
    }
    
    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = isRefreshing,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Pull to refresh indicator content
            }
        }
        
        content()
    }
}

/**
 * Swipe to dismiss animation
 */
@Composable
fun SwipeToDismissItem(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = animatedOffsetX
                alpha = 1f - kotlin.math.abs(animatedOffsetX) / 1000f
            }
    ) {
        content()
    }
}

/**
 * Loading more items animation
 */
@Composable
fun LoadMoreIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it / 2 }
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { it / 2 }
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Load more indicator content
        }
    }
}