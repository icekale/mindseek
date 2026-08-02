package com.mindseek.podcast.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions

/**
 * 导航辅助类，提供常用的导航操�?
 */
class NavigationHelper(private val navController: NavController) {
    
    /**
     * 导航到播客详情页�?
     */
    fun navigateToPodcastDetail(podcastId: String) {
        navController.navigate(Screen.PodcastDetail.createRoute(podcastId))
    }
    
    /**
     * 导航到播放器页面
     */
    fun navigateToPlayer() {
        navController.navigate(Screen.Player.route)
    }
    
    /**
     * 导航到历史记录页�?
     */
    fun navigateToHistory() {
        navController.navigate(Screen.History.route)
    }
    
    /**
     * 导航到收藏页�?
     */
    fun navigateToFavorites() {
        navController.navigate(Screen.Favorites.route)
    }
    
    /**
     * 返回上一�?
     */
    fun navigateBack(): Boolean {
        return navController.popBackStack()
    }
    
    /**
     * 导航到底部导航页面，清除回退�?
     */
    fun navigateToBottomNavScreen(screen: Screen) {
        if (screen in bottomNavScreens) {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    /**
     * 清除回退栈并导航到指定页�?
     */
    fun navigateAndClearStack(route: String) {
        navController.navigate(route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    }
    
    /**
     * 获取当前路由
     */
    fun getCurrentRoute(): String? {
        return navController.currentBackStackEntry?.destination?.route
    }
    
    /**
     * 检查是否可以返�?
     */
    fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }
}

/**
 * 创建导航辅助类的扩展函数
 */
fun NavController.asNavigationHelper(): NavigationHelper {
    return NavigationHelper(this)
}