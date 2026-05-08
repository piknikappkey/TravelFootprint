package com.example.travel_footprint_android.presentation2.components.bg_box

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BGColumn(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp, // 阴影大小
    shape: RoundedCornerShape = RoundedCornerShape(8.dp), // 圆角
    bgColor: Color = Color(0xffffffff),
    composable: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,                 // 阴影高度
                shape = shape, // 圆角
                clip = true                        // 同时按照该形状裁剪内容
            )
            .background(
                color = bgColor,
            )
    ) {
        composable()
    }
}