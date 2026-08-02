package com.mindseek.podcast.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 主题预览组件
 * 用于展示和测试播客应用的主题系统
 */
@Composable
fun ThemePreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "播客应用主题预览",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        item { ColorPalette() }
        item { TypographyShowcase() }
        item { ShapeShowcase() }
        item { ComponentShowcase() }
        item { ExtendedColorsShowcase() }
    }
}

/**
 * 颜色调色板展示
 */
@Composable
private fun ColorPalette() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastShapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "颜色调色板",
                style = MaterialTheme.typography.titleLarge
            )
            
            val colors = listOf(
                "Primary" to MaterialTheme.colorScheme.primary,
                "Secondary" to MaterialTheme.colorScheme.secondary,
                "Tertiary" to MaterialTheme.colorScheme.tertiary,
                "Error" to MaterialTheme.colorScheme.error,
                "Background" to MaterialTheme.colorScheme.background,
                "Surface" to MaterialTheme.colorScheme.surface,
                "Outline" to MaterialTheme.colorScheme.outline
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors) { (name, color) ->
                    ColorSwatch(name = name, color = color)
                }
            }
        }
    }
}

/**
 * 颜色样本
 */
@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 字体样式展示
 */
@Composable
private fun TypographyShowcase() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastShapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "字体样式",
                style = MaterialTheme.typography.titleLarge
            )
            
            val typographyStyles = listOf(
                "Display Large" to MaterialTheme.typography.displayLarge,
                "Headline Large" to MaterialTheme.typography.headlineLarge,
                "Title Large" to MaterialTheme.typography.titleLarge,
                "Body Large" to MaterialTheme.typography.bodyLarge,
                "Label Large" to MaterialTheme.typography.labelLarge
            )
            
            typographyStyles.forEach { (name, style) ->
                TypographySample(name = name, style = style)
            }
        }
    }
}

/**
 * 字体样本
 */
@Composable
private fun TypographySample(name: String, style: TextStyle) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "播客应用示例文本",
            style = style,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 形状展示
 */
@Composable
private fun ShapeShowcase() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastShapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "形状系统",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShapeSample("Small", PodcastShapes.small)
                ShapeSample("Medium", PodcastShapes.medium)
                ShapeSample("Large", PodcastShapes.large)
            }
        }
    }
}

/**
 * 形状样本
 */
@Composable
private fun ShapeSample(name: String, shape: androidx.compose.foundation.shape.CornerBasedShape) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 组件展示
 */
@Composable
private fun ComponentShowcase() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastShapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "组件展示",
                style = MaterialTheme.typography.titleLarge
            )
            
            // 按钮组件
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { }) {
                    Text("播放")
                }
                OutlinedButton(onClick = { }) {
                    Text("暂停")
                }
                TextButton(onClick = { }) {
                    Text("跳过")
                }
            }
            
            // 浮动操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "播放")
                }
                
                ExtendedFloatingActionButton(
                    onClick = { },
                    icon = { Icon(Icons.Default.Download, contentDescription = "下载") },
                    text = { Text("下载节目") }
                )
            }
            
            // 开关和滑块
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var switchState by remember { mutableStateOf(true) }
                Switch(
                    checked = switchState,
                    onCheckedChange = { switchState = it }
                )
                
                var sliderValue by remember { mutableStateOf(0.5f) }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 扩展颜色展示
 */
@Composable
private fun ExtendedColorsShowcase() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastShapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "播客应用专用颜色",
                style = MaterialTheme.typography.titleLarge
            )
            
            val extendedColors = listOf(
                "播放按钮" to MaterialTheme.extendedColors.playButton,
                "暂停按钮" to MaterialTheme.extendedColors.pauseButton,
                "进度条" to MaterialTheme.extendedColors.progressBar,
                "收藏" to MaterialTheme.extendedColors.favorite,
                "下载" to MaterialTheme.extendedColors.download,
                "评论" to MaterialTheme.extendedColors.comment
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(extendedColors) { (name, color) ->
                    ColorSwatch(name = name, color = color)
                }
            }
        }
    }
}

/**
 * 播客应用组件预览
 */
@Composable
fun PodcastComponentPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 模拟播客卡片
        PodcastCardPreview()
        
        // 模拟播放器控件
        PlayerControlsPreview()
    }
    }
}

@Composable
private fun PodcastCardPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastCustomShapes.PodcastCover
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(PodcastCustomShapes.PodcastCover)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "播客标题示例",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "播客作者",
                    style = PodcastTextStyles.PodcastAuthor,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "45:30",
                    style = PodcastTextStyles.EpisodeDuration,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = MaterialTheme.extendedColors.playButton
                )
            }
        }
    }
}

@Composable
private fun PlayerControlsPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PodcastCustomShapes.MiniPlayer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.playButton)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = 0.3f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.extendedColors.progressBar,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "15:30",
                        style = PodcastTextStyles.PlaybackTime,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "45:00",
                        style = PodcastTextStyles.PlaybackTime,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


// 预览组合
@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun ThemePreviewLight() {
    XiaoyuzhouPodcastAppTheme(darkTheme = false) {
        ThemePreview()
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun ThemePreviewDark() {
    XiaoyuzhouPodcastAppTheme(darkTheme = true) {
        ThemePreview()
    }
}

@Preview(name = "Podcast Components Light", showBackground = true)
@Composable
private fun PodcastComponentPreviewLight() {
    XiaoyuzhouPodcastAppTheme(darkTheme = false) {
        PodcastComponentPreview()
    }
}

@Preview(name = "Podcast Components Dark", showBackground = true)
@Composable
private fun PodcastComponentPreviewDark() {
    XiaoyuzhouPodcastAppTheme(darkTheme = true) {
        PodcastComponentPreview()
    }
}