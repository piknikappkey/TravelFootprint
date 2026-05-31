// app/src/main/java/com/example/travel_footprint_android/MainActivity.kt

/*
 * ============================================================================
 * MainActivity.kt - 应用主入口 Activity
 * ============================================================================
 *
 * 【用途】
 *   - 整个应用的启动入口和根 Activity
 *   - 负责初始化 Compose UI 环境、启动主界面、管理闪屏动画
 *
 * 【功能】
 *   1. 启动闪屏（SplashScreen）动画，提升用户首次打开体验
 *   2. 启用边缘到边缘（Edge-to-Edge）沉浸式显示
 *   3. 加载并展示 MainScreen2 作为应用的主 UI 容器
 *   4. 通过 Hilt 依赖注入获取 DebugHelper 调试工具类
 *
 * 【关联组件】
 *   - MainScreen2: 应用主界面 Composable，包含导航、页面容器、调试面板
 *   - DebugHelper: 单例调试工具类，提供足迹、图片、城市点亮等功能的自动化测试方法
 *   - Hilt (@AndroidEntryPoint): 为 Activity 提供依赖注入能力
 *
 * 【实现逻辑简述】
 *   - onCreate() 中先安装闪屏，随后调用 enableEdgeToEdge() 实现全面屏适配
 *   - 通过 setContent {} 启动 Compose UI 树，将 MainScreen2 作为根 Composable
 *   - 当前 debugHelper 参数传 null，因此调试面板不会在主界面中显示
 * ============================================================================
 */

package com.example.travel_footprint_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.travel_footprint_android.presentation2.screen.MainScreen2
import com.example.travel_footprint_android.utils.DebugHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * AndroidEntryPoint 注解：让 Hilt 为该 Activity 生成依赖注入组件
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * 通过 Hilt 注入 DebugHelper 实例
     * 用于开发调试阶段自动化测试足迹、图片、城市点亮等核心功能
     */
    @Inject
    lateinit var debugHelper: DebugHelper

    /**
     * Activity 生命周期：创建时调用
     * - 安装并配置闪屏
     * - 启用沉浸式全屏显示
     * - 启动 Compose UI 并加载主界面
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装闪屏动画，并设置不保持闪屏状态（立即进入主界面）
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        // 启用 Edge-to-Edge 模式，让内容延伸到状态栏和导航栏下方
        enableEdgeToEdge()

        // 设置 Compose 内容，MainScreen2 为应用主界面
        // 当前 debugHelper 传 null，故调试悬浮面板不会显示
        setContent {
            MainScreen2(debugHelper = null)
        }
    }
}
