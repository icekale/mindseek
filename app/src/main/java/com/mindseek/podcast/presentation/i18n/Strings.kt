package com.mindseek.podcast.presentation.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.*

/**
 * String resources for internationalization
 */
object Strings {
    
    // Navigation
    const val NAV_HOME = "nav_home"
    const val NAV_SEARCH = "nav_search"
    const val NAV_SUBSCRIPTION = "nav_subscription"
    const val NAV_PROFILE = "nav_profile"
    
    // Common actions
    const val ACTION_PLAY = "action_play"
    const val ACTION_PAUSE = "action_pause"
    const val ACTION_STOP = "action_stop"
    const val ACTION_NEXT = "action_next"
    const val ACTION_PREVIOUS = "action_previous"
    const val ACTION_RETRY = "action_retry"
    const val ACTION_CANCEL = "action_cancel"
    const val ACTION_CONFIRM = "action_confirm"
    const val ACTION_SAVE = "action_save"
    const val ACTION_DELETE = "action_delete"
    const val ACTION_SHARE = "action_share"
    const val ACTION_DOWNLOAD = "action_download"
    const val ACTION_SUBSCRIBE = "action_subscribe"
    const val ACTION_UNSUBSCRIBE = "action_unsubscribe"
    const val ACTION_FAVORITE = "action_favorite"
    const val ACTION_UNFAVORITE = "action_unfavorite"
    
    // Loading states
    const val LOADING_PODCASTS = "loading_podcasts"
    const val LOADING_EPISODES = "loading_episodes"
    const val LOADING_COMMENTS = "loading_comments"
    const val LOADING_GENERAL = "loading_general"
    
    // Error messages
    const val ERROR_NETWORK = "error_network"
    const val ERROR_SERVER = "error_server"
    const val ERROR_UNKNOWN = "error_unknown"
    const val ERROR_PLAYBACK = "error_playback"
    const val ERROR_DOWNLOAD = "error_download"
    const val ERROR_STORAGE = "error_storage"
    
    // Content descriptions
    const val CD_PODCAST_COVER = "cd_podcast_cover"
    const val CD_PLAY_BUTTON = "cd_play_button"
    const val CD_PAUSE_BUTTON = "cd_pause_button"
    const val CD_NEXT_BUTTON = "cd_next_button"
    const val CD_PREVIOUS_BUTTON = "cd_previous_button"
    const val CD_VOLUME_SLIDER = "cd_volume_slider"
    const val CD_PROGRESS_SLIDER = "cd_progress_slider"
    const val CD_SPEED_CONTROL = "cd_speed_control"
    const val CD_FAVORITE_BUTTON = "cd_favorite_button"
    const val CD_DOWNLOAD_BUTTON = "cd_download_button"
    const val CD_SHARE_BUTTON = "cd_share_button"
    const val CD_MORE_OPTIONS = "cd_more_options"
    
    // Player states
    const val PLAYER_PLAYING = "player_playing"
    const val PLAYER_PAUSED = "player_paused"
    const val PLAYER_STOPPED = "player_stopped"
    const val PLAYER_LOADING = "player_loading"
    const val PLAYER_ERROR = "player_error"
    
    // Time formats
    const val TIME_HOURS = "time_hours"
    const val TIME_MINUTES = "time_minutes"
    const val TIME_SECONDS = "time_seconds"
    const val TIME_JUST_NOW = "time_just_now"
    const val TIME_DAYS_AGO = "time_days_ago"
    const val TIME_HOURS_AGO = "time_hours_ago"
    const val TIME_MINUTES_AGO = "time_minutes_ago"
    
    // File sizes
    const val SIZE_BYTES = "size_bytes"
    const val SIZE_KB = "size_kb"
    const val SIZE_MB = "size_mb"
    const val SIZE_GB = "size_gb"
    
    // Accessibility
    const val A11Y_HEADING_LEVEL = "a11y_heading_level"
    const val A11Y_SELECTED = "a11y_selected"
    const val A11Y_NOT_SELECTED = "a11y_not_selected"
    const val A11Y_ENABLED = "a11y_enabled"
    const val A11Y_DISABLED = "a11y_disabled"
    const val A11Y_PROGRESS = "a11y_progress"
    const val A11Y_LOADING_PROGRESS = "a11y_loading_progress"
}

/**
 * String resource provider
 */
class StringProvider(private val locale: Locale) {
    
