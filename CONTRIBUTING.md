# 贡献指南

感谢您对思维碰撞播客应用的关注！我们欢迎所有形式的贡献，包括但不限于代码、文档、设计、测试和反馈。

## 🤝 如何贡献

### 报告问题
如果您发现了bug或有功能建议，请：
1. 检查 [Issues](https://github.com/yourusername/mindseek-podcast/issues) 确保问题未被报告
2. 使用相应的issue模板创建新issue
3. 提供详细的描述和复现步骤

### 提交代码
1. **Fork项目** 到您的GitHub账户
2. **创建分支** 从 `main` 分支创建功能分支
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **编写代码** 遵循项目的编码规范
4. **添加测试** 为新功能添加相应的测试
5. **提交更改** 使用清晰的提交信息
   ```bash
   git commit -m "feat: add new search functionality"
   ```
6. **推送分支** 到您的fork
   ```bash
   git push origin feature/your-feature-name
   ```
7. **创建Pull Request** 详细描述您的更改

## 📝 编码规范

### Kotlin代码风格
- 遵循 [Kotlin官方编码约定](https://kotlinlang.org/docs/coding-conventions.html)
- 使用4个空格缩进
- 行长度限制为120字符
- 使用有意义的变量和函数名

### 架构规范
- 遵循MVVM架构模式
- 使用Repository模式管理数据
- 业务逻辑封装在UseCase中
- UI状态通过StateFlow管理

### 提交信息规范
使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：
- `feat:` 新功能
- `fix:` 修复bug
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建过程或辅助工具的变动

## 🧪 测试要求

### 单元测试
- 为新功能添加单元测试
- 测试覆盖率应保持在80%以上
- 使用JUnit和Mockito编写测试

### UI测试
- 为新的UI组件添加Compose测试
- 确保关键用户流程有端到端测试

### 运行测试
```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew testDebugUnitTest
```

## 📋 Pull Request检查清单

在提交PR之前，请确保：
- [ ] 代码遵循项目编码规范
- [ ] 添加了相应的测试
- [ ] 所有测试通过
- [ ] 更新了相关文档
- [ ] 提交信息清晰明确
- [ ] 没有合并冲突

## 🎯 开发环境设置

### 必需工具
- Android Studio Hedgehog | 2023.1.1+
- JDK 8+
- Git

### 推荐工具
- [Ktlint](https://ktlint.github.io/) - Kotlin代码格式化
- [Detekt](https://detekt.github.io/) - 静态代码分析

### 环境配置
1. 克隆项目
2. 在Android Studio中打开
3. 等待Gradle同步
4. 配置API密钥（如需要）

## 🐛 调试指南

### 常见问题
1. **构建失败**: 检查Gradle版本和依赖
2. **测试失败**: 确保模拟器/设备正常运行
3. **网络问题**: 检查API配置和网络连接

### 调试工具
- 使用Android Studio调试器
- 查看Logcat输出
- 使用Flipper进行网络调试

## 📚 学习资源

### Android开发
- [Android开发者官网](https://developer.android.com/)
- [Jetpack Compose文档](https://developer.android.com/jetpack/compose)
- [Kotlin官方文档](https://kotlinlang.org/docs/)

### 项目相关
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [MVVM模式](https://docs.microsoft.com/en-us/xamarin/xamarin-forms/enterprise-application-patterns/mvvm)

## 🏷️ 版本发布

### 版本号规则
遵循 [语义化版本](https://semver.org/) 规范：
- `MAJOR.MINOR.PATCH`
- 主版本号：不兼容的API修改
- 次版本号：向下兼容的功能性新增
- 修订号：向下兼容的问题修正

### 发布流程
1. 更新版本号
2. 更新CHANGELOG
3. 创建发布标签
4. 构建发布版本

## 💬 社区

### 沟通渠道
- GitHub Issues: 问题报告和功能请求
- GitHub Discussions: 一般讨论和问答
- Email: 私人或敏感问题

### 行为准则
我们致力于为每个人提供友好、安全和欢迎的环境。请：
- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

## 🎉 认可贡献者

我们会在以下地方认可贡献者：
- README文件中的贡献者列表
- 发布说明中的特别感谢
- 项目网站（如有）

感谢您的贡献！🙏