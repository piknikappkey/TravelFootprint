package com.example.travel_footprint_android.presentation.components.button.button_border

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.SecondColor1
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun ButtonBorder(
    modifier: Modifier = Modifier,
    bgColor: Color = SecondColor1,
    borderColor: Color = SecondColor3,
    borderWidth: Int = 1,
    paddingValues: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 1.dp,                 // 阴影高度
                shape = shape, // 圆角
                clip = true                        // 同时按照该形状裁剪内容
            )
            .background(
                color = bgColor,
            )
            .border(
                width = borderWidth.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(paddingValues),
    ) {
        content()
    }
}