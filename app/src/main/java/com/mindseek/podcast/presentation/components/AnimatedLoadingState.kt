package com.mindseek.podcast.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Enhanced loading indicator with pulse animation
 */
@Composable
fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "加载�?..",
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            }
            
            AnimatedVisibility(
                visible = message.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dots loading animation
 */
@Composable
fun DotsLoadingIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val delay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * Shimmer loading effect for skeleton screens
 */
@Composable
fun ShimmerLoadingBox(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = shimmerTranslateAnim, y = shimmerTranslateAnim)
    )

    Box(
        modifier = modifier.then(
            if (isLoading) {
                Modifier.background(brush, RoundedCornerShape(4.dp))
            } else {
                Modifier.background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            }
        )
    )
}

/**
 * Enhanced podcast card skeleton with shimmer effect
 */
@Composable
fun ShimmerPodcastCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 封面占位�?
                ShimmerLoadingBox(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                // 信息占位�?
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 标题占位�?
                    ShimmerLoadingBox(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(20.dp)
                    )
                    
                    // 作者占位符
                    ShimmerLoadingBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                    )
                    
                    // 分类占位�?
                    ShimmerLoadingBox(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 描述占位�?
            repeat(2) {
                ShimmerLoadingBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

/**
 * Wave loading animation
 */
@Composable
fun WaveLoadingIndicator(
    modifier: Modifier = Modifier,
    waveCount: Int = 5,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(waveCount) { index ->
            val delay = index * 100
            val height by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 24f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, delayMillis = delay, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_height_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * Loading state with fade in/out animation
 */
@Composable
fun FadeLoadingState(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String = "加载�?..",
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(300)
            )
        ) {
            content()
        }
        
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            PulsingLoadingIndicator(
                modifier = Modifier.fillMaxSize(),
                message = message
            )
        }
    }
}

/**
 * Staggered loading animation for lists
 */
@Composable
fun StaggeredLoadingList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier,
    itemContent: @Composable (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) { index ->
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(index) {
                delay(index * 100L) // Stagger the animation
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(300)
                )
            ) {
                itemContent(index)
            }
        }
    }
}

/**
 * Pull to refresh loading indicator
 */
@Composable
fun PullToRefreshIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isRefreshing,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DotsLoadingIndicator()
        }
    }
}