package com.mindseek.podcast.presentation.navigation

/**
 * 导航参数常量
 */
object NavArgs {
    const val PODCAST_ID = "podcastId"
    const val EPISODE_ID = "episodeId"
    const val AUTO_PLAY = "autoPlay"
}

/**
 * 应用导航路由定义
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Subscription : Screen("subscription")
    object Profile : Screen("profile")
    object PodcastDetail : Screen("podcast_detail/{${NavArgs.PODCAST_ID}}") {
        fun createRoute(podcastId: String) = "podcast_detail/$podcastId"
    }
    object Player : Screen("player?${NavArgs.AUTO_PLAY}={${NavArgs.AUTO_PLAY}}") {
        fun createRoute(autoPlay: Boolean = false) = "player?${NavArgs.AUTO_PLAY}=$autoPlay"
    }
    object History : Screen("history")
    object Favorites : Screen("favorites")
    object Comments : Screen("comments/{${NavArgs.EPISODE_ID}}") {
        fun createRoute(episodeId: String) = "comments/$episodeId"
    }
}

/**
 * 底部导航栏路�?
 */
val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Subscription,
    Screen.Profile
)