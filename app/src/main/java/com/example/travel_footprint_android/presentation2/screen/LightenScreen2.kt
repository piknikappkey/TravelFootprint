package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.background
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
import com.example.travel_footprint_android.ui.theme.BGLight0

@Composable
fun LightenScreen2(
    lightenViewModel: LightenViewModel = hiltViewModel() // 自动注入
) {
    val uiState by lightenViewModel.uiState.collectAsState()
    // 点亮城市
    val lightCityList = uiState.lightedCities
    val lightedCityCount = uiState.lightedCityCount
    // 点亮省份
    val lightedProvinces = uiState.lightedProvinces
    val lightedProvinceCount = uiState.lightedProvinceCount

    var lightenCityMode by remember { mutableStateOf(LightenCityMode.PROVINCE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BGLight0)
            .clip(RoundedCornerShape(0.dp))
    ) {
        SVGMap(
            Modifier.weight(1f),
            { lightenCityMode = it },
            lightedProvinces
        )
//        LightAMap(Modifier.weight(1f), { lightenCityMode = it })
        LightPanel2(Modifier, lightenCityMode)
    }
}

enum class LightenCityMode {
    CITY,
    PROVINCE
}