package com.example.travel_footprint_android.presentation.components.setting_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation.components.journey_map.weather.WeatherViewModel
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.MainColor3

@Composable
fun SettingView(
    weatherViewModel: WeatherViewModel,
    onOpenRainSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()

    Column(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true
            )
    ) {
        BGImgBox(
            R.drawable.bg_rectangular_1__2__3,
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Headline(
                    text = "设置",
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(10.dp))

                SettingTitle("天气设置")
                SwitchRow(
                    label = "显示天气卡片",
                    checked = weatherState.showWeatherCard,
                    onCheckedChange = { weatherViewModel.toggleWeatherCard() }
                )
                Spacer(Modifier.height(10.dp))

                SettingTitle("涂鸦雨设置")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextMedium(
                        text = "打开涂鸦雨设置",
                        fontSize = 15.sp,
                    )
                    ButtonMain(
                        onClick = onOpenRainSettings,
                        paddingValues = PaddingValues(10.dp, 3.dp)
                    ) {
                        TextMedium(
                            text = "设置",
                            fontSize = 13.sp,
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun SettingTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextSmall(
            text = title,
        )
        LineBetween(lineLength = 1f)
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextMedium(
            text = label,
            fontSize = 15.sp,
        )
        Switch(
            modifier = Modifier.scale(.6f).height(18.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MainColor3,
                checkedTrackColor = MainColor3.copy(alpha = 0.5f),
            )
        )
    }
}
