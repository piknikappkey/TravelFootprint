package com.example.travel_footprint_android.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation.components.journey_map.JourneyMapView
import com.example.travel_footprint_android.presentation.components.journey_panel.JourneyPanelView

@Composable
fun FootprintScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        /**
         * 高德地图组件
         * 地图组件需要读取Room数据库中关于旅程和足迹的数据库表，并在地图上显示旅程标点
         * 当用户点击旅程标点时，要显示这个旅程下对应的足迹标点以及对应的足迹路线图
         */
        JourneyMapView()

        /**
         * 旅程面板（旅程、足迹）
         * 该面板用于显示旅程、足迹的详细信息
         */
        JourneyPanelView()

    }
}
