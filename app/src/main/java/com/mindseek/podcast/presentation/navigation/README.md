# 导航架构文档

## 概述

本项目使用 Jetpack Compose Navigation 实现了完整的导航架构，支持底部导航栏、页面间数据传递和流畅的转场动画。

## 核心组件

### 1. Screen.kt - 路由定义
定义了应用中所有的导航路由和参数：

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Subscription : Screen("subscription")
    object Profile : Screen("profile")
    object PodcastDetail : Screen("podcast_detail/{podcastId}") {
        fun createRoute(podcastId: String) = "podcast_detail/$podcastId"
    }
    object Player : Screen("player?autoPlay={autoPlay}") {
        fun createRoute(autoPlay: Boolean = false) = "player?autoPlay=$autoPlay"
    }
    object History : Screen("history")
    object Favorites : Screen("favorites")
    object Comments : Screen("comments/{episodeId}") {
        fun createRoute(episodeId: String) = "comments/$episodeId"
    }
}
```

### 2. PodcastNavGraph.kt - 导航图
配置了所有页面的导航逻辑和转场动画：

- **流畅转场动画**: 使用 `slideIntoContainer` 和 `slideOutOfContainer` 实现页面间的滑动动画
- **参数传递**: 支持路径参数和查询参数
- **回退栈管理**: 正确处理页面回退逻辑

### 3. PodcastBottomNavigation.kt - 底部导航栏
实现了底部导航栏组件：

- **状态保存**: 支持页面状态保存和恢复
- **单例模式**: 避免重复导航到同一页面
- **动态显示**: 只在底部导航页面显示导航栏

### 4. NavigationHelper.kt - 导航辅助类
提供了便捷的导航操作方法：

```kotlin
class NavigationHelper(private val navController: NavController) {
    fun navigateToPodcastDetail(podcastId: String)
    fun navigateToPlayer()
    fun navigateToComments(episodeId: String)
    fun navigateBack(): Boolean
    fun navigateToBottomNavScreen(screen: Screen)
    fun navigateAndClearStack(route: String)
}
```

### 5. NavigationArgs.kt - 参数处理
提供了安全的参数获取方法和数据传递机制：

```kotlin
object NavigationArgs {
    fun NavBackStackEntry.getStringArg(key: String): String
    fun NavBackStackEntry.getBooleanArg(key: String, defaultValue: Boolean = false): Boolean
    // ... 其他参数获取方法
}

object NavigationDataStore {
    fun <T> putData(key: String, data: T)
    fun <T> getData(key: String): T?
    // ... 数据存储管理方法
}
```

## 功能特性

### 1. 底部导航栏
- 支持四个主要页面：首页、搜索、订阅、个人中心
- 自动状态保存和恢复
- 智能显示/隐藏逻辑

### 2. 页面间数据传递
- **路径参数**: 用于传递简单的ID等数据
- **查询参数**: 用于传递可选的配置参数
- **数据存储**: 用于传递复杂对象数据

### 3. 流畅转场动画
- 页面进入/退出动画
- 回退动画
- 300ms 动画时长，提供流畅的用户体验

### 4. 错误处理
- 参数安全获取，避免空指针异常
- 导航状态检查
- 回退栈验证

## 使用示例

### 基本导航
```kotlin
// 在 Composable 中使用
val navController = rememberNavController()

// 导航到播客详情
navController.navigate(Screen.PodcastDetail.createRoute("podcast123"))

// 导航到播放器并自动播放
navController.navigate(Screen.Player.createRoute(autoPlay = true))
```

### 使用导航辅助类
```kotlin
val navigationHelper = navController.asNavigationHelper()

// 导航到播客详情
navigationHelper.navigateToPodcastDetail("podcast123")

// 检查是否可以返回
if (navigationHelper.canNavigateBack()) {
    navigationHelper.navigateBack()
}
```

### 数据传递
```kotlin
// 存储复杂数据
NavigationDataStore.putData(NavigationDataStore.Keys.SELECTED_PODCAST, podcast)

// 在目标页面获取数据
val podcast = NavigationDataStore.getData<PodcastDomain>(NavigationDataStore.Keys.SELECTED_PODCAST)
```

## 满足的需求

### 需求 1.3 - 播客详情导航
✅ 实现了从首页点击播客封面导航到播客详情页面的功能

### 需求 5.2 - 流畅转场动画
✅ 实现了页面间的流畅滑动转场动画，提升用户体验

## 扩展性

导航架构设计具有良好的扩展性：

1. **新增页面**: 只需在 `Screen.kt` 中添加新的路由定义
2. **新增参数**: 在 `NavArgs` 中添加新的参数常量
3. **自定义动画**: 可以为特定页面定制专门的转场动画
4. **深度链接**: 支持添加深度链接处理

## 最佳实践

1. **使用类型安全的路由**: 通过 `createRoute()` 方法确保参数正确传递
2. **合理使用数据存储**: 对于复杂对象使用 `NavigationDataStore`，简单数据使用路径参数
3. **及时清理数据**: 在页面销毁时清理不需要的存储数据
4. **错误处理**: 始终使用安全的参数获取方法
5. **性能优化**: 合理使用 `launchSingleTop` 和 `restoreState` 参数