package com.example.travel_footprint_android.presentation2.components.svg_map.city_box

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.ui.theme.BGLight1

@Composable
fun CityBox(
    selectedCity: String?,
    cityState: Boolean,
    lightedProvinces: List<LightedProvince>,
    onLightCityClick: (provinceAdcode: String, provinceName: String) -> Unit = { _, _ -> }
) {
    // 透明度动画
    val animatedAlpha by animateFloatAsState(
        targetValue = if (cityState) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "cityBoxAlpha"
    )

    // 提取城市名称（去掉可能的后缀 _partX）
    val cityName = selectedCity?.split("_")?.firstOrNull() ?: ""

    // 检查省份是否已被点亮
    val isLighted = cityIsLighted(selectedCity, lightedProvinces)

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
                    clip = false
                )
                .background(
                    color = BGLight1,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            // 城市名称
            if (selectedCity != null) {
                Text(
                    text = cityName,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 点亮状态和按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态文字
                Text(
                    text = if (isLighted) "✨ 该城市已点亮！" else "🔘 该城市未点亮",
                    fontSize = 14.sp,
                    color = if (isLighted) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )

                // 未点亮时显示点亮按钮
                if (!isLighted && selectedCity != null) {
                    Button(
                        onClick = {
                            // 注意：这里需要传入省份的 adcode，但目前没有这个参数
                            // 如果只有名称，可以暂时用名称作为标识
                            onLightCityClick(cityName, cityName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "点亮此地",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 检查城市是否已被点亮
 * @param selectedCity 选中的城市名称（可能带后缀）
 * @param lightedProvinces 已点亮的省份列表
 * @return true 表示已点亮
 */
fun cityIsLighted(
    selectedCity: String?,
    lightedProvinces: List<LightedProvince>
): Boolean {
    if (selectedCity == null) return false

    val cityName = selectedCity.split("_").firstOrNull() ?: return false

    lightedProvinces.forEach { province ->
        if (province.provinceName == cityName) {
            return true
        }
    }
    return false
}