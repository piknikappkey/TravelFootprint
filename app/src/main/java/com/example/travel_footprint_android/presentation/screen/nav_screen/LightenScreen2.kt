package com.example.travel_footprint_android.presentation2.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.image_random.ImageRain
import com.example.travel_footprint_android.presentation.components.light_panel2.LightPanel2
import com.example.travel_footprint_android.presentation.components.svg_map.SVGMap
import com.example.travel_footprint_android.presentation.components.svg_map.ShowMapMode
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel

@Composable
fun LightenScreen2(
    lightenViewModel: LightenViewModel = hiltViewModel(),
) {
    val uiState by lightenViewModel.uiState.collectAsState()
    val lightedProvinces = uiState.lightedProvinces
    val lightedCity = uiState.lightedCities
    var lightenCityMode by remember { mutableStateOf(LightenCityMode.PROVINCE) }
    var navigateRequest by remember { mutableStateOf<LightenCityMode?>(null) }

    val showMapMode: ShowMapMode? = when (lightenCityMode) {
        LightenCityMode.PROVINCE -> ShowMapMode.CITY
        LightenCityMode.CITY -> ShowMapMode.PROVINCE
    }

    val onBackButtonClick: () -> Unit = {
        navigateRequest = when (lightenCityMode) {
            LightenCityMode.PROVINCE -> LightenCityMode.CITY
            LightenCityMode.CITY -> LightenCityMode.PROVINCE
        }
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
        ) {
            //背景图片
            Image(
                painter = painterResource(id = R.drawable.bg_svg_water_color_blue),
                contentDescription = "背景图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            // 地图区域（填充整个屏幕）
            SVGMap(
                modifier = Modifier.fillMaxSize(),
                onLightCityClick = { adcode, name ->
                    lightenViewModel.LightedRecord(
                        adcode = adcode,
                        name = name
                    )
                },
                lightedProvinces = lightedProvinces,
                lightedCity = lightedCity,
                onModeChange = { newMode ->
                    lightenCityMode = newMode
                    Log.d("显示模式", "$newMode")
                },
                navigateRequest = navigateRequest,
            )

            // 雨滴叠加在内容上层
//            OpenGLRainBackground(show = true)


            // 底部可拖拽面板（覆盖在地图之上）
            LightPanel2(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                lightenCityMode = lightenCityMode,
                lightenViewModel = lightenViewModel,
                showMapMode = showMapMode,
                onBackButtonClick = onBackButtonClick,
            )

            //插花雨效果
            ImageRain(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f),
            )

        }
    }


enum class LightenCityMode {
    PROVINCE, CITY;

    val isCityMode: Boolean
        get() = this == CITY

    val isProvinceMode: Boolean
        get() = this == PROVINCE
}