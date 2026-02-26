package com.mindseek.podcast.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 播客应用深色主题配色方案
 */
private val PodcastDarkColorScheme = darkColorScheme(
    primary = PodcastPrimaryDark,
    onPrimary = PodcastOnPrimaryDark,
    primaryContainer = PodcastPrimaryContainerDark,
    onPrimaryContainer = PodcastOnPrimaryContainerDark,
    secondary = PodcastSecondaryDark,
    onSecondary = PodcastOnSecondaryDark,
    secondaryContainer = PodcastSecondaryContainerDark,
    onSecondaryContainer = PodcastOnSecondaryContainerDark,
    tertiary = PodcastTertiaryDark,
    onTertiary = PodcastOnTertiaryDark,
    tertiaryContainer = PodcastTertiaryContainerDark,
    onTertiaryContainer = PodcastOnTertiaryContainerDark,
    error = PodcastErrorDark,
    onError = PodcastOnErrorDark,
    errorContainer = PodcastErrorContainerDark,
    onErrorContainer = PodcastOnErrorContainerDark,
    background = PodcastBackgroundDark,
    onBackground = PodcastOnBackgroundDark,
    surface = PodcastSurfaceDark,
    onSurface = PodcastOnSurfaceDark,
    surfaceVariant = PodcastSurfaceVariantDark,
    onSurfaceVariant = PodcastOnSurfaceVariantDark,
    outline = PodcastOutlineDark,
    outlineVariant = PodcastOutlineVariantDark,
    scrim = PodcastScrimDark
)

/**
 * 播客应用浅色主题配色方案
 */
private val PodcastLightColorScheme = lightColorScheme(
    primary = PodcastPrimary,
    onPrimary = PodcastOnPrimary,
    primaryContainer = PodcastPrimaryContainer,
    onPrimaryContainer = PodcastOnPrimaryContainer,
    secondary = PodcastSecondary,
    onSecondary = PodcastOnSecondary,
    secondaryContainer = PodcastSecondaryContainer,
    onSecondaryContainer = PodcastOnSecondaryContainer,
    tertiary = PodcastTertiary,
    onTertiary = PodcastOnTertiary,
    tertiaryContainer = PodcastTertiaryContainer,
    onTertiaryContainer = PodcastOnTertiaryContainer,
    error = PodcastError,
    onError = PodcastOnError,
    errorContainer = PodcastErrorContainer,
    onErrorContainer = PodcastOnErrorContainer,
    background = PodcastBackground,
    onBackground = PodcastOnBackground,
    surface = PodcastSurface,
    onSurface = PodcastOnSurface,
    surfaceVariant = PodcastSurfaceVariant,
    onSurfaceVariant = PodcastOnSurfaceVariant,
    outline = PodcastOutline,
    outlineVariant = PodcastOutlineVariant,
    scrim = PodcastScrim
)

/**
 * 播客应用主题
 * 
 * @param darkTheme 是否使用深色主题，默认跟随系统设置
 * @param dynamicColor 是否使用动态颜色（Android 12+），默认启用
 * @param content 应用内容
 */
@Composable
fun XiaoyuzhouPodcastAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 动态颜色支持（Android 12+�?
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 深色主题
        darkTheme -> PodcastDarkColorScheme
        // 浅色主题
        else -> PodcastLightColorScheme
    }
    
    // 选择对应的扩展颜�?
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 设置状态栏颜色为背景色，提供更好的沉浸式体�?
            window.statusBarColor = colorScheme.background.toArgb()
            // 根据主题调整状态栏图标颜色
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 提供扩展颜色和Material主题
    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PodcastTypography,
            shapes = PodcastShapes,
            content = content
        )
    }
}