package com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel

/**
 * JourneyMap3ViewModel - 地图与定位 ViewModel
 *
 * 功能：管理高德地图初始化、定位服务、位置标记
 * 实现方法：
 *  - 使用 Hilt 依赖注入 + AndroidViewModel
 *  - 初始化 MapView、AMap、AMapLocationClient
 *  - 配置 MyLocationStyle 自定义定位图标
 *  - 通过 Marker 在地图上标记选中位置
 */

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
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
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
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    private val _selectedMarker = MutableStateFlow<Marker?>(null)

    val aMap: StateFlow<AMap?> = _aMap.asStateFlow()

    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    init {
        initializeMapIfNeeded(application)
    }

    /** 初始化地图（仅首次调用时执行） */
    private fun initializeMapIfNeeded(context: android.content.Context) {
        if (_mapView.value == null) {
            val mapView = MapView(context)
            _mapView.value = mapView

            val aMap = mapView.map
            _aMap.value = aMap

            val locationClient = AMapLocationClient(context)
            _locationClient.value = locationClient

            setupMap(aMap, locationClient)
            setLocationIcon(aMap, context)
            setupMapClickListener(aMap)

            _isInitialized.value = true
        }
    }

    /** 配置地图基础设置和定位参数 */
    private fun setupMap(aMap: AMap, locationClient: AMapLocationClient) {
        // 隐藏默认缩放控件和定位按钮
        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.isMyLocationButtonEnabled = false

        // 配置高精度定位模式，单次定位
        val locationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationClient.setLocationOption(locationOption)

        // 定位成功后将相机移动到当前位置
        locationClient.setLocationListener { location ->
            if (location.errorCode == 0) {
                val latLng = LatLng(location.latitude, location.longitude)
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            }
        }
    }

    /** 配置定位样式（图标颜色、覆盖范围颜色） */
    private fun setLocationIcon(aMap: AMap, context: Context) {
        val coloredIcon = getColoredLocationIcon(context, LocationIconColor)

        val myLocationStyle = MyLocationStyle()
            .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            .showMyLocation(true)
            .myLocationIcon(coloredIcon)
            .strokeColor(LocationStrokeColor)
            .radiusFillColor(LocationRadiusFillColor)
        aMap.myLocationStyle = myLocationStyle

        aMap.isMyLocationEnabled = true
    }

    /** 生成带颜色的定位图标 BitmapDescriptor */
    private fun getColoredLocationIcon(
        context: Context,
        targetColor: Int
    ): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_user_location2)?.mutate()
        drawable?.setTint(targetColor)

        val width = drawable?.intrinsicWidth ?: 1
        val height = drawable?.intrinsicHeight ?: 1

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /** 生成大尺寸红色标记图标（用于选中位置） */
    private fun getLargeLocationIcon(context: Context): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_location)?.mutate()
        drawable?.setTint(0xFFE53935.toInt())

        val width = 96
        val height = 192

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /** 设置地图点击监听，点击地图时清除选中标记 */
    private fun setupMapClickListener(aMap: AMap) {
        aMap.setOnMapClickListener {
//            clearSelectedMarker()
        }
    }

    /** 在地图上设置选中位置标记 */
    fun setSelectedLocation(latLng: LatLng) {
        clearSelectedMarker()

        val aMap = _aMap.value ?: return
        val context = getApplication<Application>().applicationContext

        val marker = aMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(getLargeLocationIcon(context))
                .anchor(0.5f, 0.5f)
        )
        _selectedMarker.value = marker

        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    }

    /** 清除地图上的选中标记 */
    fun clearSelectedMarker() {
        _selectedMarker.value?.remove()
        _selectedMarker.value = null
    }

    /** 更新当前定位位置 */
    fun setCurrentLocation(currentLocation: LatLng?) {
        _currentLocation.value = currentLocation
    }

    /** 启动定位服务 */
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