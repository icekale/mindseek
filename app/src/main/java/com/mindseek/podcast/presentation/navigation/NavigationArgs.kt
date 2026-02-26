package com.mindseek.podcast.presentation.navigation

import android.os.Bundle
import androidx.navigation.NavBackStackEntry

/**
 * 导航参数辅助�?
 */
object NavigationArgs {
    
    // 使用Screen.kt中定义的常量
    const val PODCAST_ID = NavArgs.PODCAST_ID
    const val EPISODE_ID = NavArgs.EPISODE_ID
    const val AUTO_PLAY = NavArgs.AUTO_PLAY
    
    /**
     * 从NavBackStackEntry中安全获取字符串参数
     */
    fun NavBackStackEntry.getStringArg(key: String): String {
        return arguments?.getString(key) ?: ""
    }
    
    /**
     * 从NavBackStackEntry中安全获取布尔参�?
     */
    fun NavBackStackEntry.getBooleanArg(key: String, defaultValue: Boolean = false): Boolean {
        return arguments?.getBoolean(key, defaultValue) ?: defaultValue
    }
    
    /**
     * 从NavBackStackEntry中安全获取整数参�?
     */
    fun NavBackStackEntry.getIntArg(key: String, defaultValue: Int = 0): Int {
        return arguments?.getInt(key, defaultValue) ?: defaultValue
    }
    
    /**
     * 从NavBackStackEntry中安全获取长整数参数
     */
    fun NavBackStackEntry.getLongArg(key: String, defaultValue: Long = 0L): Long {
        return arguments?.getLong(key, defaultValue) ?: defaultValue
    }
}

/**
 * 导航数据传递辅助类
 * 用于在页面间传递复杂数�?
 */
object NavigationDataStore {
    private val dataMap = mutableMapOf<String, Any>()
    
    /**
     * 存储数据
     */
    fun <T> putData(key: String, data: T) {
        dataMap[key] = data as Any
    }
    
    /**
     * 获取数据
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getData(key: String): T? {
        return dataMap[key] as? T
    }
    
    /**
     * 移除数据
     */
    fun removeData(key: String) {
        dataMap.remove(key)
    }
    
    /**
     * 清除所有数�?
     */
    fun clearAll() {
        dataMap.clear()
    }
    
    // 预定义的数据�?
    object Keys {
        const val SELECTED_PODCAST = "selected_podcast"
        const val SELECTED_EPISODE = "selected_episode"
        const val PLAYER_STATE = "player_state"
        const val SEARCH_QUERY = "search_query"
    }
}