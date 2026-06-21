/*
 * ============================================================================
 * MainScreen2.kt - 应用主界面 Composable
 * ============================================================================
 *
 * 【用途】
 *   - 应用的根级 UI 容器，组合所有顶层界面元素
 *   - 负责管理闪屏动画与主界面之间的切换逻辑
 *
 * 【功能】
 *   1. 展示闪屏动画（SplashScreen），动画结束后自动切换到主界面
 *   2. 主界面采用 Column 垂直布局：上方为页面内容区（CustomNavHost2），下方为底部导航栏（Navigation2）
 *   3. 可选叠加调试悬浮面板（DebugOverlay），仅在传入 debugHelper 时显示
 *
 * 【关联组件】
 *   - SplashScreen: 闪屏动画组件，包含 Logo 缩放和淡入淡出效果
 *   - CustomNavHost2: 自定义导航容器，根据当前路由展示 LightenScreen2 或 JourneyScreen2，带白板过渡动画
 *   - Navigation2: 自定义底部导航栏，包含"点亮"和"旅程"两个 Tab，带选中动画
 *   - DebugOverlay: 调试悬浮层，右下角悬浮按钮触发的调试面板，用于测试点亮城市等功能
 *   - NavPathObj2: 导航路由对象，定义了 lighten（点亮）和 journey（旅程）两个导航目标
 *   - CustomNavController: 全局导航状态管理器，维护当前页面路由和防抖导航逻辑
 *
 * 【实现逻辑简述】
 *   - 使用两个 remember 状态：showSplash 控制闪屏显示，showScreen 控制主界面显示
 *   - Box 布局实现闪屏与主界面的层叠关系，闪屏在上层播放动画
 *   - 闪屏回调 onShowScreen 先触发主界面渲染，动画结束后 onFinished 移除闪屏层
 *   - Surface 包裹主界面内容并提供主题背景色
 *   - Column 使用 weight(1f) 让导航内容区占据剩余空间，底部导航栏自适应高度
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation.screen

// Compose 布局组件

// Material 3 主题组件

// Compose 运行时

// Compose UI 修饰符

// 项目内部组件
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.debug.DebugOverlay
import com.example.travel_footprint_android.presentation.navigation.CustomNavHost2
import com.example.travel_footprint_android.presentation.navigation.Navigation2
import com.example.travel_footprint_android.presentation.screen.viewmodel.NavigationViewModel
import com.example.travel_footprint_android.utils.DebugHelper

/**
 * 应用主界面 Composable 函数
 *
 * @param debugHelper 调试工具类实例，传入 null 时不显示调试面板（默认不显示）
 */
@Composable
fun MainScreen2(
    debugHelper: DebugHelper? = null,
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    var showSplash by remember { mutableStateOf(true) }
    var showScreen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        if (showScreen) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CustomNavHost2(
                            navigationViewModel = navigationViewModel,
                        )
                    }
                    Navigation2(
                        modifier = Modifier.wrapContentHeight(),
                        navigationViewModel = navigationViewModel,
                    )
                }
                if (debugHelper != null) {
                    DebugOverlay(
                        debugHelper = debugHelper,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        if (showSplash) {
            SplashScreen(
                onFinished = { showSplash = false },
                onShowScreen = { showScreen = true }
            )
        }
    }
}
