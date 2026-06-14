/*
 * JourneyMap3ViewModel - 地图与定位 ViewModel
 *
 * 【用途】
 *  - 作为旅程详情页中地图模块的状态管理与业务逻辑层
 *  - 管理高德地图（AMap）的初始化、定位服务、位置标记、路线绘制
 *  - 通过 StateFlow 向 Compose UI 层暴露地图状态
 *
 * 【功能】
 *  1. 地图初始化：管理 MapView 生命周期，初始化 AMap 和 AMapLocationClient
 *  2. 定位服务：配置高精度定位模式，将定位结果更新到 _currentLocation StateFlow
 *  3. 位置标记：在地图上添加/清除选中位置的大尺寸红色标记图标
 *  4. 自定义地图样式：从 assets 加载 style.data / style_extra.data 自定义地图视觉风格
 *  5. 路线绘制：将 Location 数据按 index 分组为多条路线，用不同颜色的 Polyline 绘制
 *  6. 路线动画：支持逐点渐进的路线绘制动画，通过协程 + delay 实现
 *  7. 定位图标：使用 getColoredLocationIcon 生成带主题色的自定义定位图标
 *  8. 状态重置：提供 reset() 方法在页面离开时清理所有地图资源
 *
 * 【关联组件】
 *  - Location 实体：数据层定位点，包含 footprintId、经纬度、路线索引
 *  - LocationStrokeColor / LocationRadiusFillColor：定位精度圈的颜色配置
 *  - 被 FootprintListPanel 等 Compose 组件调用，传递定位数据和路线信息
 *
 * 【简单实现逻辑】
 *  1. @HiltViewModel 注解 + @Inject constructor，由 Hilt 自动注入 Application
 *  2. 用 MutableStateFlow 管理地图对象、定位客户端、当前位置等状态
 *  3. initializeMap() 在 Compose 层 MapView.onCreate() 后调用，初始化地图和定位
 *  4. startLocation() 启动 AMapLocationClient 开始定位，结果回调中更新相机位置
 *  5. updateRoutes() 按 index 分组 Location → 新增/更新/删除对应 Polyline
 *  6. updateRoutesWithAnimation() 通过协程逐点追加 polyline 点实现动画效果
 *  7. clearAllRoutes() / reset() 清理所有路线、标记和定位资源
 */

