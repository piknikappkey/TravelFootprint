package com.example.travel_footprint_android.presentation.components.journey_panel

/**
 * ====================================================================
 * JourneyPanel — 旅程底部面板根组件
 * ====================================================================
 *
 * 【用途】
 *  - 作为整个旅程功能的底部弹出面板容器，管理旅程/足迹的完整交互流程
 *  - 是 presentation 层中最核心的面板容器，承载所有旅程相关子页面的切换与路由
 *
 * 【功能】
 *  1. 面板高度可拖拽：通过 DragHandle 检测垂直拖拽手势，面板高度在 20%~70% 屏幕高度间动态调整
 *  2. 5 个页面状态切换：JOURNEY_LIST / JOURNEY_DETAIL / JOURNEY_EDIT / FOOTPRINT_LIST / FOOTPRINT_EDIT
 *  3. 页面切换动画：使用 AnimatedContent 实现渐变(fadeIn/fadeOut) + 垂直滑动(slideInVertically/slideOutVertically)
 *  4. 面板外观：顶部圆角 + 阴影(elevation=2.dp) + 随机背景纹理(BGImgBox) + 拖拽手柄指示条
 *  5. 数据联动：通过 JourneyViewModel 管理旅程和足迹数据的增删改查，Jetpack Room 持久化
 *
 * 【关联组件】
 *  - JourneyPanelState(数据类) : 面板状态容器，包含 currentPage(JourneyPanel2State)、selectedJourney、selectedFootprint
 *  - JourneyPanel2State(枚举)  : 面板页面状态枚举，5 种取值(列表/详情/编辑旅程 + 列表/编辑足迹)
 *  - JourneyViewModel(Hilt)    : 核心 ViewModel，管理旅程/足迹的 Room 数据库操作和 UI 状态流(StateFlow)
 *  - BGImgBox                 : 背景图片容器，为面板内容区提供随机背景纹理(Coil 异步加载，半透明白色遮罩)
 *  - DragHandle               : 拖拽手柄组件，28×4dp 灰色横条 + detectVerticalDragGestures 手势检测
 *  - JourneyList/JourneyDetail/JourneyEdit/FootprintList/FootprintEdit : 5 个子页面组件
 *  - Journey/Footprint(Room Entity) : 数据库实体类，分别对应 journeys/footprints 表
 *
 * 【简单实现逻辑】
 *  1. 状态读取：通过 panelState.currentPage / selectedJourney / selectedFootprint 获取当前面板状态
 *  2. 屏幕适配：LocalConfiguration 获取 density 和 screenHeightDp，用于拖拽像素→比例换算
 *  3. 高度管理：currentHeightRatio(初始 0.4, 范围 0.2~0.7) 记录面板高度比例
 *     - 拖拽中(isDragging=true)直接跟随手指，实时更新 currentHeightRatio
 *     - 松手后通过 animateFloatAsState 以 300ms tween 动画平滑过渡到目标值
 *  4. 布局技巧：外层 Box 通过 layout 修饰符向上偏移 60.dp，遮挡高德地图 SDK 的 logo
 *  5. 拖拽区域：DragHandle 组件(32.dp 高透明区域 + 28×4dp 灰色指示条)，detectVerticalDragGestures 检测手势
 *  6. 内容容器：shadow(2.dp) + 顶部 RoundedCornerShape(12.dp) + BGImgBox 背景 + 动态高度(screenHeightDp * aniJourneyHeight)
 *  7. 页面路由：AnimatedContent 以 journeyPanel2State 为 targetState，when 分支匹配 5 种状态渲染对应子组件
 *  8. 数据传递：各子组件通过 onPanelNavigate / addJourney / updateJourney / addFootprint 等回调与 JourneyPanel 和 ViewModel 通信
 *  9. 高度切换：togglePanelHeight 在 0.4 和 0.6 之间切换，供子页面的展开/收缩按钮调用
 * ====================================================================
 */


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.journey_map.location_button.LocationButton
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.FootprintEdit
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_list.FootprintList
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail.JourneyDetail
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.JourneyEdit
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list.JourneyList
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.FOOTPRINT_EDIT
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.FOOTPRINT_LIST
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.JOURNEY_DETAIL
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.JOURNEY_EDIT
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.JOURNEY_LIST
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanelState
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel

