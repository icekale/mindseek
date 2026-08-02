package com.mindseek.podcast.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航栏项目数据类
 */
data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

/**
 * 底部导航栏项目列表
 */
val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        icon = Icons.Default.Home,
        label = "首页"
    ),
    BottomNavItem(
        screen = Screen.Search,
        icon = Icons.Default.Search,
        label = "搜索"
    ),
    BottomNavItem(
        screen = Screen.Profile,
        icon = Icons.Default.Person,
        label = "我的"
    )
)