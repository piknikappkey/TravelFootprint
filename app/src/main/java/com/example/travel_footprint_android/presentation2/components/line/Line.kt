package com.example.travel_footprint_android.presentation2.components.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.SecondColor1

@Composable
fun Line(
    modifier: Modifier = Modifier.fillMaxWidth(),
    color: Color = SecondColor1,
    dashLength: Float = 18f,
    gapLength: Float = 6f,
    thickness: Float = 1f,
) {
    Canvas(
        modifier = modifier
            .height(thickness.dp)  // 线的高度
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = thickness.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength, gapLength), // 实线段长度, 间隔长度
                phase = 0f
            )
        )
    }
}