package com.example.travel_footprint_android.presentation.components.journey_map.viewmodel

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
import com.amap.api.maps.TextureMapView
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
class JourneyMapViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // ========== 状态 StateFlow（暴露给 UI 层读取） ==========

    // 地图视图实例（使用 TextureMapView 避免 SurfaceView 黑色区域问题）
    private val _mapView = MutableStateFlow<TextureMapView?>(null)
    // 高德地图控制器
    private val _aMap = MutableStateFlow<AMap?>(null)
    // 定位客户端
    private val _locationClient = MutableStateFlow<AMapLocationClient?>(null)
    // 是否已完成初始化
    private val _isInitialized = MutableStateFlow(false)
    // 当前定位位置
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    // 当前选中的标记
    private val _selectedMarker = MutableStateFlow<Marker?>(null)
    // 网络错误提示信息（null 表示网络正常）
    private val _networkError = MutableStateFlow<String?>(null)

    // ========== 闪屏状态 ==========

    // 是否显示地图闪屏动画（由 JourneyScreen2 使用，避免在 AnimatedContent 切换时丢失状态）
    private val _showSplash = MutableStateFlow(true)
    // 是否显示地图主界面
    private val _showMapScreen = MutableStateFlow(false)

    // ========== 路线管理 ==========

    // 已绘制的路线集合：key = 路线索引(index)，value = Polyline
    private val _routePolylines = mutableMapOf<Int, Polyline>()
    // 标记路线动画是否已播放（首次播放后，增量更新不再播放动画）
    private var _routeAnimationPlayed = false
    // 动画协程 Job，用于取消正在播放的动画
    private var animationJob: Job? = null
    // 定位后相机移动动画时长（毫秒）
    private var _aniMoveTime: Long = 0
    // 是否处于持续跟随模式
    private var _isFollowMode = false
    // 进入跟随模式前保存的动画时长，退出时恢复
    private var _savedAniMoveTime: Long = 0

    // 屏幕参数和面板偏移量（用于底部面板补偿定位）
    private var _screenWidthPx: Int = 0
    private var _screenHeightPx: Int = 0
    private var _panelTopY: Int = 0

    // 面板偏移持久化存储（由 JourneyPanel 在每次组合时写入，供初始自动定位等场景使用）
    private var _storedScreenWidthPx: Int = 0
    private var _storedScreenHeightPx: Int = 0
    private var _storedPanelTopY: Int = 0

    // 供 JourneyPanel 写入当前面板偏移量，使初始定位等场景也能使用补偿定位
    fun setPanelOffset(screenWidthPx: Int, screenHeightPx: Int, panelTopY: Int) {
        _storedScreenWidthPx = screenWidthPx
        _storedScreenHeightPx = screenHeightPx
        _storedPanelTopY = panelTopY
    }

    // ========== 对外暴露的只读 StateFlow ==========

    val aMap: StateFlow<AMap?> = _aMap.asStateFlow()
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()
    val showMapScreen: StateFlow<Boolean> = _showMapScreen.asStateFlow()
    val networkError: StateFlow<String?> = _networkError.asStateFlow()

    // ========== 地图初始化 ==========

    /** 初始化地图（由 Compose 层在 TextureMapView.onCreate() 之后调用） */
    fun initializeMap(mapView: TextureMapView) {
        // 防止重复初始化
        if (_mapView.value != null) return

        Log.d("JourneyMap3ViewModel", "init")

        _mapView.value = mapView
        val aMap = mapView.map
        _aMap.value = aMap

        val context = getApplication<Application>()
        viewModelScope.launch {
            delay(100)
            // 创建定位客户端
            val locationClient = AMapLocationClient(context)
            _locationClient.value = locationClient
            // 配置地图设置和定位参数
            setupMap(aMap, locationClient)
            delay(100)
            // 设置自定义定位图标
            setLocationIcon(aMap, context)
            // 设置地图点击监听（点击清除选中标记）
            setupMapClickListener(aMap)
            // 标记初始化完成
            _isInitialized.value = true
            Log.d("JourneyMap3ViewModel", "init over!")
        }
    }

    /** 配置地图基础设置和定位参数 */
    private fun setupMap(aMap: AMap, locationClient: AMapLocationClient) {
        // 隐藏默认缩放控件和定位按钮（使用自定义 UI 替代）
        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.isMyLocationButtonEnabled = false

        // 从 assets 加载自定义地图样式文件
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

        // 配置定位参数：高精度模式 + 单次定位
        val locationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationClient.setLocationOption(locationOption)

        // 设置定位结果监听：成功后将相机移动到定位位置
        locationClient.setLocationListener { location ->
            if (location.errorCode == 0) {
                val latLng = LatLng(location.latitude, location.longitude)
                _currentLocation.value = latLng
                // 定位成功，清除网络错误提示
                _networkError.value = null
                val aniTime = _aniMoveTime
                // 跟随模式下始终读取最新的面板参数（面板可能被用户拖拽改变）
                val spWidth = if (_isFollowMode) _storedScreenWidthPx else _screenWidthPx
                val spHeight = if (_isFollowMode) _storedScreenHeightPx else _screenHeightPx
                val pTopY = if (_isFollowMode) _storedPanelTopY else _panelTopY
                // 如果传入了有效的屏幕参数（>0），使用补偿定位；否则回退到标准定位
                if (aniTime > 0 && spWidth > 0 && spHeight > 0 && pTopY > 0) {
                    animateCameraToPanelCompensatedCenter(aMap, latLng, aniTime, spWidth, spHeight.toFloat(), pTopY.toFloat())
                } else if (aniTime > 0) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f), aniTime, null)
                } else {
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            } else {
                // 定位失败：检测网络相关错误码
                val code = location.errorCode
                if (code == 7 || code == 18 || code == 19) {
                    // 7=网络异常, 18=网络超时, 19=网络连接异常
                    _networkError.value = "网络连接不可用，请检查网络"
                }
            }
        }
    }

    /**
     * 底部面板补偿定位：直接用数学公式计算补偿经纬度，使定位位置出现在可见区域（全屏-底部面板）的中心
     *
     * 原理：
     *   Web Mercator 投影下，在 zoom=z、纬度 φ 时：
     *     pixels_per_degree_lat = 256 * 2^z / (360 * cos(φ))
     *   用户位置需要从屏幕中心上升 offsetY_px 到可见区域中心，
     *   因此补偿后的相机目标 = 用户位置向南偏移 offsetY_px / pixels_per_degree_lat
     */
    private fun animateCameraToPanelCompensatedCenter(
        aMap: AMap,
        userLatLng: LatLng,
        aniTime: Long,
        screenWidthPx: Int,
        screenHeightPx: Float,
        panelTopY: Float
    ) {
        val lat = userLatLng.latitude
        val lng = userLatLng.longitude
        val zoom = 17f

        // 用户位置在屏幕上需要向上偏移的像素量（从屏幕中心到可见区域中心）
        val offsetPx = ((((screenHeightPx / 2) - (panelTopY / 2)) / screenHeightPx) * 1080)

        // Web Mercator: 在给定 zoom 和纬度下，每纬度对应的像素数
        // 256 * 2^zoom / 360 = 每个经度对应的赤道像素数
        // 除以 cos(lat) 得到纬度方向的像素/度（Mercator 拉伸）
        val pixelsPerDegreeLat = 256f * (1 shl zoom.toInt()) / 360f / kotlin.math.cos(Math.toRadians(lat)).toFloat()

        // 目标纬度偏移（offsetPx 为正 = 用户位置在可见区域中心偏上 = 目标在用户位置偏南）
        val latOffset = offsetPx / pixelsPerDegreeLat
        val compensatedLat = lat - latOffset

        val compensatedLatLng = LatLng(compensatedLat, lng)

        Log.d("JourneyMap3ViewModel", "compensate: screenH=$screenHeightPx panelTopY=$panelTopY offsetPx=$offsetPx pxPerDegLat=$pixelsPerDegreeLat latOffset=$latOffset compensatedLat=$compensatedLat")

        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(compensatedLatLng, zoom), aniTime, null)
    }

    // ========== 定位图标 ==========

    /** 配置定位样式（图标颜色、覆盖范围颜色） */
    private fun setLocationIcon(aMap: AMap, context: Context) {
        // 生成自定义着色的定位图标
        val coloredIcon = getColoredLocationIcon(context)

        // 配置定位点样式：旋转跟随、显示位置、自定义图标和精度圈颜色
        val myLocationStyle = MyLocationStyle()
            .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            .showMyLocation(true)
            .myLocationIcon(coloredIcon)
            .strokeColor(LocationStrokeColor)       // 精度圈描边色（半透明蓝）
            .radiusFillColor(LocationRadiusFillColor) // 精度圈填充色（半透明浅蓝）
        aMap.myLocationStyle = myLocationStyle

        // 启用定位图层
        aMap.isMyLocationEnabled = true
    }

    /** 将 ic_user_location6 drawable 转为 BitmapDescriptor（用于自定义定位图标） */
    private fun getColoredLocationIcon(
        context: Context
    ): BitmapDescriptor? {
        // 获取原始 drawable 并 mutate 以避免影响其他引用
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_user_location6)?.mutate()

        // 按原始尺寸创建 Bitmap
        val width = drawable?.intrinsicWidth ?: 1
        val height = drawable?.intrinsicHeight ?: 1

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /** 生成大尺寸红色标记图标（用于在地图上高亮显示选中的位置） */
    private fun getLargeLocationIcon(context: Context): BitmapDescriptor? {
        // 获取 ic_location drawable 并着色为红色
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_location)?.mutate()
        drawable?.setTint(0xFFE53935.toInt()) // 红色

        // 缩放为大尺寸（96x192）
        val width = 96
        val height = 192

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // ========== 标记管理 ==========

    /** 设置地图点击监听，点击地图空白区域时清除选中标记 */
    private fun setupMapClickListener(aMap: AMap) {
        aMap.setOnMapClickListener {
            clearSelectedMarker()
        }
    }

    /** 在地图上添加大尺寸红色标记，并移动相机到该位置 */
    fun setSelectedLocation(latLng: LatLng) {
        // 清除旧标记
        clearSelectedMarker()

        val aMap = _aMap.value ?: return
        val context = getApplication<Application>().applicationContext

        // 添加新标记，使用大尺寸红色图标，锚点在中心
        val marker = aMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(getLargeLocationIcon(context))
                .anchor(0.5f, 0.5f)
        )
        _selectedMarker.value = marker

        // 移动相机到标记位置，缩放级别17
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    }

    /** 清除地图上的选中标记 */
    fun clearSelectedMarker() {
        _selectedMarker.value?.remove()
        _selectedMarker.value = null
    }

    // ========== 定位控制 ==========

    /** 更新当前定位位置（供外部同步位置数据时调用） */
    fun setCurrentLocation(currentLocation: LatLng?) {
        _currentLocation.value = currentLocation
    }

    // ========== 闪屏控制 ==========

    /** 设置闪屏显示状态 */
    fun setShowSplash(show: Boolean) {
        _showSplash.value = show
    }

    /** 设置地图屏幕显示状态 */
    fun setShowMapScreen(show: Boolean) {
        _showMapScreen.value = show
    }

    /** 启动定位服务
     *  @param aniMoveTime 定位成功后相机移动动画时长（毫秒），0 表示无动画
     *  @param screenWidthPx 屏幕宽度（像素），>0 时启用底部面板补偿定位
     *  @param screenHeightPx 屏幕高度（像素）
     *  @param panelTopY 面板顶部距屏幕顶部的 Y 偏移（像素），即可见区域底部边界
     */
    fun startLocation(aniMoveTime: Long = 0, screenWidthPx: Int = 0, screenHeightPx: Int = 0, panelTopY: Int = 0) {
        Log.d("JourneyMap3ViewModel", "startLocation called: screenW=$screenWidthPx screenH=$screenHeightPx panelTopY=$panelTopY aniTime=$aniMoveTime")
        _aniMoveTime = aniMoveTime
        // 优先使用显式传入的参数，否则使用 JourneyPanel 最后一次写入的持久化值
        _screenWidthPx = if (screenWidthPx > 0) screenWidthPx else _storedScreenWidthPx
        _screenHeightPx = if (screenHeightPx > 0) screenHeightPx else _storedScreenHeightPx
        _panelTopY = if (panelTopY > 0) panelTopY else _storedPanelTopY
        _locationClient.value?.startLocation()
    }

    /** 启动持续跟随模式：定位回调会以面板补偿方式持续移动相机到用户位置 */
    fun startFollowUser() {
        val locationClient = _locationClient.value ?: return
        if (_isFollowMode) return
        _isFollowMode = true

        // 保存当前动画时长，跟随模式下使用较短时长以获得流畅体验
        _savedAniMoveTime = _aniMoveTime
        _aniMoveTime = 1000

        // 确保面板参数可用（使用 JourneyPanel 写入的持久化值）
        if (_screenWidthPx <= 0) _screenWidthPx = _storedScreenWidthPx
        if (_screenHeightPx <= 0) _screenHeightPx = _storedScreenHeightPx
        if (_panelTopY <= 0) _panelTopY = _storedPanelTopY

        // 切换为持续定位模式
        val option = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = false
            interval = 2000L
        }
        locationClient.setLocationOption(option)
        locationClient.startLocation()

        Log.d("JourneyMap3ViewModel", "startFollowUser: panelTopY=$_panelTopY screenH=$_screenHeightPx")
    }

    /** 停止持续跟随模式，恢复为单次定位 */
    fun stopFollowUser() {
        val locationClient = _locationClient.value ?: return
        if (!_isFollowMode) return
        _isFollowMode = false

        locationClient.stopLocation()

        // 恢复为单次定位
        val option = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationClient.setLocationOption(option)

        // 恢复之前的动画时长
        _aniMoveTime = _savedAniMoveTime

        Log.d("JourneyMap3ViewModel", "stopFollowUser: restored aniMoveTime=$_aniMoveTime")
    }

    // ========== 路线绘制 ==========

    /**
     * 增量更新路线：将 Location 数据按 index 分组，每组作为一条路线
     *  - 新增组 → 创建新 Polyline
     *  - 已有组 → 更新 Polyline 的点集
     *  - 消失组 → 移除对应的 Polyline
     */
    fun updateRoutes(locations: List<Location>) {
        val aMap = _aMap.value ?: return

        // 按 index 分组，过滤掉 index<1 的数据（index 最小为1）
        val groups = locations
            .filter { it.index >= 1 }
            .groupBy { it.index }

        // 计算新增、已有、消失的路线索引集合
        val existingKeys = _routePolylines.keys.toSet()
        val newKeys = groups.keys

        // 新增路线：在 _routePolylines 中不存在的 index
        for (index in newKeys - existingKeys) {
            val locs = groups[index]!!
            val points = locs.map {
                LatLng(it.latitude, it.longitude)
            }
            if (points.size >= 2) {
                val footprintId = locs.first().footprintId
                val polyline = aMap.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .color(getRouteColor(footprintId))
                        .width(8f)
                        .transparency(0.85f)
                        .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound) // 圆角连接
                        .lineCapType(PolylineOptions.LineCapType.LineCapRound)     // 圆头端点
                        .zIndex(1f)
                )
                _routePolylines[index] = polyline
            }
        }

        // 更新已有路线：更新点集以反映最新位置
        for (index in newKeys.intersect(existingKeys)) {
            val points = groups[index]!!.map {
                LatLng(it.latitude, it.longitude)
            }
            if (points.size >= 2) {
                _routePolylines[index]?.setPoints(points)
            }
        }

        // 删除消失的路线：从地图移除并清理集合
        for (index in existingKeys - newKeys) {
            _routePolylines[index]?.remove()
            _routePolylines.remove(index)
        }
    }

    /**
     * 带动画效果的路线绘制
     *  - 首次调用时，逐点渐增绘制路线
     *  - 后续调用（_routeAnimationPlayed=true）直接走 updateRoutes 增量更新
     *
     *  @param stepIntervalMs 每增加一个点的间隔毫秒数
     */
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

        // 取消旧的动画，清除已有路线
        animationJob?.cancel()
        clearAllRoutes()

        // 标记动画已播放
        _routeAnimationPlayed = true

        // 按 index 分组
        val groups = locations
            .filter { it.index >= 1 }
            .groupBy { it.index }

        if (groups.isEmpty()) return

        // 启动协程，按路线逐点绘制动画
        animationJob = viewModelScope.launch {
            for ((index, locs) in groups) {
                val allPoints = locs.map { LatLng(it.latitude, it.longitude) }
                if (allPoints.size < 2) continue

                val footprintId = locs.first().footprintId
                // 先绘制前2个点创建 Polyline
                val polyline = aMap.addPolyline(
                    PolylineOptions()
                        .addAll(allPoints.take(2))
                        .color(getRouteColor(footprintId))
                        .width(8f)
                        .transparency(0.85f)
                        .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                        .lineCapType(PolylineOptions.LineCapType.LineCapRound)
                        .zIndex(1f)
                )
                _routePolylines[index] = polyline

                // 从第3个点开始逐个追加，每次追加后 delay
                for (i in 2 until allPoints.size) {
                    delay(stepIntervalMs)
                    polyline.setPoints(allPoints.take(i + 1))
                }
            }
        }
    }

    /** 清除所有已绘制的路线（取消动画、移除 Polyline、重置动画标志） */
    fun clearAllRoutes() {
        animationJob?.cancel()
        animationJob = null
        _routePolylines.values.forEach { it.remove() }
        _routePolylines.clear()
        _routeAnimationPlayed = false
    }

    /** 根据足迹ID返回对应的颜色，每个足迹有专属颜色，最多支持8种颜色循环 */
    private fun getRouteColor(footprintId: Long): Int {
        val colors = listOf(
            0xFFFFA321.toInt(),   // 橙色
            0xFFBE65FF.toInt(),   // 紫色
            0xFF30DCFF.toInt(),   // 青色
            0xFFFF573C.toInt(),   // 红色
            0xFF00F0B8.toInt(),   // 蓝绿色
            0xFFE63991.toInt(),   // 粉红色
            0xFF813CFA.toInt(),   // 深紫色
            0xFFDDFC00.toInt(),   // 黄绿色
        )
        return colors[((footprintId - 1) % colors.size).toInt()]
    }

    // ========== 查询方法 ==========

    /** 获取当前定位的经纬度 Pair */
    fun getCurrentLatLngPair(): Pair<Double, Double>? {
        return _currentLocation.value?.let { Pair(it.latitude, it.longitude) }
    }

    fun getMapView(): TextureMapView? = _mapView.value
    fun getAMap(): AMap? = _aMap.value
    fun getLocationClient(): AMapLocationClient? = _locationClient.value

    // ========== 生命周期管理 ==========

    /** 重置所有状态（页面离开时调用，以便重新进入时重新初始化） */
    fun reset() {
        _isInitialized.value = false
        clearAllRoutes()
        _locationClient.value?.onDestroy()
        _locationClient.value = null
        clearSelectedMarker()
        _currentLocation.value = null
        _networkError.value = null
        _aMap.value = null
        _mapView.value = null
        _showSplash.value = true
        _showMapScreen.value = false
    }

    /** ViewModel 清除时（Activity 销毁）释放所有地图资源 */
    override fun onCleared() {
        super.onCleared()
        _mapView.value?.onDestroy()
        _mapView.value = null
        clearAllRoutes()
        _locationClient.value?.onDestroy()
        _locationClient.value = null
        clearSelectedMarker()
        _currentLocation.value = null
        _networkError.value = null
        _aMap.value = null
        _isInitialized.value = false
    }
}