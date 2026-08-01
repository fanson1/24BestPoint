# 极速24点 (24 Best Point) - Kotlin Multiplatform Project

这是一个使用 Kotlin Multiplatform (KMP) 开发的全栈跨端项目，支持 Android、iOS、Desktop (JVM)、Web (Wasm) 和后端 Server。所有用户数据均通过云端同步。

## 核心特性 (Core Features)

*   **全栈跨端 (Full-stack Multiplatform)**: 使用 Compose Multiplatform 实现 UI 共享，支持 Android, iOS, Desktop (JVM), 和 Web (Wasm)。
*   **云端实时同步 (Cloud Sync)**: 玩家进度、得分、等级、提示额度跨设备无缝同步。
*   **单点登录 (SSO) 与安全**: 
    *   支持多设备互占踢出。
    *   **优化体验**: 当账号在另一台设备登录时，当前设备会弹出确认对话框提示“会话已失效”并引导重新登录。
*   **多语言国际化 (Localization)**: 
    *   完整适配中英文环境。
    *   **动态等级系统**: 等级标签（如：小学/Primary, 教授/Professor）根据当前系统语言动态映射。
*   **Web 端深度优化 (Web Optimization)**: 
    *   针对 Wasm 目标进行了资源预加载（Noto Sans SC 字体）。
    *   解决 Skia 渲染中的方块（Tofu）问题，提供平滑的中文显示体验。
    *   API 请求增加 Cache-Busting，解决浏览器缓存导致的数据不同步。

## 项目结构 (Project Structure)

* [/composeApp](./composeApp/src) : **Compose Multiplatform 核心模块**。包含所有跨端共享的 UI 和逻辑代码。
  - [commonMain](./composeApp/src/commonMain/kotlin) : 核心业务逻辑（MVI 架构、ViewModel、SSO 逻辑、算法等）。
  - [wasmJsMain](./composeApp/src/wasmJsMain/kotlin) : Web (Wasm) 平台适配及字体资源优化。
* [/server](./server/src/main/kotlin) : **Ktor 后端模块**。
  - 提供 REST API 支持，使用 Exposed ORM 持久化数据。
  - **自动同步**: 启动时自动完成数据库架构迁移（Migration）。

## 构建与运行 (Build and Run)

### 1. 启动后端服务器 (Cloud Server)

必须先启动后端，否则客户端登录和同步功能将不可用：
```shell
./gradlew :server:run
```
默认运行在 `http://localhost:8081`。

### 2. 运行移动端/桌面端 (Mobile & Desktop)

*   **Android**: 选择 `app` 运行配置或使用 `./gradlew :app:assembleDebug`。
*   **Desktop**: `./gradlew :composeApp:run`。
*   **iOS**: 在 Mac 上使用 Xcode 打开 `iosApp` 目录运行。

### 3. 运行 Web 浏览器端 (Web Wasm)

```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
*首次加载会下载 16MB 的中文字体文件，请耐心等待。*

## 技术栈 (Technology Stack)

- **UI 框架**: Compose Multiplatform
- **逻辑控制**: MVI (Model-View-Intent)
- **依赖注入**: Koin
- **网络引擎**: Ktor Client (OkHttp/Darwin/JS)
- **后端框架**: Ktor Server
- **持久化层**: Exposed ORM + H2 Database
- **数据序列化**: Kotlinx Serialization
- **国际化**: Compose Multiplatform Resources

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) and [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/).
