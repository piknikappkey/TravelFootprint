package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

@Composable
fun LightCityScreen(
    lightPanel2State: LightPanel2State,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightenCityMode: LightenCityMode, // 显示模式（城市/省份）
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal =  30.dp, vertical = 15.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ),
        horizontalArrangement = Arrangement.spacedBy(2.dp), // 水平间距
        verticalArrangement = Arrangement.spacedBy(8.dp)    // 垂直间距
    ) {
        if(lightPanel2State == LightPanel2State.EDIT) return@FlowRow
        if(lightenCityMode == LightenCityMode.CITY) {
            for ((index, city) in lightCityList.withIndex()) {
                // 粗略显示下只显示前10个
                if(index > 9 && lightPanel2State == LightPanel2State.ROUGH_DISPLAY) break
                if(index != 0) {
                    Text("、")
                }
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(city.cityName)
                }
            }
        } else if (lightenCityMode == LightenCityMode.PROVINCE) {
            for ((index, city) in lightedProvinces.withIndex()) {
                // 粗略显示下只显示前10个
                if(index > 9 && lightPanel2State == LightPanel2State.ROUGH_DISPLAY) break
                if(index != 0) {
                    Text("、")
                }
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(city.provinceName)
                }
            }
        }
    }
}
