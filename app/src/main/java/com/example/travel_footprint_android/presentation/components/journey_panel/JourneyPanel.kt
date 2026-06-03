package com.example.travel_footprint_android.presentation.components.journey_panel

/**
 * 旅程面板根组件
 *
 * 用途：
 * - 作为整个旅程功能的底部面板容器，管理旅程列表、详情、编辑及足迹的完整交互流程
 * - 是 presentation 层级中最核心的面板容器，承载了所有旅程相关子页面的切换与路由
 *
 * 功能：
 * - 面板高度可拖拽：通过顶部拖拽手柄和标题栏的垂直拖拽手势，面板高度在 20%~80% 屏幕高度之间动态调整
 * - 5 个页面状态切换：通过 AnimatedContent 实现 JOURNEY_LIST / JOURNEY_DETAIL / JOURNEY_EDIT / FOOTPRINT_LIST / FOOTPRINT_EDIT 之间的过渡动画
 * - 页面切换动画：渐变 + 垂直滑动动画，通过 aniTime 参数控制动画时长
 * - 面板外观：顶部圆角、阴影、随机背景图、拖拽手柄指示条
 * - 数据联动：通过 JourneyViewModel 管理旅程和足迹数据的增删改查
 *
 * 关联组件：
 * - JourneyPanelState(数据类): 面板状态容器，包含 currentPage(JourneyPanel2State)、selectedJourney、selectedFootprint
 * - JourneyPanel2State(枚举): 面板页面状态，包含 JOURNEY_LIST / JOURNEY_DETAIL / JOURNEY_EDIT / FOOTPRINT_LIST / FOOTPRINT_EDIT
 * - JourneyViewModel(Hilt): 核心 ViewModel，管理旅程/足迹的数据库操作和 UI 状态
 * - BGImgBox: 背景图片容器，为面板内容区域提供随机背景纹理
 * - JourneyList / JourneyDetail / JourneyEdit / FootprintList / FootprintEdit: 各子页面组件
 *
 * 实现逻辑：
 * - 状态读取：通过 panelState.currentPage / selectedJourney / selectedFootprint 获取当前面板状态
 * - 屏幕适配：通过 LocalConfiguration 获取屏幕 density 和像素高度，用于手势计算
 * - 高度管理：currentHeightRatio(0.2~0.8) 记录面板高度比例，拖拽时直接修改，松手后 animateFloatAsState 平滑过渡
 * - 布局技巧：外层 Box 通过 layout 修饰符向上偏移 60.dp，使面板内容从视觉上从底部弹出
 * - 手势区域：一个 32.dp 高的透明 Box + 28×4dp 灰色小横条作为拖拽手柄，detectVerticalDragGestures 检测手势
 * - 内容容器：使用 shadow + 顶部圆角 + BGImgBox 背景 + 动态高度(screenHeightDp * aniJourneyHeight)
 * - 页面路由：AnimatedContent 的 targetState = journeyPanel2State，when 分支匹配 5 种状态渲染对应子组件
 * - 数据传递：各子组件通过 onPanelNavigate / addJourney / updateJourney 等回调与 JourneyPanel 和 ViewModel 通信
 */

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
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


