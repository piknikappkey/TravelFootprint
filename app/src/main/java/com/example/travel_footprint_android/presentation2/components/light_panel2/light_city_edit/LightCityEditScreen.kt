package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_set_time.LightCityEditSetTime
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_select.LightCityEidtSelect
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_province_edit_select.LightProvinceEditSelect
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import java.time.LocalDate

@Composable
fun LightCityEditScreen(
    lightPanel2State: LightPanel2State,
    lightenCityMode: LightenCityMode, // 显示模式（城市/省份）
) {
    Box(
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
    ) {
        if(lightPanel2State != LightPanel2State.EDIT) {
            return
        }
        if(lightenCityMode == LightenCityMode.CITY) {
            // 选择城市模块
            LightCityEidtSelect()
        } else {
            // 选择省份模块
            LightProvinceEditSelect()
        }
        var lightCityTime by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
        // 点亮时间模块
        LightCityEditSetTime("北京市", lightCityTime, { lightCityTime = it })
    }
}