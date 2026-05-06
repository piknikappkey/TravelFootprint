package com.example.travel_footprint_android.presentation2.components.svg_map.city_box

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.ui.theme.BGLight1

@Composable
fun CityBox(
    selectedCity: String?,
    cityState: Boolean,
    lightedProvinces: List<LightedProvince>
) {
    // 透明度动画
    val animatedAlpha by animateFloatAsState(
        targetValue = if (cityState) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemAlpha"
    )


    Box(
        modifier = Modifier
            .alpha(animatedAlpha)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false  // 不裁剪阴影，保持默认
                )
                // 2. 背景：将形状传给 background，自动带有圆角
                .background(
                    color = BGLight1,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .wrapContentHeight() // 根据内容调整高度
        ) {
            selectedCity?.let {
                Text(
                    text = it.split("_")[0]
                )
                Text(
                    text = if(cityIsLighted(selectedCity, lightedProvinces)) "该城市已点亮！" else "该城市未点亮"
                )
            }
        }
    }
}

fun cityIsLighted(
    selectedCity: String,
    lightedProvinces: List<LightedProvince>
): Boolean {
    lightedProvinces.forEach { it ->
        if(it.provinceName == selectedCity) {
            return true
        }
    }
    return false
}