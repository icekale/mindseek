package com.mindseek.podcast.presentation.i18n

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

/**
 * Localization manager for handling multiple languages
 */
class LocalizationManager(private val context: Context) {
    
    private val _currentLocale = mutableStateOf(getCurrentSystemLocale())
    val currentLocale: State<Locale> = _currentLocale
    
    private val supportedLocales = listOf(
        Locale.CHINESE,
        Locale.SIMPLIFIED_CHINESE,
        Locale.TRADITIONAL_CHINESE,
        Locale.ENGLISH,
        Locale.US,
        Locale.UK
    )
    
    /**
     * Get current system locale
     */
    private fun getCurrentSystemLocale(): Locale {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
    
    /**
     * Set application locale
     */
    fun setLocale(locale: Locale) {
        _currentLocale.value = locale
        updateContextLocale(locale)
    }
    
    /**
     * Update context locale
     */
    private fun updateContextLocale(locale: Locale) {
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    /**
     * Check if locale is supported
     */
    fun isLocaleSupported(locale: Locale): Boolean {
        return supportedLocales.any { 
            it.language == locale.language && 
            (it.country.isEmpty() || it.country == locale.country)
        }
    }
    
    /**
     * Get supported locales
     */
    fun getSupportedLocales(): List<Locale> = supportedLocales
    
    /**
     * Get locale display name
     */
    fun getLocaleDisplayName(locale: Locale): String {
        return locale.getDisplayName(locale)
    }
    
    /**
     * Format time duration based on locale
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return when (_currentLocale.value.language) {
            "zh" -> {
                when {
                    hours > 0 -> "${hours}小时${minutes}分钟"
                    minutes > 0 -> "${minutes}分钟${seconds}秒"
                    else -> "${seconds}秒"
                }
            }
            else -> {
                when {
                    hours > 0 -> "${hours}h ${minutes}m"
                    minutes > 0 -> "${minutes}m ${seconds}s"
                    else -> "${seconds}s"
                }
            }
        }
    }
    
    /**
     * Format file size based on locale
     */
    fun formatFileSize(sizeBytes: Long): String {
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when (_currentLocale.value.language) {
            "zh" -> {
                when {
                    gb >= 1 -> String.format("%.1f GB", gb)
                    mb >= 1 -> String.format("%.1f MB", mb)
                    kb >= 1 -> String.format("%.1f KB", kb)
                    else -> "${sizeBytes} 字节"
                }
            }
            else -> {
                when {
                    gb >= 1 -> String.format("%.1f GB", gb)
                    mb >= 1 -> String.format("%.1f MB", mb)
                    kb >= 1 -> String.format("%.1f KB", kb)
                    else -> "$sizeBytes bytes"
                }
            }
        }
    }
    
    /**
     * Format date based on locale
     */
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = java.text.DateFormat.getDateInstance(
            java.text.DateFormat.MEDIUM,
            _currentLocale.value
        )
        return formatter.format(date)
    }
    
    /**
     * Format relative time (e.g., "2 hours ago")
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when (_currentLocale.value.language) {
            "zh" -> {
                when {
                    days > 0 -> "${days}天前"
                    hours > 0 -> "${hours}小时"
                    minutes > 0 -> "${minutes}分钟"
                    else -> "刚刚"
                }
            }
            else -> {
                when {
                    days > 0 -> "$days days ago"
                    hours > 0 -> "$hours hours ago"
                    minutes > 0 -> "$minutes minutes ago"
                    else -> "just now"
                }
            }
        }
    }
}

/**
 * Composable for providing localization context
 */
@Composable
fun rememberLocalizationManager(): LocalizationManager {
    val context = LocalContext.current
    return remember { LocalizationManager(context) }
}

/**
 * Localization provider composable
 */
@Composable
fun LocalizationProvider(
    localizationManager: LocalizationManager = rememberLocalizationManager(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLocalizationManager provides localizationManager,
        content = content
    )
}

/**
 * CompositionLocal for localization manager
 */
val LocalLocalizationManager = compositionLocalOf<LocalizationManager> {
    error("LocalizationManager not provided")
}