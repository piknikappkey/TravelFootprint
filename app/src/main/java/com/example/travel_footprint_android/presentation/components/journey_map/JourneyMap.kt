package com.example.travel_footprint_android.presentation.components.journey_map

/**
 * JourneyMap3 - 地图展示组件
 *
 * 用途：
 * - 在旅程页面（JourneyScreen2）中提供高德地图视图，作为地图功能的顶层容器组件
 * - 负责 MapView 的完整生命周期管理（创建 → 初始化 → 恢复/暂停 → 销毁）
 * - 将路径定位点（Location 列表）渲染为地图上的轨迹路线（Polyline）
 * - 提供可拖拽的浮动定位按钮，支持用户手动触发定位
 *
 * 功能：
 * - 高德地图集成：通过 AndroidView 在 Compose 中嵌入原生 MapView，集成自定义样式和定位服务
 * - 生命周期管理：通过 DisposableEffect + LaunchedEffect 管理 MapView 的 onCreate/onResume/onPause/onDestroy 和 ViewModel 重置
 * - 定位服务：地图初始化后自动启动高精度单次定位，相机以 2000ms 动画平滑移动到当前位置
 * - 路线绘制：根据 Location 列表自动在地图上渲染/更新/清除轨迹路线（Polyline），支持首次绘制时的逐点动画效果
 * - 容器尺寸感知：通过 onSizeChanged 监听容器尺寸，确保 MapView 在尺寸就绪后才 onResume
 *
 * 关联组件：
 * - JourneyMap3ViewModel（通过 Hilt 注入，key="JourneyMap3"）：
 *   - 管理 MapView/AMap/AMapLocationClient 实例
 *   - 初始化地图：配置自定义样式、高精度定位、定位图标样式
 *   - 管理定位监听：定位结果回调后以 animateCamera 移动到当前位置
 *   - 管理路线：提供 updateRoutesWithAnimation（逐点动画）和 updateRoutes（增量更新）两种模式
 *   - 管理标记：支持设置/清除选中位置的大尺寸标记
 *   - 重置机制：reset() 清除所有状态，页面离开后重新进入时重新初始化
 * - LocationButton：可自由拖拽的 FAB 风格定位按钮组件
 *   - 覆盖在地图上方（通过 matchParentSize），点击调用 startLocation(aniMoveTime=2000)
 *   - 支持拖拽手势，Y 方向底部预留 250px 空间避免遮挡面板
 *   - 首次加载自动定位到容器右下角
 * - Location（数据实体）：
 *   - Room 数据库实体，包含 latitude/longitude/index 等字段
 *   - 通过 index 分组形成多个路径段，每组内的点按顺序连成轨迹线
 *
 * 实现逻辑：
 * 1. 使用 remember 创建 MapView 实例并调用 onCreate(null)
 * 2. LaunchedEffect(mapView) → 调用 ViewModel.initializeMap(mapView) 初始化 AMap 和定位客户端
 * 3. LaunchedEffect(mapSizeReady) → 容器尺寸就绪后调用 onResume() 恢复地图渲染
 * 4. DisposableEffect(mapView) → 组件销毁时执行 onPause → onDestroy → reset() 清理资源
 * 5. Box 容器中 AndroidView 嵌入 MapView + LocationButton 覆盖层
 * 6. LaunchedEffect(isInitialized) → 地图初始化完成后启动定位（2000ms 动画时长）
 * 7. LaunchedEffect(isInitialized, locationList) → 初始化完成后根据位置列表更新路线：
 *    - 有数据时调用 updateRoutesWithAnimation 绘制逐点动画路线
 *    - 无数据时调用 clearAllRoutes 清除所有路线
 */

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.MapView
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.presentation.components.journey_map.location_button.LocationButton
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMap3ViewModel
import com.example.travel_footprint_android.presentation.components.journey_map.weather.WeatherCard
import com.example.travel_footprint_android.presentation.components.journey_map.weather.WeatherViewModel

