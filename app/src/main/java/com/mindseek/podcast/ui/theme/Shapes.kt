package com.mindseek.podcast.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 播客应用形状系统
 * 基于Material Design 3规范，为播客应用提供一致的圆角和形状设�?
 */
val PodcastShapes = Shapes(
    // 小圆�?- 用于按钮、芯片等小组件
    extraSmall = RoundedCornerShape(4.dp),
    
    // 中小圆角 - 用于卡片、输入框等
    small = RoundedCornerShape(8.dp),
    
    // 中等圆角 - 用于播客封面、节目卡片等
    medium = RoundedCornerShape(12.dp),
    
    // 大圆角- 用于底部弹窗、对话框等
    large = RoundedCornerShape(16.dp),
    
    // 超大圆角 - 用于全屏模态等
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * 播客应用自定义形状
 * 用于特定UI组件的形状定义
 */
object PodcastCustomShapes {
    // 播放器控制按钮- 圆形
    val PlayerButton = RoundedCornerShape(50)
    
    // 播客封面 - 中等圆角
    val PodcastCover = RoundedCornerShape(12.dp)
    
    // 迷你播放器- 顶部圆角
    val MiniPlayer = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // 底部导航栏- 顶部圆角
    val BottomNavigation = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // 搜索框- 大圆角
    val SearchBar = RoundedCornerShape(24.dp)
    
    // 评论气泡 - 不对称圆�?
    val CommentBubble = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 16.dp
    )
    
    // 进度条轨�?- 胶囊形状
    val ProgressTrack = RoundedCornerShape(50)
    
    // 标签/徽章 - 小圆�?
    val Badge = RoundedCornerShape(6.dp)
    
    // 播放列表�?- 轻微圆角
    val PlaylistItem = RoundedCornerShape(8.dp)
    
    // 全屏播放�?- 顶部圆角
    val FullScreenPlayer = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}