# Travel Footprint Map (旅行足迹地图)
一款基于 Android + Jetpack Compose 的旅行足迹记录应用，帮助你在高德地图上记录、可视化和管理你的旅行经历。

## 主要功能
- 📌 足迹记录 — 在地图上标记你去过的地方，添加描述、评分和标签
- 🚗 旅程管理 — 记录旅程中的各种见闻（照片），支持封面、描述和地点信息
- 💡 点亮城市 — 标记你去过的城市和省份，在交互式 SVG 中国地图上可视化展示
- 🤖 AI 智能生成 — 调用 AI 服务自动生成旅程/足迹的标题、描述和评分，还能将旅程封面绘制成各种风格的趣味图片
- 📍 后台轨迹录制 — 前台服务在后台实时定位跟踪，记录旅行轨迹路线
- 🌤️ 天气预报 — 集成高德天气 API，显示当前城市天气
- 🏆 里程碑统计 — 统计旅行里程、成就解锁、月度图表等数据
## 技术栈
类别 技术 语言 Kotlin 100% UI Jetpack Compose, Material 3 依赖注入 Dagger Hilt + KSP 数据库 Room + KSP 地图 高德地图 3D SDK 定位 高德定位 SDK + Google Play Services Location 网络 OkHttp + Gson 图片加载 Coil 导航 自定义 ViewModel 驱动导航 + 底部导航栏 本地存储 DataStore Preferences AI 服务端 Python Flask 编译 AGP 8.7.0, Kotlin 1.9.24, Java 17 最低支持 Android 7.0 (API 24)

## 项目架构
项目采用 Clean Architecture 风格分层：

## 功能模块
模块 说明 旅程 记录旅程中的照片见闻，支持封面、描述、地点信息，AI 可生成多种风格的封面图片 足迹 在地图上标记足迹点，包含描述、评分、运动数据（距离/时长/速度/卡路里） 点亮城市 标记已游览城市，在 SVG 中国地图上互动展示，支持省/市两级粒度 AI 助手 智能生成标题、描述、评分；将旅程封面绘制成各种风格的趣味图片 轨迹录制 后台前台服务 + 实时定位跟踪，在地图上绘制运动路径 天气 基于高德天气 API 的地图天气卡片 里程碑 里程统计、解锁进度、月度图表

## 环境要求
- Android Studio Ladybug (2024.2.1) 或更高版本
- JDK 17
- 高德地图 SDK Key（需在 AndroidManifest.xml 中配置）
- Android 设备或模拟器（API 24+，建议 arm64-v8a 架构）
## 快速开始
1. 克隆项目
2. 在 AndroidManifest.xml 中配置高德地图 App Key
3. 使用 Android Studio 打开项目，等待 Gradle 同步完成
4. 连接设备或启动模拟器，点击运行
## 下载体验
本项目提供已打包好的 APK 安装包，可直接在 Android 设备上安装体验。
