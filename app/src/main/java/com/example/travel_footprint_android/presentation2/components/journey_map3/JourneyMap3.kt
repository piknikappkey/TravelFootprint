package com.example.travel_footprint_android.presentation2.components.journey_map3

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
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun JourneyMap3(
    modifier: Modifier = Modifier,
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(),
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
        ) { view ->
            // 设置定位监听器来获取当前位置
            val locationListener = AMapLocationListener { location ->
                if (location.errorCode == 0) {
                    journeyMap3ViewModel.setCurrentLocation(LatLng(location.latitude, location.longitude))
                }
            }
            journeyMap3ViewModel.getLocationClient()?.setLocationListener(locationListener)
        }
    }

    LaunchedEffect(Unit) {
        if (isInitialized) {
            journeyMap3ViewModel.startLocation()
        }
    }
}