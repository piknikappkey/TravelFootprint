package com.example.travel_footprint_android.presentation2.components.svg_map.city_box

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

data class SelectedCityInfo(
    val name: String,           // 城市名称，如 "北京市"
    val adcode: String,         // 城市/省份代码，如 "110000"
    val parentAdcode: String = ""  // 父级代码
)

@Composable
fun CityBox(
    selectedCityInfo: SelectedCityInfo?,
    cityState: Boolean,
    lightedProvinces: List<LightedProvince>,
    lightedCity: List<LightedCity>,
    lightenCityMode: LightenCityMode,
    onLightCityClick: (provinceAdcode: String, provinceName: String) -> Unit = { _, _ -> }
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (cityState) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "cityBoxAlpha"
    )

    val cityName = selectedCityInfo?.name ?: ""
    val cityAdcode = selectedCityInfo?.adcode ?: ""
    val isLighted = when (lightenCityMode) {
        LightenCityMode.CITY -> {
            cityIsLighted(cityName, lightedCity)
        }
        LightenCityMode.PROVINCE -> {
            provinceIsLighted(cityName, lightedProvinces)
        }
    }

    Log.d("选中的地区：${cityName}", "模式${lightenCityMode} + $isLighted")

    Box(
        modifier = Modifier
            .alpha(animatedAlpha)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 16.dp, horizontal = 18.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            if (selectedCityInfo != null) {
                Text(
                    text = cityName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLighted) "✓ 该地区已点亮" else "该地区未点亮",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLighted) Color(0xFF10B981) else Color(0xFFF59E0B)
                )

                if (!isLighted && selectedCityInfo != null) {
                    Button(
                        onClick = { onLightCityClick(cityAdcode, cityName) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "点亮此地",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// 保留原有函数以兼容旧代码
fun cityIsLighted(
    selectedCity: String?,
    lightedCity: List<LightedCity>
): Boolean {
    if (selectedCity.isNullOrEmpty()) return false
    val cityName = selectedCity.split("_").firstOrNull() ?: return false
    return lightedCity.any { it.cityName == cityName }
}

fun provinceIsLighted(
    selectedCity: String?,
    lightedProvinces: List<LightedProvince>
): Boolean {
    if (selectedCity.isNullOrEmpty()) return false
    val cityName = selectedCity.split("_").firstOrNull() ?: return false
    return lightedProvinces.any { it.provinceName == cityName }
}