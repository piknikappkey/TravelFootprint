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
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.ui.theme.LocationRadiusFillColor
import com.example.travel_footprint_android.ui.theme.LocationStrokeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val _routePolylines = mutableMapOf<Int, Polyline>()

    private var _routeAnimationPlayed = false // 用于标记路线动画是否已播放

    private var animationJob: Job? = null

    val aMap: StateFlow<AMap?> = _aMap.asStateFlow()

    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    /** 初始化地图（由 Compose 层在 MapView.onCreate() 之后调用） */
    fun initializeMap(mapView: MapView) {
        if (_mapView.value != null) return

        _mapView.value = mapView
        val aMap = mapView.map
        _aMap.value = aMap

        val context = getApplication<Application>()
        viewModelScope.launch {
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

        // 加载自定义地图样式
        val context = getApplication<Application>()
        try {
            val styleData = context.assets.open("mystyle_sdk_1780028774_0100/style.data").readBytes()
            val styleExtraData = context.assets.open("mystyle_sdk_1780028774_0100/style_extra.data").readBytes()
            val options = CustomMapStyleOptions()
                .setStyleData(styleData)
                .setStyleExtraData(styleExtraData)
            aMap.setCustomMapStyle(options)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 配置高精度定位模式，单次定位
        val locationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationClient.setLocationOption(locationOption)

        locationClient.setLocationListener { location ->
            if (location.errorCode == 0) {
                val latLng = LatLng(location.latitude, location.longitude)
                _currentLocation.value = latLng
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            }
        }
    }

    /** 配置定位样式（图标颜色、覆盖范围颜色） */
    private fun setLocationIcon(aMap: AMap, context: Context) {
        val coloredIcon = getColoredLocationIcon(context)

        val myLocationStyle = MyLocationStyle()
            .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            .showMyLocation(true)
            .myLocationIcon(coloredIcon)
            .strokeColor(LocationStrokeColor)
            .radiusFillColor(LocationRadiusFillColor)
        aMap.myLocationStyle = myLocationStyle

        aMap.isMyLocationEnabled = true
    }

    /** 生成带颜色的定位图标 BitmapDescriptor */
    private fun getColoredLocationIcon(
        context: Context
    ): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_user_location6)?.mutate()

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
            clearSelectedMarker()
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

    fun updateRoutes(locations: List<Location>) {
        val aMap = _aMap.value ?: return

        val groups = locations
            .filter { it.index >= 1 }
            .groupBy { it.index }

        val existingKeys = _routePolylines.keys.toSet()
        val newKeys = groups.keys

        for (index in newKeys - existingKeys) {
            val points = groups[index]!!.map {
                LatLng(it.latitude, it.longitude)
            }
            if (points.size >= 2) {
                val polyline = aMap.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .color(getRouteColor(index))
                        .width(8f)
                        .transparency(0.85f)
                        .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                        .lineCapType(PolylineOptions.LineCapType.LineCapRound)
                        .zIndex(1f)
                )
                _routePolylines[index] = polyline
            }
        }

        for (index in newKeys.intersect(existingKeys)) {
            val points = groups[index]!!.map {
                LatLng(it.latitude, it.longitude)
            }
            if (points.size >= 2) {
                _routePolylines[index]?.setPoints(points)
            }
        }

        for (index in existingKeys - newKeys) {
            _routePolylines[index]?.remove()
            _routePolylines.remove(index)
        }
    }

    fun updateRoutesWithAnimation(
        locations: List<Location>,
        stepIntervalMs: Long = 8L
    ) {
        val aMap = _aMap.value ?: return

        Log.d("JourneyMap3ViewModel", "_routeAnimationPlayed = $_routeAnimationPlayed")

        if (_routeAnimationPlayed) {
            // 首次动画已播放过 → 后续增量更新不走动画
            updateRoutes(locations)
            return
        }

        animationJob?.cancel()
        clearAllRoutes()

        _routeAnimationPlayed = true

        val groups = locations
            .filter { it.index >= 1 }
            .groupBy { it.index }

        if (groups.isEmpty()) return

        animationJob = viewModelScope.launch {
            for ((index, locs) in groups) {
                val allPoints = locs.map { LatLng(it.latitude, it.longitude) }
                if (allPoints.size < 2) continue

                val polyline = aMap.addPolyline(
                    PolylineOptions()
                        .addAll(allPoints.take(2))
                        .color(getRouteColor(index))
                        .width(8f)
                        .transparency(0.85f)
                        .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                        .lineCapType(PolylineOptions.LineCapType.LineCapRound)
                        .zIndex(1f)
                )
                _routePolylines[index] = polyline

                for (i in 2 until allPoints.size) {
                    delay(stepIntervalMs)
                    polyline.setPoints(allPoints.take(i + 1))
                }
            }
        }
    }

    fun clearAllRoutes() {
        animationJob?.cancel()
        animationJob = null
        _routePolylines.values.forEach { it.remove() }
        _routePolylines.clear()
        _routeAnimationPlayed = false  // 重置标志
    }

    private fun getRouteColor(index: Int): Int {
        val colors = listOf(
            0xFFFFA321.toInt(),
            0xFFBE65FF.toInt(),
            0xFF30DCFF.toInt(),
            0xFFFF573C.toInt(),
            0xFF00F0B8.toInt(),
            0xFFE63991.toInt(),
            0xFF813CFA.toInt(),
            0xFFDDFC00.toInt(),
        )
        return colors[(index - 1) % colors.size]
    }

    fun getCurrentLatLngPair(): Pair<Double, Double>? {
        return _currentLocation.value?.let { Pair(it.latitude, it.longitude) }
    }

    fun getMapView(): MapView? = _mapView.value

    fun getAMap(): AMap? = _aMap.value

    fun getLocationClient(): AMapLocationClient? = _locationClient.value

    /** 重置状态（页面离开时调用，以便重新进入时重新初始化） */
    fun reset() {
        _isInitialized.value = false
        clearAllRoutes()
        _locationClient.value?.onDestroy()
        _locationClient.value = null
        clearSelectedMarker()
        _currentLocation.value = null
        _aMap.value = null
        _mapView.value = null
    }

    override fun onCleared() {
        super.onCleared()
        clearAllRoutes()
        _locationClient.value?.onDestroy()
    }
}