package com.example.travel_footprint_android.presentation.components.journey_map

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.MapView

@Composable
fun JourneyMapScreen() {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    // 处理 MapView 的生命周期
    DisposableEffect(key1 = Unit) {
        mapView.onCreate(Bundle())
        onDispose {
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    ) {view ->
        // 初始化地图
        val aMap = view.map
        // 设置地图默认显示位置（北京）
        val cameraUpdate = com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(
            com.amap.api.maps.model.LatLng(39.9042, 116.4074),
            10f
        )
        aMap.moveCamera(cameraUpdate)
    }
}