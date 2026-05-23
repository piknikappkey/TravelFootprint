// SVGMap.kt (修改后)
package com.example.travel_footprint_android.presentation2.components.svg_map

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R  // ← 这是你需要的
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.back_buttom.city_province_backButtom
import com.example.travel_footprint_android.presentation2.components.svg_map.city_box.CityBox
import com.example.travel_footprint_android.presentation2.components.svg_map.city_box.SelectedCityInfo
import com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map.InteractiveChinaCityMap
import com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map.InteractiveChinaProviceMap
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

@Composable
fun SVGMap(
    modifier: Modifier = Modifier,
    onLightCityClick: (adcode: String, name: String) -> Unit,
    lightedProvinces: List<LightedProvince>,
    lightedCity: List<LightedCity>,
    onModeChange: (LightenCityMode) -> Unit = {}
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
    Log.d("缩放阈值", "当前缩放阈值为：${currentScale}")

    // 监听缩放变化，自动切换地图
    LaunchedEffect(currentScale) {
        when {
            currentScale > ZOOM_THRESHOLD && showProvinceMap -> {
                Log.d("SVGMap", "Switching to city map, scale=$currentScale")
                showProvinceMap = false
                onModeChange(LightenCityMode.CITY)         // 显示城市
                showCityMap=true
                // 切换时关闭城市信息框
                cityState = false
                selectedCityInfo = null
            }
            currentScale <= ZOOM_THRESHOLD && !showProvinceMap -> {
                Log.d("SVGMap", "Switching to province map, scale=$currentScale")
                showProvinceMap = true
                onModeChange(LightenCityMode.PROVINCE)         // 显示省份
                showCityMap=false
                // 切换时关闭城市信息框
                cityState = false
                selectedCityInfo = null
            }
        }
    }
    // 专门用于返回省份地图的函数
    fun backToProvinceMap() {
        showProvinceMap = true
        showCityMap = false
        cityState = false
        selectedCityInfo = null
    }

    // 专门用于跳转城市地图的函数
    fun NavigateToCityMap() {
        showCityMap = true
        showProvinceMap = false
        cityState = false
        selectedCityInfo = null
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // 添加边距，避免贴边
                contentAlignment = Alignment.BottomEnd
            ) {
                city_province_backButtom(
                    ShowMapMode.CITY,
                    onClick = {
                        NavigateToCityMap()
                    }
                )
            }
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
//                onZoomChange = { scale -> currentScale = scale }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // 添加边距，避免贴边
                contentAlignment = Alignment.BottomEnd
            ) {
                city_province_backButtom(
                    ShowMapMode.PROVINCE,
                    onClick = {
                        backToProvinceMap()
                    }
                )
            }

        }


        CityBox(
            selectedCityInfo = selectedCityInfo,
            cityState = cityState,
            lightedProvinces = lightedProvinces,
            onLightCityClick = onLightCityClick
        )
    }
}
enum class ShowMapMode {
    PROVINCE,  // 省份地图
    CITY       // 城市地图
}