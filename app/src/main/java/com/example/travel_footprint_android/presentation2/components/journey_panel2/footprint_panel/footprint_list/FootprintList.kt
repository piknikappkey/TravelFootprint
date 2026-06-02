package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list

/**
 * FootprintList - 足迹列表组件
 *
 * 【用途】
 *  - 在旅程面板中显示某个旅程下的所有足迹列表，是 FOOTPRINT_LIST 状态的视图层实现
 *  - 支持拖拽操作以实现面板高度调整，提供添加和编辑足迹的入口
 *
 * 【功能】
 *  - 1. 足迹列表展示：使用 LazyColumn 展示当前旅程的所有足迹，每项支持点击展开详情
 *  - 2. 空状态提示：当足迹列表为空时，显示友好提示文字引导用户添加足迹
 *  - 3. 导航控制：通过返回按钮回到旅程列表，通过添加按钮跳转到足迹编辑页
 *  - 4. 面板高度交互：标题栏和内容区域均支持拖拽手势，联动调整面板高度
 *  - 5. 数据加载：通过 LaunchedEffect 在旅程切换时自动加载对应足迹
 *
 * 【关联组件】
 *  - JourneyViewModel：管理足迹数据（footprints 列表），提供 loadFootprintsForJourney 方法
 *  - JourneyNavController：全局单例导航控制器，管理面板状态和选中数据
 *  - JourneyPanel2State：枚举状态，FOOTPRINT_LIST / FOOTPRINT_EDIT / JOURNEY_LIST 等
 *  - FootprintListItem：单个足迹卡片组件，支持点击展开标题栏按钮和详情面板
 *  - FootprintListAddIcon：右下角添加足迹按钮，内部使用 IconAdd 组件
 *  - IcJourneyHeightButton：面板高度切换图标按钮（上箭头），带旋转动画
 *  - LineBetween：分隔线组件，用虚线分隔标题行和内容区
 *  - Headline / TextMedium：自定义文本组件
 *  - Journey（Entity）：旅程数据实体，提供 id、title 等字段
 *  - Footprint（Entity）：足迹数据实体，提供 id、title、description、address、createTime 等字段
 *
 * 【简单实现逻辑】
 *  - 1. 从 JourneyViewModel.uiState 收集足迹数据，通过 LaunchedEffect(journeySelected) 加载
 *  - 2. 使用 clickItemIndex 状态变量跟踪当前被选中（展开）的足迹条目
 *  - 3. HeadRow 标题行：返回按钮导航至 JOURNEY_LIST，标题支持拖拽手势，右侧为面板高度切换按钮
 *  - 4. Content 内容区：Box 容器内，足迹为空时显示 TextMedium 提示；非空时渲染 LazyColumn
 *  - 5. LazyColumn 中每项 FootprintListItem，点击时更新 clickItemIndex；列表底部留 70dp 间距
 *  - 6. FootprintListAddIcon 位于 BottomEnd，点击导航至 FOOTPRINT_EDIT 页面
 */

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun FootprintList(
    journeySelected: Journey,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"),
) {
    // 从 ViewModel 收集 UI 状态，获取足迹数据
    val journeyUiState by journeyViewModel.uiState.collectAsState()

    // 从 ViewModel 状态中读取当前旅程的足迹列表
    val footprints = journeyUiState.footprints

    // 当选中旅程变化时，自动加载对应的足迹数据
    LaunchedEffect(journeySelected) {
        journeyViewModel.loadFootprintsForJourney(journeyId = journeySelected.id)
    }

    // 跟踪当前被点击（展开）的足迹条目索引，-1 表示无选中项
    var clickItemIndex by remember { mutableStateOf(-1) }

    Log.d("FootprintList", "footprints = ${footprints}")

    // 垂直布局：间距 + 标题行 + 分隔线 + 内容区
    Column {
        Spacer(Modifier.height(10.dp))
        HeadRow(
            journeySelected,
            journeyPanelHeightState,
            setJourneyPanelHeightState,
            setIsDragging = setIsDragging,
            onDragDelta = onDragDelta,
            onPanelNavigate = onPanelNavigate,
        )
        LineBetween(paddingUp = 2.dp, paddingDown = 2.dp, )
        Content(
            footprints,
            journeySelected,
            clickItemIndex,
            { i -> clickItemIndex = i},
            onPanelNavigate = onPanelNavigate,
        )
    }
}

// 标题行组件：返回按钮 + 旅程标题（支持拖拽手势）+ 面板高度切换按钮
@Composable
private fun HeadRow(
    journeySelected: Journey,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    onPanelNavigate(JourneyPanel2State.JOURNEY_DETAIL, journeySelected, null)
                }),
            painter = painterResource(id = R.drawable.ic_left2),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )
        Spacer(Modifier.width(5.dp))
        // 标题文本：显示"旅程名——足迹"，支持垂直拖拽手势以调整面板高度
        Headline(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { setIsDragging(true) },
                        onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                        onDragEnd = { setIsDragging(false) }
                    )
                },
            text = "${journeySelected.title}——足迹",
            fontSize = 18.sp
        )
        // 面板高度切换按钮：点击切换面板高度状态（展开/折叠）
        IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })
        Spacer(Modifier.width(10.dp))
    }
}

// 内容区组件：足迹列表或空状态提示，以及右下角的添加按钮
@Composable
private fun Content(
    footprints: List<Footprint>,
    journeySelected: Journey,
    clickItemIndex: Int,
    setClickItemIndex: (Int) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    // Box 容器：最小高度 200dp，叠加列表和添加按钮
    Box(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(200.dp)
    ) {
        if (footprints.isEmpty()) {
            // 空状态：居中显示提示文本引导用户添加足迹
            TextMedium(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "目前还没有足迹内容哦~\n点击右下角添加按钮，新增你的足迹~",
                fontSize = 15.sp,
            )
        } else {
            // LazyColumn 足迹列表：使用足迹 id 作为 key 保证列表高效重组
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 1000.dp)
                    .padding(horizontal = 20.dp)
            ) {
                // 按索引渲染每项足迹，FootprintListItem 接收点击回调更新选中状态
                itemsIndexed(footprints, key = { i, it -> it.id}) { index, footprint ->
                    FootprintListItem(
                        footprint = footprint,
                        footprintClick = { i ->
                            setClickItemIndex(i ?: index)
                        },
                        (index == clickItemIndex),
                        journeySelected,
                        onPanelNavigate = onPanelNavigate,
                    )
                }
                // 列表底部留白，避免被添加按钮遮挡
                item {
                    Spacer(Modifier.height(70.dp))
                }
            }
        }

        FootprintListAddIcon(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            clickable = {
                onPanelNavigate(JourneyPanel2State.FOOTPRINT_EDIT, journeySelected, null)
            }
        )
    }
}