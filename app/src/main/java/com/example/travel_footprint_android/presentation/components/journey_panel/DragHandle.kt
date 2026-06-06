package com.example.travel_footprint_android.presentation.components.journey_panel

/**
 * ================================================================
 * DragHandle — 拖拽手柄组件
 * ================================================================
 *
 * 【用途】
 *  - 作为 JourneyPanel(旅程底部面板)的拖拽手柄，提供垂直拖拽手势检测
 *  - 是面板拖拽交互的核心入口，连接用户手势与面板高度变化
 *
 * 【功能】
 *  1. 视觉指示：28×4dp 的灰色圆角小横条，提示用户该区域可拖拽
 *  2. 手势检测：通过 detectVerticalDragGestures 检测手指垂直拖拽
 *  3. 三个回调：拖拽开始/拖拽中/拖拽结束，与 JourneyPanel 的高度管理联动
 *  4. 布局技巧：整体向上偏移 28dp (offset(y=-28.dp))，使其在面板顶部半可见
 *
 * 【关联组件】
 *  - JourneyPanel(父组件) : 本组件的调用方，通过三个回调实时更新面板拖拽状态和高度
 *  - JourneyPanelState     : 面板状态容器，isDragging 标记与 onDragStart/onDragEnd 联动
 *
 * 【简单实现逻辑】
 *  1. 外层 Box：fillMaxWidth + 32.dp 高 + 透明背景 + offset(y=-28.dp) 向上偏移
 *  2. pointerInput + detectVerticalDragGestures 检测垂直拖拽手势
 *     - onDragStart: 通知父组件禁用动画，进入实时跟随模式
 *     - onVerticalDrag: 将像素位移传给父组件的 onDragDelta 进行比例换算
 *     - onDragEnd: 通知父组件恢复动画，触发 animateFloatAsState 平滑过渡
 *  3. 内层 Box：28×4dp 灰色小横条，RoundedCornerShape(5.dp) 产生圆角效果
 *     居中显示在外层 Box 中，作为视觉拖拽手柄
 * ================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// =========================================================================
// DragHandle Composable: 面板拖拽手柄 — 拖拽指示条 + 垂直手势检测
// =========================================================================
@Composable
fun DragHandle(
    onDragStart: () -> Unit,    // 拖拽开始回调：通知父组件进入实时跟随模式（禁用动画）
    onDragEnd: () -> Unit,      // 拖拽结束回调：通知父组件恢复动画（触发平滑过渡）
    onDragDelta: (Float) -> Unit, // 拖拽位移回调：将拖拽像素位移传给父组件换算为高度比例
    modifier: Modifier = Modifier, // 外部 Modifier，用于父组件布局定位
) {
    // 外层容器：32dp 高透明区域，向上偏移 28dp，使手柄条在面板顶部半可见
    Box(
        modifier = modifier
            .fillMaxWidth()                    // 宽度撑满父容器
            .height(32.dp)                     // 固定 32dp 高度，作为手势检测区域
            .offset(y = (-28).dp)              // 向上偏移 28dp，使指示条刚好露出面板顶部
            .background(Color.Transparent)     // 透明背景，不遮挡下方内容
            // pointerInput + detectVerticalDragGestures 注册垂直拖拽手势监听
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },                  // 手势开始 → 禁用动画
                    onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) }, // 拖拽中 → 传递像素位移
                    onDragEnd = { onDragEnd() }                       // 手势结束 → 恢复动画
                )
            },
        contentAlignment = Alignment.Center  // 内层指示条居中显示
    ) {
        // 拖拽指示条：28×4dp 灰色圆角小横条，作为视觉拖拽手柄
        Box(
            modifier = Modifier
                .width(28.dp)                                                       // 宽度 28dp
                .height(4.dp)                                                       // 高度 4dp
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(5.dp)) // 35% 透明度黑色 + 5dp 圆角
        )
    }
}
