package com.example.travel_footprint_android.presentation2.components.svg_map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.presentation2.components.svg_map.city_box.CityBox
import com.example.travel_footprint_android.presentation2.components.svg_map.interactive_china_map.InteractiveChinaMap
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

@Composable
fun SVGMap(
    modifier: Modifier = Modifier,
    setLightenCityMode: (LightenCityMode) -> Unit,
    lightedProvinces: List<LightedProvince>
) {
    // 存储选中的城市
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var cityInfo by remember { mutableStateOf<String?>(null) }

    // 是否显示选中城市
    var cityState by remember { mutableStateOf(false) }

    Log.d("SVGMap", "进入 SVGMap")

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // 交互式地图
        InteractiveChinaMap(
            { cityName, info ->
                selectedCity = cityName
                cityInfo = info
            },
            {
                cityState = it
            },
            lightedProvinces = lightedProvinces,
        )

        CityBox(selectedCity, cityState, lightedProvinces)
    }
}