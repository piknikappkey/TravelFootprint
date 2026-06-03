package com.example.travel_footprint_android.presentation.navigation

/*
 * ============================================================================
 * NavPath2.kt — 导航路径数据模型（底部导航栏专用）
 * ============================================================================
 *
 * 【用途】
 *   定义底部导航栏中每个导航项的数据结构及预置常量。
 *   作为 CustomNavController 和 Navigation2 之间的标准路由描述单位。
 *
 * 【功能】
 *   1. NavPath2 数据类：每个导航项包含显示名称（name）和图标资源 ID（icon）
 *   2. 拷贝构造函数：支持通过现有 NavPath2 实例创建副本
 *   3. NavPathObj2 常量对象：预定义"点亮"和"旅程"两个导航项，
 *      并提供 list 列表供 Navigation2 遍历渲染
 *
 * 【关联组件】
 *   - Navigation2（同包）：底部导航栏 UI 组件，遍历 NavPathObj2.list
 *     生成等宽 NavItem，点击时调用 CustomNavController.navigate(navPath)
 *   - CustomNavController（viewmodel.nav_controller）：全局单例导航控制器，
 *     管理当前路由状态（currentDestination），初始值为 NavPathObj2.lighten，
 *     并提供 navigate() 方法（含 200ms 防抖）
 *   - CustomNavHost2（同包）：页面容器，监听 currentDestination 变化，
 *     切换显示 LightenScreen2 或 JourneyScreen2
 *   - ic_journey_nav_item / ic_light_nav_item（R.drawable）：导航图标资源
 *
 * 【简单实现逻辑】
 *   1. NavPath2 作为数据载体，存储单个导航项的名称和图标 ID
 *   2. NavPathObj2 预置两个导航项实例——lighten（点亮）和 journey（旅程）
 *   3. CustomNavController 初始化时将 currentDestination 设为 lighten
 *   4. Navigation2 遍历 list 渲染底部导航栏，被选中的项触发动画效果
 *   5. 用户点击 Tab → CustomNavController.navigate(navPath)
 *      → 防抖通过后更新 currentDestination → 触发 CustomNavHost2 重组切换页面
 * ============================================================================
 */

import com.example.travel_footprint_android.R

// —— 导航路径数据类 ——
// 底部导航栏中每一项的数据模型，包含显示名称和图标资源 ID
data class NavPath(
    // 导航项显示名称（如"点亮"、"旅程"）
    val name: String,
    // 导航图标在 R.drawable 中的资源 ID
    val icon: Int,
) {
    // 拷贝构造函数：通过现有 NavPath2 实例创建新实例
    constructor(navPath: NavPath) : this(navPath.name, navPath.icon)
}

// —— 导航路径常量对象 ——
// 预定义底部导航栏的所有导航项及其列表
object NavPathObj {
    // "点亮"页面导航项，使用旅程图标（ic_journey_nav_item）
    val lighten = NavPath("点亮", R.drawable.ic_light_nav_item)
    // "旅程"页面导航项，使用点亮图标（ic_light_nav_item）
    val journey = NavPath("旅程", R.drawable.ic_journey_nav_item)
    // "我的"页面导航项
    val my = NavPath("我的", R.drawable.ic_my)
    // 导航项列表，按显示顺序排列，供 Navigation2 遍历渲染
    val list = listOf(lighten, journey, my)
}