    private val strings = mapOf(
        // Chinese strings
        Locale.CHINESE.language to mapOf(
            Strings.NAV_HOME to "首页",
            Strings.NAV_SEARCH to "搜索",
            Strings.NAV_SUBSCRIPTION to "订阅",
            Strings.NAV_PROFILE to "我的",
            
            Strings.ACTION_PLAY to "播放",
            Strings.ACTION_PAUSE to "暂停",
            Strings.ACTION_STOP to "停止",
            Strings.ACTION_NEXT to "下一集",
            Strings.ACTION_PREVIOUS to "上一集",
            Strings.ACTION_RETRY to "重试",
            Strings.ACTION_CANCEL to "取消",
            Strings.ACTION_CONFIRM to "确定",
            Strings.ACTION_SAVE to "保存",
            Strings.ACTION_DELETE to "删除",
            Strings.ACTION_SHARE to "分享",
            Strings.ACTION_DOWNLOAD to "下载",
            Strings.ACTION_SUBSCRIBE to "订阅",
            Strings.ACTION_UNSUBSCRIBE to "取消订阅",
            Strings.ACTION_FAVORITE to "收藏",
            Strings.ACTION_UNFAVORITE to "取消收藏",
            
            Strings.LOADING_PODCASTS to "加载播客...",
            Strings.LOADING_EPISODES to "加载节目...",
            Strings.LOADING_COMMENTS to "加载评论...",
            Strings.LOADING_GENERAL to "加载...",
            
            Strings.ERROR_NETWORK to "网络连接失败",
            Strings.ERROR_SERVER to "服务器错误",
            Strings.ERROR_UNKNOWN to "未知错误",
            Strings.ERROR_PLAYBACK to "播放失败",
            Strings.ERROR_DOWNLOAD to "下载失败",
            Strings.ERROR_STORAGE to "存储空间不足",
            
            Strings.CD_PODCAST_COVER to "播客封面",
            Strings.CD_PLAY_BUTTON to "播放按钮",
            Strings.CD_PAUSE_BUTTON to "暂停按钮",
            Strings.CD_NEXT_BUTTON to "下一集按钮",
            Strings.CD_PREVIOUS_BUTTON to "上一集按钮",
            Strings.CD_VOLUME_SLIDER to "音量滑块",
            Strings.CD_PROGRESS_SLIDER to "进度滑块",
            Strings.CD_SPEED_CONTROL to "播放速度控制",
            Strings.CD_FAVORITE_BUTTON to "收藏按钮",
            Strings.CD_DOWNLOAD_BUTTON to "下载按钮",
            Strings.CD_SHARE_BUTTON to "分享按钮",
            Strings.CD_MORE_OPTIONS to "更多选项",
            
            Strings.PLAYER_PLAYING to "正在播放",
            Strings.PLAYER_PAUSED to "已暂停",
            Strings.PLAYER_STOPPED to "已停止",
            Strings.PLAYER_LOADING to "加载中",
            Strings.PLAYER_ERROR to "播放错误",
            
            Strings.TIME_HOURS to "小时",
            Strings.TIME_MINUTES to "分钟",
            Strings.TIME_SECONDS to "秒",
            Strings.TIME_JUST_NOW to "刚刚",
            Strings.TIME_DAYS_AGO to "天前",
            Strings.TIME_HOURS_AGO to "小时",
            Strings.TIME_MINUTES_AGO to "分钟",
            
            Strings.SIZE_BYTES to "字节",
            Strings.SIZE_KB to "KB",
            Strings.SIZE_MB to "MB",
            Strings.SIZE_GB to "GB",
            
            Strings.A11Y_HEADING_LEVEL to "标题级别",
            Strings.A11Y_SELECTED to "已选择",
            Strings.A11Y_NOT_SELECTED to "未选择",
            Strings.A11Y_ENABLED to "已启用",
            Strings.A11Y_DISABLED to "已禁用",
            Strings.A11Y_PROGRESS to "进度",
            Strings.A11Y_LOADING_PROGRESS to "加载进度"
        ),
        
        // English strings
        Locale.ENGLISH.language to mapOf(
            Strings.NAV_HOME to "Home",
            Strings.NAV_SEARCH to "Search",
            Strings.NAV_SUBSCRIPTION to "Subscriptions",
            Strings.NAV_PROFILE to "Profile",
            
            Strings.ACTION_PLAY to "Play",
            Strings.ACTION_PAUSE to "Pause",
            Strings.ACTION_STOP to "Stop",
            Strings.ACTION_NEXT to "Next",
            Strings.ACTION_PREVIOUS to "Previous",
            Strings.ACTION_RETRY to "Retry",
            Strings.ACTION_CANCEL to "Cancel",
            Strings.ACTION_CONFIRM to "Confirm",
            Strings.ACTION_SAVE to "Save",
            Strings.ACTION_DELETE to "Delete",
            Strings.ACTION_SHARE to "Share",
            Strings.ACTION_DOWNLOAD to "Download",
            Strings.ACTION_SUBSCRIBE to "Subscribe",
            Strings.ACTION_UNSUBSCRIBE to "Unsubscribe",
            Strings.ACTION_FAVORITE to "Favorite",
            Strings.ACTION_UNFAVORITE to "Unfavorite",
            
            Strings.LOADING_PODCASTS to "Loading podcasts...",
            Strings.LOADING_EPISODES to "Loading episodes...",
            Strings.LOADING_COMMENTS to "Loading comments...",
            Strings.LOADING_GENERAL to "Loading...",
            
            Strings.ERROR_NETWORK to "Network connection failed",
            Strings.ERROR_SERVER to "Server error",
            Strings.ERROR_UNKNOWN to "Unknown error",
            Strings.ERROR_PLAYBACK to "Playback failed",
            Strings.ERROR_DOWNLOAD to "Download failed",
            Strings.ERROR_STORAGE to "Insufficient storage space",
            
            Strings.CD_PODCAST_COVER to "Podcast cover",
            Strings.CD_PLAY_BUTTON to "Play button",
            Strings.CD_PAUSE_BUTTON to "Pause button",
            Strings.CD_NEXT_BUTTON to "Next episode button",
            Strings.CD_PREVIOUS_BUTTON to "Previous episode button",
            Strings.CD_VOLUME_SLIDER to "Volume slider",
            Strings.CD_PROGRESS_SLIDER to "Progress slider",
            Strings.CD_SPEED_CONTROL to "Playback speed control",
            Strings.CD_FAVORITE_BUTTON to "Favorite button",
            Strings.CD_DOWNLOAD_BUTTON to "Download button",
            Strings.CD_SHARE_BUTTON to "Share button",
            Strings.CD_MORE_OPTIONS to "More options",
            
            Strings.PLAYER_PLAYING to "Playing",
            Strings.PLAYER_PAUSED to "Paused",
            Strings.PLAYER_STOPPED to "Stopped",
            Strings.PLAYER_LOADING to "Loading",
            Strings.PLAYER_ERROR to "Playback error",
            
            Strings.TIME_HOURS to "hours",
            Strings.TIME_MINUTES to "minutes",
            Strings.TIME_SECONDS to "seconds",
            Strings.TIME_JUST_NOW to "just now",
            Strings.TIME_DAYS_AGO to "days ago",
            Strings.TIME_HOURS_AGO to "hours ago",
            Strings.TIME_MINUTES_AGO to "minutes ago",
            
            Strings.SIZE_BYTES to "bytes",
            Strings.SIZE_KB to "KB",
            Strings.SIZE_MB to "MB",
            Strings.SIZE_GB to "GB",
            
            Strings.A11Y_HEADING_LEVEL to "Heading level",
            Strings.A11Y_SELECTED to "Selected",
            Strings.A11Y_NOT_SELECTED to "Not selected",
            Strings.A11Y_ENABLED to "Enabled",
            Strings.A11Y_DISABLED to "Disabled",
            Strings.A11Y_PROGRESS to "Progress",
            Strings.A11Y_LOADING_PROGRESS to "Loading progress"
        )
    )
    
    fun getString(key: String): String {
        val languageStrings = strings[locale.language] ?: strings[Locale.ENGLISH.language]!!
        return languageStrings[key] ?: key
    }
    
    fun getString(key: String, vararg args: Any): String {
        val template = getString(key)
        return String.format(locale, template, *args)
    }
}

/**
 * Composable function to get localized strings
 */
@Composable
@ReadOnlyComposable
fun stringResource(key: String): String {
    val localizationManager = LocalLocalizationManager.current
    val stringProvider = StringProvider(localizationManager.currentLocale.value)
    return stringProvider.getString(key)
}

/**
 * Composable function to get localized strings with formatting
 */
@Composable
@ReadOnlyComposable
fun stringResource(key: String, vararg args: Any): String {
    val localizationManager = LocalLocalizationManager.current
    val stringProvider = StringProvider(localizationManager.currentLocale.value)
    return stringProvider.getString(key, *args)
}