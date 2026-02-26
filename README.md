# 思维碰撞 - 播客应用

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg" alt="UI">
  <img src="https://img.shields.io/badge/Architecture-MVVM-red.svg" alt="Architecture">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</div>

## 📱 项目简介

思维碰撞是一款现代化的Android播客应用，采用最新的Android开发技术栈构建。应用提供了流畅的播客发现、订阅、播放和管理体验，支持离线下载、智能搜索、播放历史等丰富功能。

## ✨ 主要功能

### 🎧 核心功能
- **播客发现**: 推荐算法驱动的个性化播客推荐
- **智能搜索**: 支持播客和节目的全文搜索，带防抖优化
- **订阅管理**: 便捷的播客订阅和取消订阅
- **音频播放**: 基于ExoPlayer的高质量音频播放
- **离线下载**: 支持节目离线下载和管理

### 📊 用户体验
- **播放历史**: 自动记录播放进度和历史
- **收藏功能**: 收藏喜爱的节目和播客
- **评论系统**: 节目评论、回复和互动
- **存储管理**: 智能的本地存储管理
- **无障碍支持**: 完整的无障碍功能支持

### 🎨 界面设计
- **Material Design 3**: 遵循最新设计规范
- **深色模式**: 支持系统主题切换
- **响应式布局**: 适配不同屏幕尺寸
- **流畅动画**: 精心设计的页面转场和交互动画

## 🏗️ 技术架构

### 架构模式
- **MVVM**: Model-View-ViewModel架构模式
- **Clean Architecture**: 清晰的分层架构
- **Repository Pattern**: 数据访问层抽象
- **Use Case Pattern**: 业务逻辑封装

### 技术栈

#### 🎯 核心技术
- **Kotlin**: 100% Kotlin开发
- **Jetpack Compose**: 现代化声明式UI框架
- **Coroutines & Flow**: 异步编程和响应式数据流
- **Hilt**: 依赖注入框架

#### 🗄️ 数据层
- **Room**: 本地数据库
- **Retrofit**: 网络请求
- **OkHttp**: HTTP客户端
- **Gson**: JSON序列化

#### 🎵 媒体播放
- **ExoPlayer**: 音频播放引擎
- **Media3**: 媒体会话管理
- **Foreground Service**: 后台播放服务

#### 🧪 测试
- **JUnit**: 单元测试
- **Mockito**: Mock框架
- **Robolectric**: Android单元测试
- **Compose Testing**: UI测试

## 📁 项目结构

```
app/src/main/java/com/mindseek/podcast/
├── core/                          # 核心工具类
│   ├── debug/                     # 调试工具
│   ├── error/                     # 错误处理
│   └── performance/               # 性能优化
├── data/                          # 数据层
│   ├── local/                     # 本地数据
│   │   ├── dao/                   # 数据访问对象
│   │   └── entity/                # 数据实体
│   ├── mapper/                    # 数据映射
│   ├── remote/                    # 远程数据
│   │   ├── api/                   # API接口
│   │   └── dto/                   # 数据传输对象
│   └── repository/                # 仓库实现
├── di/                            # 依赖注入
├── domain/                        # 领域层
│   ├── model/                     # 领域模型
│   ├── repository/                # 仓库接口
│   └── usecase/                   # 用例
├── presentation/                  # 表现层
│   ├── components/                # 可复用组件
│   ├── navigation/                # 导航
│   ├── screens/                   # 页面
│   └── ui/                        # UI相关
├── service/                       # 系统服务
└── ui/theme/                      # 主题样式
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 8 或更高版本
- Android SDK API 24 (Android 7.0) 或更高版本
- Kotlin 1.9.20

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/mindseek-podcast.git
   cd mindseek-podcast
   ```

2. **打开项目**
   - 使用Android Studio打开项目
   - 等待Gradle同步完成

3. **配置API**
   - 在 `local.properties` 文件中添加API配置
   ```properties
   api.base.url="https://your-api-endpoint.com/"
   api.key="your-api-key"
   ```

4. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击运行按钮或使用快捷键 `Shift + F10`

## 🧪 测试

### 运行测试
```bash
# 运行所有单元测试
./gradlew test

# 运行UI测试
./gradlew connectedAndroidTest

# 生成测试报告
./gradlew testDebugUnitTest --continue
```

### 测试覆盖率
项目包含完整的测试套件：
- 单元测试覆盖核心业务逻辑
- 集成测试验证数据流
- UI测试确保用户体验

## 📈 性能优化

### 已实现的优化
- **启动优化**: 应用冷启动时间优化
- **内存管理**: 智能内存使用和回收
- **网络优化**: 请求缓存和重试机制
- **UI优化**: Compose性能最佳实践
- **电池优化**: 后台播放电量优化

### 监控工具
- **性能监控**: 集成性能监控工具
- **错误追踪**: 完整的错误日志系统
- **用户分析**: 用户行为分析

## 🔧 开发工具

### 代码质量
- **Ktlint**: Kotlin代码格式化
- **Detekt**: 静态代码分析
- **ProGuard**: 代码混淆和优化

### 调试工具
- **Flipper**: 网络和数据库调试
- **LeakCanary**: 内存泄漏检测
- **Timber**: 日志管理

## 🤝 贡献指南

我们欢迎所有形式的贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细信息。

### 贡献流程
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 团队

- **开发者**: [Your Name](https://github.com/yourusername)
- **设计师**: [Designer Name](https://github.com/designerusername)

## 📞 联系我们

- **项目主页**: [https://github.com/yourusername/mindseek-podcast](https://github.com/yourusername/mindseek-podcast)
- **问题反馈**: [Issues](https://github.com/yourusername/mindseek-podcast/issues)
- **邮箱**: your.email@example.com

## 🙏 致谢

感谢以下开源项目和社区的支持：
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [ExoPlayer](https://exoplayer.dev/)
- [Retrofit](https://square.github.io/retrofit/)
- [Room](https://developer.android.com/training/data-storage/room)
- [Hilt](https://dagger.dev/hilt/)

---

<div align="center">
  <p>如果这个项目对你有帮助，请给我们一个 ⭐️</p>
  <p>Made with ❤️ by MindSeek Team</p>
</div>