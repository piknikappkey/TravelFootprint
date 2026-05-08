package com.example.travel_footprint_android.presentation2.components.journey_map2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.travel_footprint_android.R
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.example.travel_footprint_android.ui.theme.LocationIconColor
import com.example.travel_footprint_android.ui.theme.LocationRadiusFillColor
import com.example.travel_footprint_android.ui.theme.LocationStrokeColor

@Composable
fun JourneyMap2(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }
    val locationClient = remember {
        AMapLocationClient(context)
    }

    DisposableEffect(key1 = Unit) {
        mapView.onCreate(Bundle())

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
        val aMap = InitAmap(view, locationClient)

        // 设置用户位置图标
        setLocationIcon(aMap, context)
    }
}

// 初始化地图
private fun InitAmap(view: MapView, locationClient: AMapLocationClient) : AMap {
    val aMap = view.map

    // 隐藏缩放按钮（放大/缩小按钮）
    aMap.uiSettings.isZoomControlsEnabled = false

    val locationOption = AMapLocationClientOption().apply {
        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        isOnceLocation = true
    }
    locationClient.setLocationOption(locationOption)

    locationClient.setLocationListener { location ->
        if (location.errorCode == 0) {
            val latLng = LatLng(location.latitude, location.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            aMap.moveCamera(cameraUpdate)
        } else {
            val defaultLatLng = LatLng(39.9042, 116.4074)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLatLng, 10f)
            aMap.moveCamera(cameraUpdate)
        }
    }

    locationClient.startLocation()

    return aMap
}

// 设置用户位置图标
private fun setLocationIcon(aMap: AMap, context: Context){
    val coloredIcon = getColoredLocationIcon(context, LocationIconColor)

    val myLocationStyle = MyLocationStyle()
        .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
        .showMyLocation(true)
        .myLocationIcon(coloredIcon)
        .strokeColor(LocationStrokeColor) // 边框颜色
        .radiusFillColor(LocationRadiusFillColor) // 填充颜色
    aMap.myLocationStyle = myLocationStyle

    aMap.isMyLocationEnabled = true
}

// 自定义用户图标
private fun getColoredLocationIcon(
    context: Context,
    targetColor: Int // 颜色int值
): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_user_location)?.mutate()
    drawable?.setTint(targetColor)

    val width = drawable?.intrinsicWidth ?: 1
    val height = drawable?.intrinsicHeight ?: 1

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable?.setBounds(0, 0, width, height)
    drawable?.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
