package com.mindseek.podcast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 播客应用扩展颜色
 * 提供应用特定的颜色定义，不在Material Design 3标准颜色中的颜色
 */
data class PodcastExtendedColors(
    val playButton: Color,
    val pauseButton: Color,
    val progressBar: Color,
    val favorite: Color,
    val download: Color,
    val comment: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val playbackTimeBackground: Color,
    val episodeCardBackground: Color,
    val miniPlayerBackground: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color
)

/**
 * 浅色主题扩展颜色
 */
val LightExtendedColors = PodcastExtendedColors(
    playButton = PlayButtonColor,
    pauseButton = PauseButtonColor,
    progressBar = ProgressBarColor,
    favorite = FavoriteColor,
    download = DownloadColor,
    comment = CommentColor,
    success = SuccessColor,
    warning = WarningColor,
    info = InfoColor,
    playbackTimeBackground = Color(0xFFF5F5F5),
    episodeCardBackground = Color(0xFFFFFFFF),
    miniPlayerBackground = Color(0xFFFAFAFA),
    shimmerBase = Color(0xFFF1F1F1),
    shimmerHighlight = Color(0xFFFFFFFF)
)

/**
 * 深色主题扩展颜色
 */
val DarkExtendedColors = PodcastExtendedColors(
    playButton = PlayButtonColor,
    pauseButton = PauseButtonColor,
    progressBar = ProgressBarColor,
    favorite = FavoriteColor,
    download = DownloadColor,
    comment = CommentColor,
    success = SuccessColor,
    warning = WarningColor,
    info = InfoColor,
    playbackTimeBackground = Color(0xFF2A2A2A),
    episodeCardBackground = Color(0xFF1E1E1E),
    miniPlayerBackground = Color(0xFF252525),
    shimmerBase = Color(0xFF2A2A2A),
    shimmerHighlight = Color(0xFF3A3A3A)
)

/**
 * CompositionLocal for extended colors
 */
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * Material Theme扩展属性
 * 提供访问扩展颜色的便捷方法
 */
val MaterialTheme.extendedColors: PodcastExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

/**
 * 颜色工具函数
 */
object ColorUtils {
    /**
     * 根据当前主题返回对应的扩展颜色
     */
    @Composable
    fun getExtendedColors(): PodcastExtendedColors {
        return if (isSystemInDarkTheme()) DarkExtendedColors else LightExtendedColors
    }
    
    /**
     * 为颜色添加透明度
     */
    fun Color.withAlpha(alpha: Float): Color {
        return this.copy(alpha = alpha)
    }
    
    /**
     * 获取播放状态对应的颜色
     */
    @Composable
    fun getPlaybackStateColor(isPlaying: Boolean): Color {
        return if (isPlaying) {
            MaterialTheme.extendedColors.pauseButton
        } else {
            MaterialTheme.extendedColors.playButton
        }
    }
    
    /**
     * 获取下载状态对应的颜色
     */
    @Composable
    fun getDownloadStateColor(isDownloaded: Boolean): Color {
        return if (isDownloaded) {
            MaterialTheme.extendedColors.success
        } else {
            MaterialTheme.extendedColors.download
        }
    }
    
    /**
     * 获取收藏状态对应的颜色
     */
    @Composable
    fun getFavoriteStateColor(isFavorited: Boolean): Color {
        return if (isFavorited) {
            MaterialTheme.extendedColors.favorite
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
}

/**
 * 主题工具函数
 */
object ThemeUtilsExtensions {
    /**
     * 检查当前是否为深色主题
     */
    @Composable
    fun isDarkTheme(): Boolean = isSystemInDarkTheme()
    
    /**
     * 获取适合当前主题的表面颜色变体
     */
    @Composable
    fun getSurfaceColorVariant(elevation: Int): Color {
        return when (elevation) {
            0 -> MaterialTheme.colorScheme.surface
            1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
            2 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
            3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.11f)
            4 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
            5 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    }
    
    /**
     * 获取内容颜色，基于背景颜色自动选择
     */
    @Composable
    fun getContentColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    }
}

/**
 * 颜色扩展函数
 */
fun Color.luminance(): Float {
    val red = red * 0.299f
    val green = green * 0.587f
    val blue = blue * 0.114f
    return red + green + blue
}