@Composable
fun JourneyMap3(
    modifier: Modifier = Modifier,
    locationList: List<Location> = emptyList(), // 足迹位置点列表，按 index 分组形成轨迹路线
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        key = "JourneyMap3"
    ), // Hilt 注入地图 ViewModel（Activity 级作用域，页面切换时不销毁）
    weatherViewModel: WeatherViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
    ), // Hilt 注入天气 ViewModel（Activity 级作用域）
) {
    // 监听地图初始化状态（ViewModel 中 initializeMap 完成后设为 true）
    val isInitialized by journeyMap3ViewModel.isInitialized.collectAsState()
    // 获取当前 Activity Context，用于创建 MapView
    val context = LocalContext.current
    // 标记容器尺寸是否已就绪，确保尺寸有效后才 onResume 地图
    var mapSizeReady by remember { mutableStateOf(false) }

    // 创建高德 MapView 实例并执行 onCreate
    // 优先复用 ViewModel 中已有的 MapView，避免二次进入时重建
    val mapView = remember {
        journeyMap3ViewModel.getMapView() ?: MapView(context).apply {
            onCreate(null)
        }
    }

    // MapView 创建/复用后 → 仅在首次初始化时调用 ViewModel 初始化地图
    // 二次进入时 ViewModel 已初始化，跳过以节省资源
    LaunchedEffect(mapView) {
        if (!journeyMap3ViewModel.isInitialized.value) {
            journeyMap3ViewModel.initializeMap(mapView)
        }
    }

    // 容器尺寸就绪后 → 恢复地图渲染（onResume + requestLayout + invalidate 确保正确显示）
    LaunchedEffect(mapSizeReady) {
        if (mapSizeReady) {
            mapView.onResume()
            mapView.requestLayout()
            mapView.invalidate()
        }
    }

    // 页面离开时 → 仅暂停 MapView（保留瓦片缓存和地图状态）
    // 不再调用 onDestroy() 和 reset()，让 ViewModel 中的 MapView 实例保持存活
    // 真正的销毁延后到 Activity 销毁时，在 ViewModel.onCleared() 中执行
    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
        }
    }

    // ========== 天气状态（在初始化之后自动加载） ==========
    val weatherState by weatherViewModel.weatherState.collectAsState()

    // 外层容器：填满父组件
    Box(modifier = modifier.fillMaxSize()) {
        // AndroidView 嵌入原生 MapView
        // 通过 onSizeChanged 监听尺寸变化，首次获取有效尺寸时标记 mapSizeReady = true
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    if (size.width > 0 && size.height > 0 && !mapSizeReady) {
                        mapSizeReady = true
                    }
                }
        )
        // 浮动定位按钮：覆盖在地图上方，matchParentSize 使其与地图容器等大
        // 支持拖拽和点击定位，拖拽范围受容器边界约束
        LocationButton(
            modifier = Modifier.matchParentSize().statusBarsPadding()
        )
        // 天气卡片：左上角显示当前天气
        if (weatherState.liveWeather != null) {
            Log.d("JourneyMap3", "weatherState.liveWeather = ${weatherState.liveWeather}")
            WeatherCard(
                weatherState = weatherState,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
            )
        }
    }

    // 地图初始化完成后 → 启动定位服务（aniMoveTime=2000 为相机移动动画时长）
    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            journeyMap3ViewModel.startLocation(aniMoveTime = 2000)
        }
    }

    // 地图初始化完成后 + 位置列表变化时 → 更新路线
    LaunchedEffect(isInitialized, locationList) {
        if (isInitialized) {
            if(locationList.size == 0) {
                Log.d("JourneyMap3Routes", "locationList = ${locationList}")
            } else {
                Log.d("JourneyMap3Routes", "locationList = ${locationList.first()}, size = ${locationList.size}")
            }
            if (locationList.isNotEmpty()) {
                journeyMap3ViewModel.updateRoutesWithAnimation(locationList)
            } else {
                journeyMap3ViewModel.clearAllRoutes()
            }
        }
    }

    // 地图初始化完成后 → 自动加载当前位置天气
    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            weatherViewModel.loadWeatherForCurrentLocation()
        }
    }
}