package com.example.travel_footprint_android.presentation2.components.light_panel2.panel_title

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

@Composable
fun PanelTitle(
    lightPanel2State: LightPanel2State,
    lightCityList: Int,
    lightedProvinceCount: Int,
    setLightPanel2State: (LightPanel2State) -> Unit,
    lightenCityMode: LightenCityMode, // 显示模式（城市/省份）
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if(lightPanel2State == LightPanel2State.ROUGH_DISPLAY ||
            lightPanel2State == LightPanel2State.ALL_DISPLAY) {
            CityTitle(lightPanel2State, lightCityList, lightedProvinceCount, setLightPanel2State, lightenCityMode)
        }
        if(lightPanel2State == LightPanel2State.EDIT) CityEditTitle(setLightPanel2State, lightenCityMode)
    }
}

@Composable
fun CityTitle(
    lightPanel2State: LightPanel2State,
    lightedCityCount: Int,
    lightedProvinceCount: Int,
    setLightPanel2State: (LightPanel2State) -> Unit,
    lightenCityMode: LightenCityMode, // 显示模式（城市/省份）
) {
    Row {
        Text("已点亮${if(lightenCityMode == LightenCityMode.CITY) lightedCityCount else lightedProvinceCount}个" + if(lightenCityMode == LightenCityMode.CITY) "城市" else "省份")
        Spacer(modifier = Modifier.weight(1f))
        if(
            lightenCityMode == LightenCityMode.CITY && lightedCityCount > 10 ||
            lightenCityMode == LightenCityMode.PROVINCE && lightedProvinceCount > 10
            ) {
            if(lightPanel2State == LightPanel2State.ROUGH_DISPLAY) {
                // 拓展按钮
                Text(
                    text = "拓展",
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                setLightPanel2State(LightPanel2State.ALL_DISPLAY)
                            }
                        )
                )
            }
            if(lightPanel2State == LightPanel2State.ALL_DISPLAY) {
                // 收缩按钮
                Text(
                    text = "收缩",
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                setLightPanel2State(LightPanel2State.ROUGH_DISPLAY)
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun CityEditTitle(
    setLightPanel2State: (LightPanel2State) -> Unit,
    lightenCityMode: LightenCityMode, // 显示模式（城市/省份）
) {
    Column {
        Row {

            //由于底部更新了取消按钮，暂时取消顶部按钮
//            // 取消按钮
//            Text(
//                text = "取消",
//                modifier = Modifier
//                    .clickable(
//                        onClick = {
//                            setLightPanel2State(LightPanel2State.ROUGH_DISPLAY)
//                        }
//                    )
//            )
//            Spacer(modifier = Modifier.weight(1f))
//            // 保存按钮
//            Text(
//                text = "保存",
//                modifier = Modifier
//                    .clickable(
//                        onClick = {
//                            setLightPanel2State(LightPanel2State.ROUGH_DISPLAY)
//                        }
//                    )
//            )
        }
//        Text("修改${if(lightenCityMode == LightenCityMode.CITY) "城市" else "省份"}")
    }
}