// 旅程面板根组件：管理底部弹出面板的拖拽、高度变化和 5 个子页面切换
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel(
    modifier: Modifier = Modifier, // 外部 Modifier，用于整体布局
    aniTime: Int, // 页面切换动画时长（毫秒），控制 fadeIn/slideIn 等过渡速度
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"), // 旅程/足迹数据的 ViewModel，Hilt 注入
    panelState: JourneyPanelState, // 面板状态容器（当前页面 + 选中旅程 + 选中足迹）
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit, // 面板导航回调，切换页面时携带数据
) {
    // 从 ViewModel 收集 UI 状态，获取旅程列表数据
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val journeyList = journeyUiState.journeys

    // 从 panelState 中解构当前页面状态和选中的旅程/足迹
    val journeyPanel2State = panelState.currentPage // 当前面板页面状态
    val journeySelected = panelState.selectedJourney // 当前选中的旅程（用于详情/编辑/足迹列表）
    val footprintSelected = panelState.selectedFootprint // 当前选中的足迹（用于足迹编辑）

    // 屏幕参数：用于将拖拽像素位移转换为面板高度比例变化
    val configuration = LocalConfiguration.current
    val density = remember { configuration.densityDpi.toFloat() / 160f } // 屏幕密度（dp → px 转换）
    val screenHeightPixels = remember { configuration.screenHeightDp * density } // 屏幕像素高度

    // 面板高度比例状态：0.2 ~ 0.8，0.4 为初始值
    var currentHeightRatio by remember { mutableFloatStateOf(0.4f) }
    // 拖拽状态标记：为 true 时禁用动画，实现实时跟随手指
    var isDragging by remember { mutableStateOf(false) }

    // 面板高度比例动画值：拖拽中实时更新，松手后以 300ms 动画过渡
    val aniJourneyHeight = if (isDragging) {
        currentHeightRatio
    } else {
        animateFloatAsState(
            targetValue = currentHeightRatio,
            animationSpec = tween(durationMillis = 300),
            label = "journeyPanelHeight"
        ).value
    }

    // 面板高度切换回调：在 0.4 和 0.6 之间切换（点击高度切换按钮时调用）
    val togglePanelHeight = { _: Boolean ->
        if (!isDragging) {
            currentHeightRatio = if (currentHeightRatio < 0.5f) 0.6f else 0.4f
        }
    }

    // 拖拽位移转比例回调：将拖拽像素差转为高度比例变化，并限制在 0.2~0.8 之间
    val onDragDelta = { deltaY: Float ->
        val ratioDelta = -deltaY / screenHeightPixels
        currentHeightRatio = (currentHeightRatio + ratioDelta).coerceIn(0.2f, 0.8f)
    }

    // 外层容器：通过 layout 修饰符向上偏移 60.dp，遮挡JourneyMap高德地图logo
    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val offsetPx = 60.dp.roundToPx()
                val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)
                layout(placeable.width, layoutHeight) {
                    placeable.placeRelative(0, -offsetPx)
                }
            }
    ) {
        // 拖拽手柄区域：32.dp 高的透明区域，覆盖整个面板顶部宽度，检测垂直拖拽手势
        DragHandle(
            onDragStart = { isDragging = true },
            onDragEnd = { isDragging = false },
            onDragDelta = onDragDelta,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 内容容器：顶部圆角阴影 + 随机背景图 + 动态面板高度
        // 动态高度容器：根据拖拽比例计算面板实际高度
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                    clip = true
                )
                .height(configuration.screenHeightDp.dp * aniJourneyHeight)
        ) {
            // 随机背景图容器，提供装饰性纹理
            BGImgBox(R.drawable.bg_rectangular_1__2__0) {
                // 页面切换动画容器：渐变 + 垂直滑动，targetState 变化时触发过渡
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
                    // 根据面板状态渲染对应子页面
                    when (state) {
                        // ====== 旅程列表页 ======
                        JOURNEY_LIST -> {
                            JourneyList(
                                journeyList = journeyList,
                                journeyPanelHeightState = currentHeightRatio > 0.5f,
                                setJourneyPanelHeightState = togglePanelHeight,
                                setIsDragging = { b -> isDragging = b },
                                onDragDelta = onDragDelta,
                                onPanelNavigate = onPanelNavigate,
                            )
                        }

                        // ====== 旅程详情页 ======
                        JOURNEY_DETAIL -> {
                            JourneyDetail(
                                journey = journeySelected,
                                updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                journeyPanelHeightState = currentHeightRatio > 0.5f,
                                setJourneyPanelHeightState = togglePanelHeight,
                                setIsDragging = { b -> isDragging = b },
                                onDragDelta = onDragDelta,
                                onPanelNavigate = onPanelNavigate,
                            )
                        }

                        // ====== 旅程新增/编辑页 ======
                        JOURNEY_EDIT -> {
                            JourneyEdit(
                                journeySelected = journeySelected,
                                navigate = { state, journey ->
                                    onPanelNavigate(state, journey, null)
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

                        // ====== 足迹列表页 ======
                        FOOTPRINT_LIST -> {
                            journeySelected?.let {
                                FootprintList(
                                    it,
                                    currentHeightRatio > 0.5f,
                                    togglePanelHeight,
                                    setIsDragging = { b -> isDragging = b },
                                    onDragDelta = onDragDelta,
                                    onPanelNavigate = onPanelNavigate,
                                )
                            }
                        }

                        // ====== 足迹新增/编辑页 ======
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
                                    currentHeightRatio > 0.5f,
                                    togglePanelHeight,
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
