package com.example.travel_footprint_android.presentation.components.custom_scrollbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.MainColor2

/**
 * 自定义垂直滚动条组件
 *
 * 使用 Canvas 绘制，兼容当前所有 Compose 版本。
 * 需放在 [Box] 中，与可滚动内容同层级。
 *
 * @param scrollState 滚动状态
 * @param modifier Modifier，用于在 Box 中定位（如 [Modifier.align]）
 * @param thumbColor 滑块颜色
 * @param thumbWidth 滚动条宽度
 * @param thumbCornerRadius 滑块圆角
 * @param minThumbHeightPx 滑块最小高度（像素），防止内容很多时滑块太小
 */
@Composable
fun VerticalCustomScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thumbColor: Color = MainColor2.copy(alpha = 0.6f),
    thumbWidth: Dp = 4.dp,
    thumbCornerRadius: Dp = 3.dp,
    minThumbHeightPx: Float = 20f,
) {
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(thumbWidth)
    ) {
        if (scrollState.maxValue == 0) return@Canvas

        val contentHeight = scrollState.maxValue + size.height
        val visibleRatio = size.height / contentHeight
        val thumbHeight = (size.height * visibleRatio).coerceIn(minThumbHeightPx, size.height)
        val maxThumbOffset = size.height - thumbHeight
        val thumbOffset = (scrollState.value / scrollState.maxValue.toFloat()) * maxThumbOffset

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(0f, thumbOffset),
            size = Size(size.width, thumbHeight),
            cornerRadius = CornerRadius(thumbCornerRadius.toPx())
        )
    }
}
