package com.example.travel_footprint_android.presentation.components.journey_panel

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

/**
 * 拖拽手柄组件
 *
 * 用途：作为底部面板的拖拽手柄，提供视觉拖拽指示条和垂直拖拽手势检测
 *
 * @param onDragStart 拖拽开始回调，用于禁用动画实现实时跟随
 * @param onDragEnd 拖拽结束回调，用于恢复动画过渡
 * @param onDragDelta 拖拽位移回调，将像素位移转换为面板高度比例变化
 */
@Composable
fun DragHandle(
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .offset(y = (-28).dp)
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                    onDragEnd = { onDragEnd() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 拖拽指示条：28×4dp 的灰色小横条，视觉提示用户可拖拽
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(4.dp)
                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
        )
    }
}
