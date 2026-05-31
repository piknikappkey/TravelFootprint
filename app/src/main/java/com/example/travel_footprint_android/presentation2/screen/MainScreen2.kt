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

package com.example.travel_footprint_android.presentation2.screen

// Compose 布局组件
import androidx.compose.foundation.layout.Box            // 层叠布局容器
import androidx.compose.foundation.layout.Column        // 垂直线性布局
import androidx.compose.foundation.layout.fillMaxSize   // 填充父容器全部尺寸
import androidx.compose.foundation.layout.wrapContentHeight // 根据内容自适应高度

// Material 3 主题组件
import androidx.compose.material3.MaterialTheme         // 获取当前主题样式和颜色
import androidx.compose.material3.Surface               // 带有背景色的表面容器

// Compose 运行时
import androidx.compose.runtime.Composable              // 声明 Composable 函数
import androidx.compose.runtime.getValue                // 读取 State 值
import androidx.compose.runtime.mutableStateOf          // 创建可变状态
import androidx.compose.runtime.remember                // 记住状态值避免重组时丢失
import androidx.compose.runtime.setValue                // 修改 State 值

// Compose UI 修饰符
import androidx.compose.ui.Modifier                     // UI 修饰符链

// 项目内部组件
import com.example.travel_footprint_android.presentation2.components.debug.DebugOverlay  // 调试悬浮面板
import com.example.travel_footprint_android.presentation2.navigation.CustomNavHost2      // 自定义导航容器
import com.example.travel_footprint_android.presentation2.navigation.Navigation2         // 底部导航栏
import com.example.travel_footprint_android.utils.DebugHelper                            // 调试工具类

/**
 * 应用主界面 Composable 函数
 *
 * @param debugHelper 调试工具类实例，传入 null 时不显示调试面板（默认不显示）
 */
@Composable
fun MainScreen2(
    debugHelper: DebugHelper? = null
) {
    // 控制闪屏是否显示的布尔状态，初始为 true（显示闪屏）
    var showSplash by remember { mutableStateOf(true) }
    // 控制主界面是否渲染的布尔状态，初始为 false（等待闪屏回调后再显示）
    var showScreen by remember { mutableStateOf(false) }

    // 全屏层叠布局：闪屏层覆盖在主界面层之上
    Box(modifier = Modifier.fillMaxSize()) {
        // 主界面层：当 showScreen 为 true 时才渲染
        if (showScreen) {
            // Surface 提供 Material 主题背景色
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background  // 使用主题背景色
            ) {
                // 垂直布局：上方页面内容区 + 下方底部导航栏
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 导航内容区：占据剩余全部空间（weight = 1f）
                    // 内部根据路由动态展示 LightenScreen2（点亮地图）或 JourneyScreen2（旅程列表）
                    CustomNavHost2(modifier = Modifier.weight(1f))
                    // 底部导航栏：自适应高度，包含"点亮"和"旅程"两个 Tab
                    Navigation2(modifier = Modifier.wrapContentHeight())
                }
                // 调试悬浮面板：仅当传入 debugHelper 时才渲染，覆盖在主界面上方
                if (debugHelper != null) {
                    DebugOverlay(
                        debugHelper = debugHelper,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        // 闪屏动画层：始终在 Box 顶层渲染，动画结束后通过 showSplash = false 移除
        if (showSplash) {
            SplashScreen(
                onFinished = { showSplash = false },     // 闪屏动画结束后回调，移除闪屏层
                onShowScreen = { showScreen = true }     // 闪屏开始播放时回调，触发主界面渲染
            )
        }
    }
}