// =========================================================================
// JourneyPanel Composable: 旅程底部面板根组件，管理 Y 轴偏移拖拽 + 5 个子页面路由
// =========================================================================
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel(
    modifier: Modifier,                                        // 外部 Modifier，用于整体布局定位
    aniTime: Int,                                              // 页面切换动画时长(毫秒)，控制 fadeIn/slideIn 过渡速度
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"), // Hilt 注入的 JourneyViewModel，管理旅程/足迹数据
) {
    // ===== 1. 从 JourneyViewModel 收集 UI 状态 =====
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val journeyList = journeyUiState.journeys

    // ===== 面板状态管理（内部状态，无需外部传入） =====
    var panelState by remember { mutableStateOf(JourneyPanelState()) }
    val onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit = { page, journey, footprint ->
        panelState = JourneyPanelState(
            currentPage = page,
            selectedJourney = journey,
            selectedFootprint = footprint,
        )
    }

    // ===== 2. 从 panelState 解构当前页面与选中数据 =====
    val journeyPanel2State = panelState.currentPage           // 当前面板页面状态枚举
    val journeySelected = panelState.selectedJourney           // 当前选中的旅程
    val footprintSelected = panelState.selectedFootprint       // 当前选中的足迹

    // ===== 3. 屏幕参数计算 =====
    val configuration = LocalConfiguration.current
    val density = remember { configuration.densityDpi.toFloat() / 160f }
    val screenWidthPx = remember { (configuration.screenWidthDp * density).toInt() }
    val screenHeightPx = remember { (configuration.screenHeightDp * density).toInt() }

    // ===== 4. 面板 Y 轴偏移量管理 =====
    // targetOffsetY：面板顶部距离屏幕顶部的像素偏移，初始 60%(屏幕高度 * 0.6)，范围 10%~80%
    var targetOffsetY by remember { mutableIntStateOf((screenHeightPx * 0.6f).toInt()) }
    // isDragging：拖拽中标记
    var isDragging by remember { mutableStateOf(false) }

    // ===== 5. 面板偏移量动画值 =====
    // 拖拽中：直接使用实时偏移量，无动画延迟，手指跟随精准
    // 松手后：通过 animateIntAsState 以 300ms tween 动画平滑过渡到目标值
    val aniOffsetY = if (isDragging) {
        targetOffsetY
    } else {
        animateIntAsState(
            targetValue = targetOffsetY,
            animationSpec = tween(durationMillis = 300),
            label = "journeyPanelOffset"
        ).value
    }

    // ===== 5b. 将面板偏移写入 JourneyMapViewModel（供初始自动定位补偿使用） =====
    val journeyMapViewModel: JourneyMapViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as androidx.activity.ComponentActivity,
        key = "JourneyMap3"
    )
    LaunchedEffect(screenWidthPx, screenHeightPx, aniOffsetY) {
        journeyMapViewModel.setPanelOffset(screenWidthPx, screenHeightPx, aniOffsetY)
    }

    // ===== 6. 面板位置切换回调（供子页面按钮调用） =====
    // 在 40% 和 60% 两个档位之间切换
    val togglePanelOffset = { _: Boolean ->
        if (!isDragging) {
            targetOffsetY = if (targetOffsetY > screenHeightPx * 0.5f) {
                (screenHeightPx * 0.4f).toInt()
            } else {
                (screenHeightPx * 0.6f).toInt()
            }
        }
    }

    // ===== 7. 拖拽位移 → Y 轴偏移量转换回调 =====
    val onDragDelta = { deltaY: Float ->
        targetOffsetY = (targetOffsetY + deltaY.toInt())
            .coerceIn(
                (screenHeightPx * 0.1f).toInt(),  // 最小偏移：面板底部露出 90%
                (screenHeightPx * 0.8f).toInt()   // 最大偏移：面板底部露出 20%
            )
    }

    // ===== 8. 内容容器：阴影 + 顶部圆角 + 可视高度 + 锚定底部 =====
    // 使用 layout 修饰符确保面板内容区域始终锚定在屏幕底部
    // 且高度 = 屏幕高度 - 偏移量，避免内容超出屏幕不可见
    val visiblePanelHeight = (screenHeightPx - if (isDragging) targetOffsetY else aniOffsetY).coerceAtLeast(0)
    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val h = visiblePanelHeight
                val placeable = measurable.measure(
                    constraints.copy(
                        minHeight = h,
                        maxHeight = h
                    )
                )
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeRelative(0, constraints.maxHeight - h)
                }
            }
    ) {
        // ===== 9a. 拖拽手柄：位于面板顶部 =====
        DragHandle(
            onDragStart = { isDragging = true },
            onDragEnd = { isDragging = false },
            onDragDelta = onDragDelta,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // ===== 9b. 定位按钮：固定在面板右上角上方 100dp 位置 =====
        LocationButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-60).dp),
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            panelTopY = aniOffsetY,
        )

        // ===== 9c. 内容容器：阴影 + 顶部圆角 + 全屏高度 + 背景纹理 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                    clip = true
                )
                .fillMaxSize()
        ) {
            // ===== 9c. 随机背景纹理容器 =====
            BGImgBox(R.drawable.bg_rectangular_1__2__0) {
                // ===== 9d. AnimatedContent：页面切换动画控制器 =====
                AnimatedContent(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(0.dp))
                        .align(Alignment.BottomCenter),
                    targetState = journeyPanel2State,
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
                                        )) using SizeTransform(
                                    clip = false,
                                )
                    },
                    label = "vertical_slide_fade_animation"
                ) { state ->
                    // ===== 10. 根据面板状态渲染对应子页面 =====
                    when (state) {
                        JOURNEY_LIST -> {
                            JourneyList(
                                journeyList = journeyList,
                                journeyPanelExpandedState = targetOffsetY <= screenHeightPx * 0.5f,
                                setJourneyPanelOffset = togglePanelOffset,
                                setIsDragging = { b -> isDragging = b },
                                onDragDelta = onDragDelta,
                                onPanelNavigate = onPanelNavigate,
                            )
                        }

                        JOURNEY_DETAIL -> {
                            JourneyDetail(
                                journeySelected = journeySelected,
                                updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                journeyPanelExpandedState = targetOffsetY <= screenHeightPx * 0.5f,
                                setJourneyPanelOffset = togglePanelOffset,
                                setIsDragging = { b -> isDragging = b },
                                onDragDelta = onDragDelta,
                                onPanelNavigate = onPanelNavigate,
                            )
                        }

                        JOURNEY_EDIT -> {
                            JourneyEdit(
                                journeySelected = journeySelected,
                                navigate = { state, journey ->
                                    onPanelNavigate(state, journey, null)
                                },
                                addJourney = { j -> journeyViewModel.createJourney(j) },
                                updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                deleteJourney = { j -> journeyViewModel.deleteJourney(j) },
                                journeyPanelExpandedState = targetOffsetY <= screenHeightPx * 0.5f,
                                setJourneyPanelOffset = togglePanelOffset,
                                setIsDragging = { b -> isDragging = b },
                                onDragDelta = onDragDelta,
                            )
                        }

                        FOOTPRINT_LIST -> {
                            journeySelected?.let {
                                FootprintList(
                                    it,
                                    targetOffsetY <= screenHeightPx * 0.5f,
                                    togglePanelOffset,
                                    setIsDragging = { b -> isDragging = b },
                                    onDragDelta = onDragDelta,
                                    onPanelNavigate = onPanelNavigate,
                                )
                            }
                        }

                        FOOTPRINT_EDIT -> {
                            journeySelected?.let {
                                FootprintEdit(
                                    footprintSelected = footprintSelected,
                                    journeySelected = it,
                                    addFootprint = { j, f ->
                                        journeyViewModel.addFootprintsForJourney(
                                            journey = j,
                                            footprint = f
                                        )
                                    },
                                    updateFootprint = { f ->
                                        journeyViewModel.updateFootprint(f.copy())
                                    },
                                    deleteFootprint = { f ->
                                        journeyViewModel.deleteFootprint(f.copy())
                                    },
                                    targetOffsetY <= screenHeightPx * 0.5f,
                                    togglePanelOffset,
                                    setIsDragging = { b -> isDragging = b },
                                    onDragDelta = onDragDelta,
                                    onPanelNavigate = onPanelNavigate,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
