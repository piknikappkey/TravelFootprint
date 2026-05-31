package com.example.travel_footprint_android.presentation2.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import com.example.travel_footprint_android.presentation2.screen.JourneyScreen2
import com.example.travel_footprint_android.presentation2.screen.LightenScreen2
import com.example.travel_footprint_android.presentation2.viewmodel.nav_controller.CustomNavController
import com.example.travel_footprint_android.ui.theme.BGLight0

/**
 * 自定义导航容器组件
 * 
 * 用途：
 * - 作为应用的主页面导航容器，替代 Jetpack Navigation 的标准 NavHost
 * - 在"点亮"页面（LightenScreen2）和"旅程"页面（JourneyScreen2）之间进行切换
 * - 提供页面切换时的白板遮罩动画过渡效果
 * 
 * 功能：
 * - 监听 CustomNavController.currentDestination 状态变化实现页面切换
 * - 使用白板遮罩动画隐藏页面切换过程，避免页面切换时的闪烁
 * - 通过 zIndex 和 alpha 动画控制遮罩层的显示和隐藏
 * 
 * 关联组件：
 * - CustomNavController：单例对象，管理当前导航目的地状态，内置 200ms 防抖机制
 * - NavPathObj2：导航路径常量对象，包含 lighten（点亮）和 journey（旅程）两个页面路径
 * - LightenScreen2：点亮页面，显示 SVG 中国地图和底部可拖拽面板，支持省份/城市点亮功能
 * - JourneyScreen2：旅程页面，显示高德地图和旅程列表面板，带有图片雨动画特效
 * - BGLight0：主题颜色，纯白色背景（#FFFFFFFF）
 * 
 * 简单实现逻辑：
 * 1. 读取 CustomNavController.currentDestination 获取当前目标页面
 * 2. 使用 currentDestOld 变量暂存旧的目标页面，确保页面切换完成后再更新
 * 3. 创建两个 Animatable：aniAlpha（透明度动画）和 aniZIndex（层级动画）
 * 4. LaunchedEffect 监听 currentDest 变化：
 *    - 将遮罩层 zIndex 提升到 10，确保遮罩层在所有页面之上
 *    - 快速将 alpha 设为 1f（显示白板遮挡）
 *    - 更新 currentDestOld 实现页面切换
 *    - 将 alpha 动画过渡到 0f（白板淡出）
 *    - 将 zIndex 降回 0，移除遮罩层
 * 5. Box 容器中根据 currentDestOld 的值使用 when 表达式渲染对应页面
 */
@Composable
fun CustomNavHost2(
    modifier: Modifier = Modifier
) {
    // 获取当前导航目的地（由 CustomNavController 管理的全局状态）
    val currentDest = CustomNavController.currentDestination.value
    // 暂存旧的目的地，用于在动画完成后才真正切换页面
    var currentDestOld by remember { mutableStateOf(currentDest) }

    // 白板动画：控制遮罩层的透明度（1f 为完全不透明，0f 为完全透明）
    val aniAlpha = remember { Animatable(1f) }
    // 白板动画：控制遮罩层的 z-index 层级（10f 为最高层，0f 为普通层）
    val aniZIndex = remember { Animatable(0f) }

    // 监听导航目的地变化，触发白板遮罩动画
    LaunchedEffect(currentDest) {
        if(currentDest != currentDestOld) {
            aniZIndex.snapTo(10f) // 调出白板用于遮挡页面
            aniAlpha.animateTo(1f, animationSpec = tween(10)) // 调整透明度
            currentDestOld = currentDest // 切换页面
            aniAlpha.animateTo(0f, animationSpec = tween(150)) // 调回透明度
            aniZIndex.snapTo(0f) // 移除白板
        }
    }


    // 主容器：白色背景的 Box，包含遮罩层和页面内容
    Box(
        modifier = modifier
            .background(BGLight0)
    ) {
        // 白板遮罩层：通过 zIndex 和 alpha 控制显示层级和透明度
        // 空 Surface 用于在页面切换时遮挡底层内容，避免闪烁
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(aniZIndex.value)
                .alpha(aniAlpha.value)
        ) {}
        // 根据当前导航目的地渲染对应页面
        when (currentDestOld) {
            NavPathObj2.lighten -> LightenScreen2()
            NavPathObj2.journey -> JourneyScreen2()
        }
    }
}
