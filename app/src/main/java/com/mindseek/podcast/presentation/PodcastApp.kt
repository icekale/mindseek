package com.mindseek.podcast.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindseek.podcast.core.error.ErrorHandler
import com.mindseek.podcast.presentation.components.GlobalErrorHandler
import com.mindseek.podcast.presentation.navigation.PodcastBottomNavigation
import com.mindseek.podcast.presentation.navigation.PodcastNavGraph
import com.mindseek.podcast.presentation.navigation.Screen
import com.mindseek.podcast.presentation.navigation.bottomNavScreens
import com.mindseek.podcast.presentation.player.components.MiniPlayer
import com.mindseek.podcast.ui.theme.XiaoyuzhouPodcastAppTheme
import javax.inject.Inject

/**
 * 应用主容器组�?
 */
@Composable
fun PodcastApp() {
    XiaoyuzhouPodcastAppTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val snackbarHostState = remember { SnackbarHostState() }
        val globalErrorViewModel: GlobalErrorViewModel = hiltViewModel()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                // 只在底部导航页面显示底部导航栏
                if (currentRoute in bottomNavScreens.map { it.route }) {
                    PodcastBottomNavigation(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 主要内容区域
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        PodcastNavGraph(
                            navController = navController,
                            startDestination = Screen.Home.route
                        )
                    }
                    
                    // 迷你播放器- 只在非播放器页面显示
                    if (currentRoute != Screen.Player.route) {
                        MiniPlayer(
                            onExpandToFullPlayer = {
                                navController.navigate(Screen.Player.route)
                            }
                        )
                    }
                }
                
                // 全局错误处理
                GlobalErrorHandler(
                    errorHandler = globalErrorViewModel.errorHandler,
                    snackbarHostState = snackbarHostState,
                    onRetry = { errorEvent ->
                        // 可以根据错误类型和上下文实现特定的重试逻辑
                        // 这里暂时不实现具体的重试逻辑，由各个页面自己处理
                    }
                )
            }
        }
    }
}