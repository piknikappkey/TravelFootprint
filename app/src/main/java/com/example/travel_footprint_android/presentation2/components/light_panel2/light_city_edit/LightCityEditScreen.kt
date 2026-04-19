package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_gettime.LightCityEditGettime
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_select.LightCityEidtSelect

@Composable
fun LightCityEditScreen(
    lightPanel2State: LightPanel2State
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
        // 选择城市模块
        LightCityEidtSelect()
        // 点亮时间模块
        LightCityEditGettime()
    }
}