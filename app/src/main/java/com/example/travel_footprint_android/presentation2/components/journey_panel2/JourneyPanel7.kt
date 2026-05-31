package com.example.travel_footprint_android.presentation2.components.journey_panel2

/*
 * ============================================================================
 * JourneyPanel7.kt — 旅程面板主容器组件（第7版）
 * ============================================================================
 *
 * 【用途】
 *   作为地图页面的底部滑动面板，承载旅程相关所有功能的入口和交互界面。
 *   是 FOOTPRINT、JOURNEY 两大业务模块的统一容器，四个子页面在此组件内
 *   以动画切换的形式呈现。
 *
 * 【功能】
 *   1. 底部面板容器：带有拖拽调节高度的交互手柄（顶部小横条），支持垂直拖拽
 *      和点击切换两种方式在 40% 和 60% 屏幕高度之间切换，范围限制在 20%~80%
 *   2. 四状态导航容器：通过 AnimatedContent 根据 JourneyNavController 的状态
 *      枚举（JourneyPanel2State）切换显示四个子页面：
 *        - JOURNEY_LIST   → JourneyList3（旅程列表，含选中/添加/前往足迹入口）
 *        - JOURNEY_EDIT   → JourneyEdit（旅程新增/编辑表单，含保存/删除）
 *        - FOOTPRINT_LIST → FootprintList（足迹列表，选中旅程的足迹展示）
 *        - FOOTPRINT_EDIT → FootprintEdit（足迹新增/编辑表单，含位置和评分）
 *   3. 自定义布局裁剪：外层 Box 使用 custom layout 将内容向上偏移 60dp 并裁剪
 *      超出部分，实现面板从屏幕底部向上延伸的视觉效果
 *   4. 拖拽高度联动：所有子组件共享同一套拖拽状态（isDragging / currentHeightRatio
 *      / onDragDelta / togglePanelHeight），确保面板高度在任何子页面内都能被拖动调整
 *   5. 动画切换：子页面切换时使用 fadeIn/fadeOut + slideInVertically/slideOutVertically
 *      组合动画，动画时长由外部参数 aniTime 控制
 *   6. 背景渲染：使用 BGImgBox 加载背景图片，叠加白色半透明遮罩和阴影效果
 *
 * 【关联组件】
 *   - JourneyNavController（单例对象）：全局导航控制器，维护 currentState、
 *     journeyData、footprintData 三个可变状态，子组件通过 navigate() 切换面板状态
 *   - JourneyPanel2State（枚举）：定义四个面板状态常量（JOURNEY_LIST / JOURNEY_EDIT
 *     / FOOTPRINT_LIST / FOOTPRINT_EDIT）
 *   - JourneyViewModel（Hilt ViewModel）：旅程和足迹的 CRUD 业务逻辑，提供
 *     createJourney / updateJourney / deleteJourney / addFootprintsForJourney
 *     / updateFootprint / deleteFootprint 等数据操作方法，注入 key="journey"
 *   - Journey（Room Entity）：旅程数据实体，字段含 id/title/description/startDate/
 *     endDate/coverStyle/coverImagePath/journeyImagePaths/address/longitude/latitude
 *   - BGImgBox：背景图片容器组件，接收 drawable 资源 ID 列表随机选图，Coil 加载后
 *     以 drawBehind 方式绘制到 Canvas，覆盖半透明白色遮罩
 *   - JourneyList3：旅程列表子页面，展示所有旅程，支持选中、添加、跳转足迹列表
 *   - JourneyEdit：旅程编辑子页面，表单字段含标题/封面/描述/地址/回忆图片
 *   - FootprintList：足迹列表子页面，展示选中旅程的所有足迹，支持展开详情
 *   - FootprintEdit：足迹编辑子页面，含标题/描述/地址/评分等编辑字段
 *
 * 【简单实现逻辑】
 *   1. 从 JourneyNavController 读取当前面板状态和选中的旅程/足迹数据
 *   2. 通过 LocalConfiguration 获取屏幕高度像素值，用于计算拖拽比例
 *   3. currentHeightRatio（默认 0.4f）控制面板高度占屏幕比例
 *   4. isDragging 区分拖拽中（直接使用 currentHeightRatio）和松手后（animateFloatAsState 动画过渡）
 *   5. togglePanelHeight：点击时在 0.4f ↔ 0.6f 之间切换
 *   6. onDragDelta：将拖拽的像素增量转换为比例增量，限制在 [0.2f, 0.8f]
 *   7. 最外层 Box 使用 custom layout 将内容裁剪并向上偏移 60dp
 *   8. 内部包含：拖拽手柄（半透明小横条 + 垂直拖拽手势检测）和带阴影的 BGImgBox 容器
 *   9. BGImgBox 内 Box 的高度 = screenHeightDp * aniJourneyHeight（响应拖拽变化）
 *   10. AnimatedContent 根据 journeyPanel2State 切换四个子页面，带入数据操作回调
 * ============================================================================
 */

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_edit.FootprintEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list.FootprintList
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.JourneyEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.JourneyList3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.FOOTPRINT_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.FOOTPRINT_LIST
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_LIST


