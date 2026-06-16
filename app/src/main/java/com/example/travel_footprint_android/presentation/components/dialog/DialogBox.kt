package com.example.travel_footprint_android.presentation.components.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox

@Composable
fun DialogBox(
    vararg imgIds: Int, // 背景图片资源 ID 列表，随机选取其中一张
    onDismissRequest: () -> Unit,
    maskAlpha: Float = 0.4f,
    maskColor: Color = Color.Black,
    animationDurationMs: Int = 250,
    paddingValues: PaddingValues = PaddingValues(15.dp),
    elevation: Dp = 1.dp, // 阴影大小
    shape: RoundedCornerShape = RoundedCornerShape(8.dp), // 圆角
    content: @Composable () -> Unit,
) {
    AppDialog(
        onDismissRequest,
        maskAlpha,
        maskColor,
        animationDurationMs
    ) {
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            BGImgBox(
                imgIds = imgIds,
                modifier = Modifier
                    .shadow(
                        elevation = elevation,                 // 阴影高度
                        shape = shape, // 圆角
                        clip = true                        // 同时按照该形状裁剪内容
                    ),
            ) {
                content()
            }
        }
    }
}
