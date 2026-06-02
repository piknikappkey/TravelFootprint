/*
 * ============================================================================
 * CustomNavController.kt — 自定义导航状态控制器（全局单例）
 * ============================================================================
 *
 * 【用途】
 *   全局唯一的导航状态管理器，管理底部导航栏的页面切换状态。
 *   作为 CustomNavHost2 和 Navigation2 之间的桥梁，以响应式状态驱动
 *   页面路由变化，替代 Jetpack Navigation 实现轻量级路由管理。
 *
 * 【功能】
 *   1. 维护当前路由状态：通过 mutableStateOf 存储当前显示的页面路由
 *      （currentDestination），供 CustomNavHost2 读取并渲染对应页面
 *   2. 提供 navigate() 方法：供 Navigation2 底部 Tab 点击时调用，
 *      切换目标页面并触发 Compose UI 重组
 *   3. 内置防抖机制：记录上次导航时间戳，间隔不足 200ms 的导航请求
 *      被忽略，防止快速连点导致性能问题和视觉闪烁
 *
 * 【关联组件】
 *   - NavPath2（navigation）：导航路径数据类，包含页面名称 name 和
 *     图标资源 ID icon，作为 navigate() 的路由参数类型
 *   - NavPathObj2（navigation）：预置常量对象，定义 lighten（点亮）
 *     和 journey（旅程）两个路由，currentDestination 初始值为 lighten
 *   - CustomNavHost2（navigation）：页面容器组件，监听 currentDestination
 *     变化来切换显示 LightenScreen2 或 JourneyScreen2
 *   - Navigation2（navigation）：底部导航栏 UI 组件，Tab 点击时调用
 *     CustomNavController.navigate() 触发路由切换
 *
 * 【简单实现逻辑】
 *   1. 使用 Kotlin object 实现全局单例，确保导航状态唯一
 *   2. 使用 mutableStateOf 存储当前路由（_currentDestination），
 *      初始化为 NavPathObj2.lighten（点亮页面）
 *   3. 通过 private backing property（_currentDestination）封装可变状态，
 *      对外暴露只读引用（currentDestination）
 *   4. navigate() 接收 NavPath2 类型的目标路由，执行防抖检查：
 *      - 距离上次导航 > 200ms → 更新 lastNavigateTime 和路由
 *      - 否则忽略本次调用
 *   5. 路由变化后，mutableStateOf 自动通知依赖该状态的 Compose 组件
 *      （CustomNavHost2）重组，切换到目标页面
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation2.viewmodel.nav_controller

import androidx.compose.runtime.mutableStateOf
import com.example.travel_footprint_android.presentation2.navigation.NavPath2
import com.example.travel_footprint_android.presentation2.navigation.NavPathObj2

// —— 自定义导航控制器（全局单例） ——
// 管理底部导航栏路由状态，提供带防抖的 navigate() 方法
object CustomNavController {
    // 当前路由的内部可变状态，初始为"点亮"页面。
    // private 防止外部直接修改，必须通过 navigate() 切换
    private val _currentDestination = mutableStateOf(NavPathObj2.journey)
    // 对外公开的只读路由状态，供 CustomNavHost2 等组件监听
    val currentDestination = _currentDestination

    // 上次导航操作的时间戳（毫秒），用于防抖计时
    private var lastNavigateTime = 0L
    // 最小导航间隔 200ms，防止用户快速连点导致页面频繁切换
    private val minInterval = 200L

    // —— 导航方法（含防抖） ——
    // 切换到目标路由，若距上次导航不足 200ms 则忽略本次请求
    fun navigate(destination: NavPath2) {
        val now = System.currentTimeMillis()
        // 防抖检查：只有间隔超过 200ms 才允许切换
        if (now - lastNavigateTime > minInterval) {
            lastNavigateTime = now
            _currentDestination.value = destination
        }
    }
}
