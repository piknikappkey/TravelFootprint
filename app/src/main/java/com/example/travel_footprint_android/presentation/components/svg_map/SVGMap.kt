package com.example.travel_footprint_android.presentation.components.svg_map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation.components.svg_map.city_box.CityBox
import com.example.travel_footprint_android.presentation.components.svg_map.city_box.SelectedCityInfo
import com.example.travel_footprint_android.presentation.components.svg_map.interactive_china_map.InteractiveChinaCityMap
import com.example.travel_footprint_android.presentation.components.svg_map.interactive_china_map.InteractiveChinaProviceMap
import com.example.travel_footprint_android.presentation.screen.nav_screen.LightenCityMode

@Composable
fun SVGMap(
    modifier: Modifier = Modifier,
    onLightCityClick: (adcode: String, name: String) -> Unit,
    lightedProvinces: List<LightedProvince>,
    lightedCity: List<LightedCity>,
    onModeChange: (LightenCityMode) -> Unit = {},
    navigateRequest: LightenCityMode? = null,
) {
    var selectedCityInfo by remember { mutableStateOf<SelectedCityInfo?>(null) }
    //
    var cityState by remember { mutableStateOf(false) }

    // 地图切换控制
    var showProvinceMap by remember { mutableStateOf(true) }

    // 地图切换城市
    var showCityMap by remember { mutableStateOf(false) }
    var currentScale by remember { mutableStateOf(1f) }

    val ZOOM_THRESHOLD = 3.6f  // 缩放阈值，超过此值切换到城市地图

    //当前点亮模式
    var currentMode by remember { mutableStateOf(LightenCityMode.PROVINCE) }

    // 监听外部导航请求
    LaunchedEffect(navigateRequest) {
        when (navigateRequest) {
            LightenCityMode.CITY -> {
                showProvinceMap = false
                showCityMap = true
                cityState = false
                selectedCityInfo = null
                currentMode = LightenCityMode.CITY
                onModeChange(LightenCityMode.CITY)
            }
            LightenCityMode.PROVINCE -> {
                showProvinceMap = true
                showCityMap = false
                cityState = false
                selectedCityInfo = null
                currentMode = LightenCityMode.PROVINCE
                onModeChange(LightenCityMode.PROVINCE)
            }
            null -> { }
        }
    }

    // 监听缩放变化，自动切换地图
    LaunchedEffect(currentScale) {
        when {
            currentScale > ZOOM_THRESHOLD && showProvinceMap -> {
                Log.d("SVGMap", "Switching to city map, scale=$currentScale")
                showProvinceMap = false
                currentMode=LightenCityMode.CITY
                onModeChange(LightenCityMode.CITY)
                showCityMap=true
                cityState = false
                selectedCityInfo = null
            }
            currentScale <= ZOOM_THRESHOLD && !showProvinceMap -> {
                Log.d("SVGMap", "Switching to province map, scale=$currentScale")
                showProvinceMap = true
                currentMode=LightenCityMode.PROVINCE
                onModeChange(LightenCityMode.PROVINCE)
                showCityMap=false
                cityState = false
                selectedCityInfo = null
            }
        }
    }

    // CityBox 展示 5 秒后自动消失
    LaunchedEffect(cityState) {
        if (cityState) {
            kotlinx.coroutines.delay(5000)
            cityState = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (showProvinceMap) {
            InteractiveChinaProviceMap(
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
        } else if(showCityMap){
            InteractiveChinaCityMap(
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
                lightedCity =lightedCity ,
            )
        }

        CityBox(
            selectedCityInfo = selectedCityInfo,
            cityState = cityState,
            lightedProvinces = lightedProvinces,
            lightedCity=lightedCity,
            lightenCityMode = currentMode,
            onLightCityClick = onLightCityClick
        )
    }
}
enum class ShowMapMode {
    PROVINCE,  // 省份地图
    CITY       // 城市地图
}