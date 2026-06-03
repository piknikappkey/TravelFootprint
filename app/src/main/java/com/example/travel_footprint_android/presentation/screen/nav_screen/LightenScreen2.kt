package com.example.travel_footprint_android.presentation.screen.nav_screen

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
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation.components.bg_animotion.RainEffect
import com.example.travel_footprint_android.presentation.components.image_random.ImageRain
import com.example.travel_footprint_android.presentation.components.light_panel2.LightPanel2
import com.example.travel_footprint_android.presentation.components.svg_map.SVGMap
import com.example.travel_footprint_android.presentation.components.svg_map.ShowMapMode

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
//            //背景图片
            /////////////加载流畅//////////////
            Image(
                painter = painterResource(id = R.drawable.bg_svg_water_color_blue),
                contentDescription = "背景图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            // 地图区域（填充整个屏幕）
            /////////////加载流畅//////////////
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


            /////////////非常卡顿//////////////
// 前景雨（带溅射效果，性能优化版）
//        IllustrationRain(
//            count = 150,           // 雨滴数量（可根据性能调整）
//            intensity = 1.2f,      // 雨势强度
//            enableSplash = true    // 启用溅射效果
//        )
//            //外层天气动效
//            RainEffect(
//                isRaining=true
//            )
//
//            /////////////第二卡顿//////////////
//            // 底部可拖拽面板（覆盖在地图之上）
            LightPanel2(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                lightenCityMode = lightenCityMode,
                lightenViewModel = lightenViewModel,
                showMapMode = showMapMode,
                onBackButtonClick = onBackButtonClick,
            )
////
////            //插花雨效果
////            /////////////加载流畅//////////////
//            ImageRain(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .alpha(1f),
//            )

        }
    }


enum class LightenCityMode {
    PROVINCE, CITY;

    val isCityMode: Boolean
        get() = this == CITY

    val isProvinceMode: Boolean
        get() = this == PROVINCE
}