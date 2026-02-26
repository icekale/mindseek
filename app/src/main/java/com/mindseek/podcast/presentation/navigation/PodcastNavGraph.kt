package com.mindseek.podcast.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mindseek.podcast.presentation.animation.PageTransitions
import com.mindseek.podcast.presentation.screens.FavoritesScreen
import com.mindseek.podcast.presentation.screens.HistoryScreen
import com.mindseek.podcast.presentation.screens.HomeScreen
import com.mindseek.podcast.presentation.screens.PodcastDetailScreen
import com.mindseek.podcast.presentation.screens.ProfileScreen
import com.mindseek.podcast.presentation.screens.SearchScreen
import com.mindseek.podcast.presentation.screens.SubscriptionScreen
import com.mindseek.podcast.presentation.player.PlayerScreen

/**
 * 应用主导航图
 */
@Composable
fun PodcastNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = PageTransitions.slideInFromRight(),
        exitTransition = PageTransitions.slideOutToLeft(),
        popEnterTransition = PageTransitions.slideInFromLeft(),
        popExitTransition = PageTransitions.slideOutToRight()
    ) {
        // 首页
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPodcastDetail = { podcastId ->
                    navController.navigate(Screen.PodcastDetail.createRoute(podcastId))
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        // 搜索页面
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToPodcastDetail = { podcastId ->
                    navController.navigate(Screen.PodcastDetail.createRoute(podcastId))
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        // 订阅页面
        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                onNavigateToPodcastDetail = { podcastId ->
                    navController.navigate(Screen.PodcastDetail.createRoute(podcastId))
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        // 个人中心
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        // 播客详情页面
        composable(
            route = Screen.PodcastDetail.route,
            arguments = listOf(
                navArgument(NavArgs.PODCAST_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val podcastId = backStackEntry.arguments?.getString(NavArgs.PODCAST_ID) ?: ""
            PodcastDetailScreen(
                podcastId = podcastId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        // 播放器页�?- 使用从底部滑入的动画
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument(NavArgs.AUTO_PLAY) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = PageTransitions.slideInFromBottom(),
            exitTransition = PageTransitions.slideOutToBottom(),
            popEnterTransition = PageTransitions.fadeIn(),
            popExitTransition = PageTransitions.slideOutToBottom()
        ) { backStackEntry ->
            val autoPlay = backStackEntry.arguments?.getBoolean(NavArgs.AUTO_PLAY, false) ?: false
            PlayerScreen(
                autoPlay = autoPlay,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 播放历史页面
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        // 收藏页面
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }

        // 评论页面
        composable(
            route = Screen.Comments.route,
            arguments = listOf(
                navArgument(NavArgs.EPISODE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString(NavArgs.EPISODE_ID) ?: ""
            // TODO: 实现评论页面
            // CommentsScreen(
            //     episodeId = episodeId,
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }
    }
}