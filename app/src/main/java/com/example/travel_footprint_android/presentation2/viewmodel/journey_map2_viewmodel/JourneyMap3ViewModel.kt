package com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.LocationIconColor
import com.example.travel_footprint_android.ui.theme.LocationRadiusFillColor
import com.example.travel_footprint_android.ui.theme.LocationStrokeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class JourneyMap3ViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _mapView = MutableStateFlow<MapView?>(null)
    private val _aMap = MutableStateFlow<AMap?>(null)
    private val _locationClient = MutableStateFlow<AMapLocationClient?>(null)
    private val _isInitialized = MutableStateFlow(false)

    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        initializeMapIfNeeded(application)
    }

    private fun initializeMapIfNeeded(context: android.content.Context) {
        if (_mapView.value == null) {
            val mapView = MapView(context)
            _mapView.value = mapView

            val aMap = mapView.map
            _aMap.value = aMap

            val locationClient = AMapLocationClient(context)
            _locationClient.value = locationClient

            // 初始化地图配置
            setupMap(aMap, locationClient)

            setLocationIcon(aMap, context)

            _isInitialized.value = true
        }
    }

    // 初始化地图配置
    private fun setupMap(aMap: AMap, locationClient: AMapLocationClient) {
        // 隐藏缩放按钮
        aMap.uiSettings.isZoomControlsEnabled = false

        // 配置定位
        val locationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationClient.setLocationOption(locationOption)

        // 设置定位回调
        locationClient.setLocationListener { location ->
            if (location.errorCode == 0) {
                val latLng = LatLng(location.latitude, location.longitude)
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            }
        }
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
        val drawable = ContextCompat.getDrawable(context, R.drawable.location_icon)?.mutate()
        drawable?.setTint(targetColor)

        val width = drawable?.intrinsicWidth ?: 1
        val height = drawable?.intrinsicHeight ?: 1

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun startLocation() {
        _locationClient.value?.startLocation()
    }

    fun getMapView(): MapView? = _mapView.value

    fun getAMap(): AMap? = _aMap.value

    fun getLocationClient(): AMapLocationClient? = _locationClient.value

    override fun onCleared() {
        super.onCleared()
        _locationClient.value?.onDestroy()
        _mapView.value?.onDestroy()
    }
}