package com.example.travel_footprint_android.presentation2.components.simple_amap

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.MapView

@Composable
fun SimpleAmap(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    // 处理 MapView 的生命周期
    DisposableEffect(key1 = Unit) {
        mapView.onCreate(Bundle())

        // 初始化定位客户端
        val locationClient = AMapLocationClient(context)

        onDispose {
            locationClient.onDestroy()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    ) { view ->
        // 初始化地图
        val aMap = view.map

        // 初始化定位客户端
        val locationClient = AMapLocationClient(context)

        // 配置定位参数
        val locationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationClient.setLocationOption(locationOption)

        // 设置定位回调
        locationClient.setLocationListener { location ->
            if (location.errorCode == 0) {
                // 定位成功
                val latLng = com.amap.api.maps.model.LatLng(location.latitude, location.longitude)
                val cameraUpdate = com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                aMap.moveCamera(cameraUpdate)
            } else {
                // 定位失败，使用默认位置
                val defaultLatLng = com.amap.api.maps.model.LatLng(39.9042, 116.4074)
                val cameraUpdate = com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(defaultLatLng, 10f)
                aMap.moveCamera(cameraUpdate)
            }
        }

        // 启动定位
        locationClient.startLocation()
    }
}