package com.example.travel_footprint_android.presentation2.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2
import com.example.travel_footprint_android.presentation2.components.svg_map.SVGMap

@Composable
fun LightenScreen2(
    lightenViewModel: LightenViewModel = hiltViewModel()
) {
    val uiState by lightenViewModel.uiState.collectAsState()
    //获取ui点亮省份数据
    val lightedProvinces = uiState.lightedProvinces
    //获取ui点亮城市数据
    val lightedCity = uiState.lightedCities

    var lightenCityMode by remember { mutableStateOf(LightenCityMode.PROVINCE) }

    // 使用 Box 作为最外层容器，支持叠加背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        // 🆕 背景图片 - 铺满整个屏幕
//        Image(
//            painter = painterResource(id = R.drawable.main_bac),  // 替换为你的图片资源
//            contentDescription = "背景图片",
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop  // 裁剪填充，保持比例填满屏幕
//        )

        // 可选：添加一层半透明遮罩，让前景内容更清晰
        // Box(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(Color.Black.copy(alpha = 0.2f))
        // )

        // 内容（地图 + 底部面板）
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SVGMap(
                modifier = Modifier.weight(1f),
                onLightCityClick = { adcode, name ->
                    lightenViewModel.LightedRecord (
                        adcode=adcode,
                        name=name
                    )
                },
                lightedProvinces = lightedProvinces,
                lightedCity=lightedCity,
            onModeChange = {newMode->
                lightenCityMode=newMode
                Log.d("显示模式","$newMode")
            } // 回调LightenCityMode,判断点亮模式
            )

            LightPanel2(Modifier, lightenCityMode)
        }
    }
}

//点亮模式
enum class LightenCityMode {
    CITY,
    PROVINCE
}
