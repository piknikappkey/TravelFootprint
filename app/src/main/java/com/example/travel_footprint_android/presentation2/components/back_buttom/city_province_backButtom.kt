package com.example.travel_footprint_android.presentation2.components.back_buttom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import com.example.travel_footprint_android.presentation2.components.svg_map.ShowMapMode
@Composable
fun city_province_backButtom(
    showMapMode: ShowMapMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color(0xFF3B82F6),
    backgroundColor: Color = Color(0xFF9370DB),
    gradientEndColor: Color = Color(0xFFB399FF)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(backgroundColor, gradientEndColor)
                    )
                )
        ) {
            // 背景图片（可选）
            Image(
                painter = painterResource(id = R.drawable.round_back),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 图标
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "返回",
                modifier = when (showMapMode) {
                    ShowMapMode.CITY -> Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .graphicsLayer {
                            scaleX = -1f  // 水平镜像翻转
                        }
                    ShowMapMode.PROVINCE -> Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .graphicsLayer {
                            scaleX = 1f  // 不镜像，正常显示
                        }
                },
                tint = iconTint
            )
        }
    }
}