// 消除"使用 screenHeightDp 替代 screenHeight"的 lint 警告
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel7(
    // Modifier 用于外部控制面板的位置和尺寸
    modifier: Modifier = Modifier,
    // 所有旅程列表数据，来自外部 ViewModel 的 Flow 收集
    journeyList: List<Journey>,
    // 动画时长（毫秒），用于子页面切换时的过渡动画速度
    aniTime: Int,
    // 旅程业务 ViewModel，使用 key="journey" 确保同一 Hilt Scope 内获取同一实例
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"),
) {
    // 从全局 JourneyNavController 单例读取当前面板的导航状态
    val journeyPanel2State = JourneyNavController.journeyNavController.value
    // 当前选中的旅程数据（可能在 JOURNEY_LIST 中被选中）
    val journeySelected = JourneyNavController.journeyData.value
    // 当前选中的足迹数据（可能在 FOOTPRINT_LIST 中被选中）
    val footprintSelected = JourneyNavController.footprintData.value

    // 获取设备屏幕配置信息
    val configuration = LocalConfiguration.current
    // 计算屏幕密度：densityDpi / 160 = 标准密度因子（1.0 = mdpi, 2.0 = xhdpi 等）
    val density = configuration.densityDpi.toFloat() / 160f
    // 屏幕物理像素高度 = dp 高度 × 密度因子
    val screenHeightPixels = configuration.screenHeightDp * density

    // 当前面板高度占屏幕比例，默认 40%
    var currentHeightRatio by remember { mutableFloatStateOf(0.4f) }
    // 是否正在拖拽中（拖拽时跳过动画，直接跟随手指）
    var isDragging by remember { mutableStateOf(false) }

    // 动画驱动的高度比例：拖拽中直接响应手指位置，松手后通过动画平滑过渡到目标值
    val aniJourneyHeight = if (isDragging) {
        currentHeightRatio
    } else {
        animateFloatAsState(
            targetValue = currentHeightRatio,
            animationSpec = tween(durationMillis = 300),
            label = "journeyPanelHeight"
        ).value
    }

    // 点击切换面板高度：< 0.5 时展开至 60%，≥ 0.5 时收起至 40%（拖拽中不响应）
    val togglePanelHeight = { _: Boolean ->
        if (!isDragging) {
            currentHeightRatio = if (currentHeightRatio < 0.5f) 0.6f else 0.4f
        }
    }

    // 拖拽位移回调：将垂直像素增量转为比例增量，限制在 [20%, 80%] 之间
    val onDragDelta = { deltaY: Float ->
        val ratioDelta = -deltaY / screenHeightPixels
        currentHeightRatio = (currentHeightRatio + ratioDelta).coerceIn(0.2f, 0.8f)
    }

    // 最外层容器：自定义 layout 将内容向上偏移 60dp 并裁剪超出部分，实现面板从底部上浮的视觉效果
    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                // 测量原本的内容
                val placeable = measurable.measure(constraints)
                val offsetPx = 60.dp.roundToPx()

                // 布局高度 = 原高度 - 偏移量（最小为 0）
                val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)

                layout(placeable.width, layoutHeight) {
                    // 把内容放到向上偏移的位置
                    placeable.placeRelative(0, -offsetPx)
                }
            }
    ) {
        // ===== 拖拽手柄区域：顶部半透明小横条 + 垂直拖拽手势检测 =====
        // 控制面板高度组件
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .background(Color.Transparent)
                // 垂直拖拽手势：开始拖拽时标记 isDragging，拖拽中调用 onDragDelta，结束时取消标记
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                        onDragEnd = { isDragging = false }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // 拖拽指示条：宽 28dp、高 4dp 的半透明黑色圆角横条
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(4.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
            )
        }

        // ===== 主内容容器：带阴影和背景图的面板主体，高度由 aniJourneyHeight 动态控制 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // 顶部圆角 + 阴影：上方为圆角，下方平直贴合屏幕边缘
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                    clip = true
                )
        ) {
            // 背景图片容器：随机选图 + Coil 加载 + Canvas 绘制，覆盖半透明白色遮罩
            BGImgBox(listOf(R.drawable.bg_rectangular_1__3__0),) {
                // 内容高度 = 屏幕 dp 高度 × 动画驱动的高度比例
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(configuration.screenHeightDp.dp * aniJourneyHeight)
                ) {
                    // ===== 四状态动画内容切换容器 =====
                    AnimatedContent(
                        modifier = Modifier.clip(shape = RoundedCornerShape(0.dp))
                            .align(Alignment.BottomCenter),
                        // targetState 为 JourneyNavController 的当前状态枚举值
                        targetState = journeyPanel2State,
                        // 入场：淡入 + 从下方 1/4 高度滑入；出场：淡出 + 向上方 1/4 高度滑出
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(durationMillis = aniTime)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 4 },
                                        animationSpec = tween(durationMillis = aniTime)
                                    )) togetherWith
                                    (fadeOut(animationSpec = tween(durationMillis = aniTime)) +
                                            slideOutVertically(
                                                targetOffsetY = { -it / 4 },
                                                animationSpec = tween(durationMillis = aniTime)
                                            ))
                        },
                        label = "vertical_slide_fade_animation"
                    ) { state ->
                        when (state) {
                            // ========== 旅程列表页面 ==========
                            JOURNEY_LIST -> {
                                JourneyList3(
                                    journeyList = journeyList,
                                    updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                    journeySelected = journeySelected,
                                    // 传递面板展开/收起状态（> 0.5 为展开态）
                                    currentHeightRatio > 0.5f,
                                    togglePanelHeight,
                                    { b -> isDragging = b },
                                    onDragDelta
                                )
                            }

                            // ========== 旅程编辑页面（新增/修改） ==========
                            JOURNEY_EDIT -> {
                                JourneyEdit(
//                                    modifier = Modifier.weight(1f),
                                    journeySelected = journeySelected,
                                    // 导航回调：子组件通过此 lambda 请求切换到指定状态
                                    navigate = { state, journey ->
                                        JourneyNavController.navigate(
                                            state,
                                            journey
                                        )
                                    },
                                    addJourney = { j -> journeyViewModel.createJourney(j) },
                                    updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                    deleteJourney = { j -> journeyViewModel.deleteJourney(j) },
                                    journeyPanelHeightState = currentHeightRatio > 0.5f,
                                    setJourneyPanelHeightState = togglePanelHeight,
                                    setIsDragging = { b -> isDragging = b },
                                    onDragDelta = onDragDelta,
                                )
                            }

                            // ========== 足迹列表页面 ==========
                            FOOTPRINT_LIST -> {
                                // 仅当有选中的旅程时才渲染足迹列表
                                journeySelected?.let {
                                    FootprintList(
                                        it,
                                        currentHeightRatio > 0.5f,
                                        togglePanelHeight,
                                        setIsDragging = { b -> isDragging = b },
                                        onDragDelta = onDragDelta,
                                    )
                                }
                            }

                            // ========== 足迹编辑页面（新增/修改） ==========
                            FOOTPRINT_EDIT -> {
                                // 仅当有选中的旅程时才渲染足迹编辑表单
                                journeySelected?.let {
                                    FootprintEdit(
                                        footprintSelected = footprintSelected,
                                        journeySelected = it,
                                        // 添加足迹：将 Journey + Footprint 对象传给 ViewModel
                                        addFootprint = { j, f ->
                                            journeyViewModel.addFootprintsForJourney(
                                                journey = j,
                                                footprint = f
                                            )
                                        },
                                        // 更新足迹：传入 copy() 副本避免直接修改原始数据
                                        updateFootprint = { f ->
                                            journeyViewModel.updateFootprint(f.copy())
                                        },
                                        // 删除足迹：传入 copy() 副本
                                        deleteFootprint = { f ->
                                            journeyViewModel.deleteFootprint(f.copy())
                                        },
                                        currentHeightRatio > 0.5f,
                                        togglePanelHeight,
                                        setIsDragging = { b -> isDragging = b },
                                        onDragDelta = onDragDelta,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}