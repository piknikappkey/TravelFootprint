/*
 * DraggableRecordingIndicator - 可拖拽录制指示器组件
 *
 * 【用途】
 *  - 在旅程主界面中显示当前正在录制的足迹信息
 *  - 支持拖拽定位，用户可自由拖动到屏幕任意位置
 *  - 点击后导航到足迹列表并自动展开对应足迹
 *
 * 【功能】
 *  1. 可拖拽：使用 DraggableBox 实现拖拽功能，支持吸附对齐
 *  2. 录制信息：显示录制中的足迹标题和当前录制状态（录制中/已暂停）
 *  3. 点击导航：点击指示器导航到足迹列表面板，并自动展开对应的足迹项
 *
 * 【关联组件】
 *  - DraggableBox：可拖拽容器组件，提供拖拽定位能力
 *  - Headline / TextMedium：自定义文字组件
 *  - RecordingViewModel：录制状态管理，提供 isRecording、isPaused、足迹标题等信息
 *  - JourneyPanelViewModel：面板导航控制器，负责页面跳转和自动展开控制
 *  - JourneyViewModel：旅程数据管理，提供旅程列表用于查找匹配的旅程
 *  - JourneyPanel2State：面板状态枚举，用于指定导航目标页面
 *
 * 【简单实现逻辑】
 *  1. 接收 recordingState（录制状态）、journeys（旅程列表）、导航回调等参数
 *  2. 使用 DraggableBox 作为外层容器，设置初始偏移和吸附对齐
 *  3. 内部 Row 布局：白色半透明圆角背景，垂直排列标题和状态文字
 *  4. 点击时通过 journeyPanelViewModel 设置自动展开的足迹 ID，并导航到足迹列表
 */
package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.DraggableBox
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.viewmodel.JourneyPanelViewModel
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation.viewmodel.RecordingUiState
import com.example.travel_footprint_android.ui.theme.MainColor2

@Composable
fun DraggableRecordingIndicator(
    recordingState: RecordingUiState,
    journeys: List<Journey>,
    journeyPanelViewModel: JourneyPanelViewModel,
) {
    // 记录父容器宽度，用于计算右上角位置
    var outerWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    // 组件预估宽度（px）
    val estimatedWidth = with(density) { 160.dp.toPx() }
    // 记录内容宽度，用于精确计算右上角位置
    var contentWidth by remember { mutableFloatStateOf(estimatedWidth) }

    // 计算右上角 X 偏移量：父容器宽度 - 内容宽度 - 右侧边距(12dp)
    val rightPadding = with(density) { 12.dp.toPx() }
    val initialX = if (outerWidth > 0f) {
        (outerWidth - contentWidth - rightPadding).coerceAtLeast(0f)
    } else {
        // 初始值设为 0，等待 outerWidth 测量后再调整
        0f
    }

    DraggableBox(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(12.dp),
        initialOffsetX = initialX,
        initialOffsetY = with(density) { 12.dp.toPx() }, // 顶部边距 12dp
        enableSnap = true,
        onOuterSizeChanged = { width, _ ->
            outerWidth = width
        },
    ) {
        Row(
            modifier = Modifier
                .onSizeChanged { size ->
                    // 使用实际测量的宽度来精确计算位置
                    contentWidth = size.width.toFloat()
                }
                .background(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    // 点击指示器：导航到足迹列表，并设置自动展开
                    journeyPanelViewModel.setAutoExpandFootprintId(
                        recordingState.recordingFootprintId ?: -1L
                    )
                    journeyPanelViewModel.navigate(
                        JourneyPanel2State.FOOTPRINT_LIST,
                        journeys.find { it.id == recordingState.recordingJourneyId },
                        null,
                    )
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                Headline(
                    text = if (recordingState.recordingFootprintTitle.length > 6) {
                        recordingState.recordingFootprintTitle.take(6) + "..."
                    } else {
                        recordingState.recordingFootprintTitle
                    },
                    fontSize = 14.sp,
                )
                TextMedium(
                    text = if (recordingState.isPaused) "路线已暂停" else "路线记录中...",
                    fontSize = 13.sp,
                    color = MainColor2,
                )
            }
        }
    }
}
