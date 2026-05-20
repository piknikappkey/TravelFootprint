// SVGMap.kt (修改后)
package com.example.travel_footprint_android.presentation2.components.svg_map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.presentation2.components.svg_map.city_box.CityBox
import com.example.travel_footprint_android.presentation2.components.svg_map.city_box.SelectedCityInfo
import com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map.InteractiveChinaMap
import com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map2.InteractiveChinaMap2

@Composable
fun SVGMap(
    modifier: Modifier = Modifier,
    onLightCityClick: (adcode: String, name: String) -> Unit,
    lightedProvinces: List<LightedProvince>
) {
    var selectedCityInfo by remember { mutableStateOf<SelectedCityInfo?>(null) }
    var cityState by remember { mutableStateOf(false) }

    // 地图切换控制
    var showProvinceMap by remember { mutableStateOf(true) }
    var currentScale by remember { mutableStateOf(1f) }

    val ZOOM_THRESHOLD = 1.8f  // 缩放阈值，超过此值切换到城市地图

    // 监听缩放变化，自动切换地图
    LaunchedEffect(currentScale) {
        when {
            currentScale > ZOOM_THRESHOLD && showProvinceMap -> {
                Log.d("SVGMap", "Switching to city map, scale=$currentScale")
                showProvinceMap = false
                // 切换时关闭城市信息框
                cityState = false
                selectedCityInfo = null
            }
            currentScale <= ZOOM_THRESHOLD && !showProvinceMap -> {
                Log.d("SVGMap", "Switching to province map, scale=$currentScale")
                showProvinceMap = true
                // 切换时关闭城市信息框
                cityState = false
                selectedCityInfo = null
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (showProvinceMap) {
            InteractiveChinaMap(
                onCityClick = { cityName, adcode, parentAdcode ->
                    selectedCityInfo = SelectedCityInfo(
                        name = cityName,
                        adcode = adcode,
                        parentAdcode = parentAdcode
                    )
                    cityState = true
                },
                cityClickState = { visible ->
                    cityState = visible
                },
                lightedProvinces = lightedProvinces,
                onZoomChange = { scale -> currentScale = scale }
            )
        } else {
            InteractiveChinaMap2(
                onCityClick = { cityName, adcode, parentAdcode ->
                    selectedCityInfo = SelectedCityInfo(
                        name = cityName,
                        adcode = adcode,
                        parentAdcode = parentAdcode
                    )
                    cityState = true
                },
                cityClickState = { visible ->
                    cityState = visible
                },
                lightedProvinces = lightedProvinces,
                onZoomChange = { scale -> currentScale = scale }
            )
        }

        CityBox(
            selectedCityInfo = selectedCityInfo,
            cityState = cityState,
            lightedProvinces = lightedProvinces,
            onLightCityClick = onLightCityClick
        )
    }
}