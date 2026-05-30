package com.example.travel_footprint_android.presentation2.components.journey_map3

/**
 * JourneyMap3 - 地图展示组件
 *
 * 功能：集成高德地图，支持定位功能
 * 实现方法：
 *  - 使用 AndroidView 在 Compose 中嵌入 MapView
 *  - 通过 DisposableEffect 管理 MapView 生命周期（onCreate/onResume/onPause）
 *  - 使用 DisposableEffect + LaunchedEffect 触发定位
 *  - 通过 AMapLocationListener 获取当前位置并更新 ViewModel
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.MapView
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun JourneyMap3(
    modifier: Modifier = Modifier,
    locationList: List<Location> = emptyList(),
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(key = "JourneyMap3"),
) {
    val isInitialized by journeyMap3ViewModel.isInitialized.collectAsState()
    val mapView = journeyMap3ViewModel.getMapView()

    // 管理 MapView 的生命周期
    DisposableEffect(key1 = mapView) {
        mapView?.onCreate(null)
        mapView?.onResume()

        onDispose {
            mapView?.onPause()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                mapView ?: MapView(ctx)
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            journeyMap3ViewModel.startLocation()
        }
    }

    LaunchedEffect(isInitialized, locationList) {
        if (isInitialized) {
            if (locationList.isNotEmpty()) {
                journeyMap3ViewModel.updateRoutesWithAnimation(locationList)
            } else {
                journeyMap3ViewModel.clearAllRoutes()
            }
        }
    }
}