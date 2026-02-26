package com.mindseek.podcast.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主题模式枚举
 */
enum class ThemeMode {
    LIGHT,      // 浅色主题
    DARK,       // 深色主题
    SYSTEM      // 跟随系统
}

/**
 * 主题管理器
 * 负责管理应用的主题设置和偏好
 */
class ThemeManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 主题模式状态流
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    // 动态颜色状态流
    private val _dynamicColor = MutableStateFlow(getDynamicColor())
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()
    
    // 高对比度状态流
    private val _highContrast = MutableStateFlow(getHighContrast())
    val highContrast: StateFlow<Boolean> = _highContrast.asStateFlow()
    
    /**
     * 获取当前主题模式
     */
    private fun getThemeMode(): ThemeMode {
        val mode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }
    
    /**
     * 获取动态颜色设置
     */
    private fun getDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }
    
    /**
     * 设置动态颜色
     */
    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }
    
    /**
     * 获取高对比度设置
     */
    private fun getHighContrast(): Boolean {
        return prefs.getBoolean(KEY_HIGH_CONTRAST, false)
    }
    
    /**
     * 设置高对比度
     */
    fun setHighContrast(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        _highContrast.value = enabled
    }
    
    /**
     * 切换主题模式
     */
    fun toggleThemeMode() {
        val currentMode = _themeMode.value
        val nextMode = when (currentMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }
    
    /**
     * 切换动态颜色
     */
    fun toggleDynamicColor() {
        setDynamicColor(!_dynamicColor.value)
    }
    
    /**
     * 切换高对比度
     */
    fun toggleHighContrast() {
        setHighContrast(!_highContrast.value)
    }
    
    /**
     * 重置所有主题设置为默认值
     */
    fun resetToDefaults() {
        setThemeMode(ThemeMode.SYSTEM)
        setDynamicColor(true)
        setHighContrast(false)
    }
    
    /**
     * 获取主题模式的显示名称
     */
    fun getThemeModeDisplayName(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.LIGHT -> "浅色主题"
            ThemeMode.DARK -> "深色主题"
            ThemeMode.SYSTEM -> "跟随系统"
        }
    }
}

/**
 * Composable函数，用于在UI中使用主题管理器
 */
@Composable
fun rememberThemeManager(context: Context): ThemeManager {
    return ThemeManager.getInstance(context)
}

/**
 * Composable函数，获取当前主题状态
 */
@Composable
fun rememberThemeState(themeManager: ThemeManager): ThemeState {
    val themeMode by themeManager.themeMode.collectAsState()
    val dynamicColor by themeManager.dynamicColor.collectAsState()
    val highContrast by themeManager.highContrast.collectAsState()
    
    return ThemeState(
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        highContrast = highContrast
    )
}

/**
 * 主题状态数据类
 */
data class ThemeState(
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
    val highContrast: Boolean
)

/**
 * 主题工具类
 */
object ThemeUtils {
    
    /**
     * 根据主题模式和系统设置确定是否使用深色主题
     */
    fun shouldUseDarkTheme(themeMode: ThemeMode, isSystemInDarkTheme: Boolean): Boolean {
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme
        }
    }
    
    /**
     * 获取主题模式的图标资源ID
     */
    fun getThemeModeIcon(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.LIGHT -> "light_mode"
            ThemeMode.DARK -> "dark_mode"
            ThemeMode.SYSTEM -> "auto_mode"
        }
    }
    
    /**
     * 获取主题模式的描述
     */
    fun getThemeModeDescription(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.LIGHT -> "始终使用浅色主题"
            ThemeMode.DARK -> "始终使用深色主题"
            ThemeMode.SYSTEM -> "根据系统设置自动切换主题"
        }
    }
}