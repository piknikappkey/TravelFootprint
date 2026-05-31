/*
 * 文件名：JourneyNavController.kt
 * 包路径：presentation2.components.journey_panel2.viewmodel
 *
 * 【用途】
 * 旅程面板的轻量级导航状态管理器。采用 Kotlin 单例 object 模式，
 * 通过 Compose 的 mutableStateOf 创建可观察状态，管理旅程面板内
 * 各子页面（旅程列表/编辑、足迹列表/编辑）之间的导航切换及数据传递。
 *
 * 它不是传统的 Android ViewModel，而是一个简单的全局状态持有者，
 * 避免了 Navigation Compose 的重型路由体系，适用于同一面板内
 * 不同内容页面的切换场景。
 *
 * 【功能】
 * 1. 面板状态管理：通过 JourneyPanel2State 枚举追踪当前显示的子页面
 * 2. 旅程数据传递：在导航时携带当前操作的 Journey 对象到目标页面
 * 3. 足迹数据传递：在导航时携带当前操作的 Footprint 对象到目标页面
 * 4. 页面导航：提供 navigate() 函数统一完成状态切换和数据绑定
 * 5. 响应式更新：使用 Compose mutableStateOf，状态变化自动触发 UI 重组
 *
 * 【关联组件】
 * - JourneyPanel2   : 使用此控制器管理其子页面切换的主面板组件
 * - JourneyList3    : 读取 journeyNavController 判断是否显示旅程列表
 * - JourneyEdit     : 读取 journeyNavController 和 journeyData 进行编辑
 * - FootprintList   : 读取 journeyNavController 和 journeyData 显示足迹列表
 * - FootprintEdit   : 读取 journeyNavController、journeyData、footprintData 编辑足迹
 * - Journey         : Room 实体，旅程数据模型
 * - Footprint       : Room 实体，足迹数据模型，通过外键关联 Journey
 *
 * 【简单实现逻辑】
 * 1. 定义 JourneyNavController 为 Kotlin object（全局单例），
 *    确保整个应用中只有一份导航状态实例
 * 2. 创建三个私有 mutableStateOf 变量存储内部状态：
 *    - _journeyNavController: 当前页面状态枚举
 *    - _journeyData: 当前操作的旅程数据
 *    - _footprintData: 当前操作的足迹数据
 * 3. 通过公有只读属性（val）暴露状态，外部可读取但无法直接修改
 * 4. navigate() 方法接收目标状态和可选的数据参数，
 *    一次性更新所有状态并触发 Compose 重组
 * 5. 定义 JourneyPanel2State 枚举，列出所有可用的导航目标：
 *    - JOURNEY_LIST: 旅程列表页（默认首页）
 *    - JOURNEY_EDIT: 旅程新增/编辑页
 *    - FOOTPRINT_LIST: 足迹列表页
 *    - FOOTPRINT_EDIT: 足迹新增/编辑页
 *    - 注释掉的 JOURNEY_DETAILS 为预留的旅程详情页
 *
 * 【与 ViewModel 的区别】
 * - 不使用 Jetpack ViewModel / Hilt，避免组件层级过深时
 *   需要逐层传递 ViewModel 实例的麻烦
 * - 使用 Compose 原生 mutableStateOf 替代 LiveData/StateFlow，
 *   无需手动管理生命周期订阅
 * - 缺点：object 单例不会随生命周期自动销毁，需注意内存管理
 */
package com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel

import androidx.compose.runtime.mutableStateOf // Compose 可观察状态容器，变化时触发重组
import com.example.travel_footprint_android.data.entity.Footprint // 足迹 Room 实体（含外键关联 Journey）
import com.example.travel_footprint_android.data.entity.Journey // 旅程 Room 实体

// 全局单例导航控制器，管理旅程面板的子页面切换和数据传递
object JourneyNavController {
    // 私有内部状态：当前面板所在页面（默认显示旅程列表）
    private val _journeyNavController = mutableStateOf(JourneyPanel2State.JOURNEY_LIST)
    // 公有只读属性：对外暴露的可观察页面状态
    val journeyNavController = _journeyNavController

    // 私有内部状态：当前正在编辑/查看的旅程数据（导航时携带）
    private val _journeyData = mutableStateOf<Journey?>(null)
    // 公有只读属性：对外暴露的可观察旅程数据
    val journeyData = _journeyData

    // 私有内部状态：当前正在编辑的足迹数据（导航时携带）
    private val _footprintData = mutableStateOf<Footprint?>(null)
    // 公有只读属性：对外暴露的可观察足迹数据
    val footprintData = _footprintData

    // 统一导航函数：切换到目标页面并携带对应的旅程/足迹数据
    fun navigate(destination: JourneyPanel2State, journeyData: Journey? = null, footprintData: Footprint? = null) {
        _footprintData.value = footprintData // 更新足迹数据（先赋值，确保数据到达时页面已准备就绪）
        _journeyData.value = journeyData // 更新旅程数据
        _journeyNavController.value = destination // 最后切换页面状态，触发 UI 重组
    }
}

// 旅程面板的子页面状态枚举，定义所有可导航的目标
enum class JourneyPanel2State {
    JOURNEY_LIST, // 旅程列表（默认首页，应用启动时首次显示的页面）
//    JOURNEY_DETAILS, // 旅程详情（预留，当前版本暂未使用）
    JOURNEY_EDIT, // 旅程新增/修改（新建旅程或编辑已有旅程）
    FOOTPRINT_LIST, // 足迹列表（查看指定旅程下的所有足迹）
    FOOTPRINT_EDIT, // 足迹新增/修改（在指定旅程下新增或编辑足迹）
}