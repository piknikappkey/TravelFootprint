package com.example.travel_footprint_android.presentation.components.journey_map

/**
 * JourneyMap - 地图展示组件（旅程详情页的地图面板核心容器）
 *
 * 用途：
 * - 在旅程详情页中提供高德（AMap）地图视图，作为地图功能的顶层 Compose 容器
 * - 负责原生 MapView（TextureMapView）在 Compose 中的创建与生命周期管理
 * - 将足迹定位点（Location 列表）按 index 分组渲染为地图上的轨迹路线（Polyline）
 * - 承载可拖拽的浮动定位按钮（LocationButton）和天气信息卡片（WeatherCard）
 *
 * 功能：
 * - 高德地图集成：通过 AndroidView 在 Compose 中嵌入原生 TextureMapView，支持自定义地图样式
 * - 生命周期管理：通过 DisposableEffect 管理 onResume/onPause，组件销毁时自动暂停地图渲染
 * - 初始化控制：通过 LaunchedEffect(mapView) 在 MapView 创建后仅首次调用 ViewModel.initializeMap()
 * - 定位服务：地图初始化完成后自动启动高精度单次定位，相机以 2000ms 动画移至当前位置
 * - 路线绘制：监听 locationList 变化，有数据时调用 updateRoutesWithAnimation（首次逐点渐变动画），
 *   无数据时调用 clearAllRoutes 清除所有路线
 * - 天气卡片：通过 WeatherCard 在左上角展示当前城市天气信息（可拖拽）
 * - 定位按钮：通过 LocationButton 提供可拖拽的 FAB 风格按钮，点击触发定位
 *
 * 关联组件：
 * - JourneyMapViewModel（通过 Hilt 注入，key="JourneyMap3"，Activity 级作用域）：
 *   - 管理 TextureMapView / AMap / AMapLocationClient 实例
 *   - 初始化地图：配置自定义样式（从 assets 加载 style.data）、高精度定位、自定义定位图标
 *   - 管理定位监听：定位成功回调后以 animateCamera/moveCamera 移动到当前位置
 *   - 管理路线：提供 updateRoutesWithAnimation（逐点渐变动画）和 updateRoutes（增量更新）两种模式
 *   - 管理标记：支持在地图上设置/清除选中位置的大尺寸红色标记
 *   - 重置机制：reset() 清除所有状态并释放资源，支持页面重新进入时重新初始化
 * - LocationButton（journey_map/location_button/LocationButton.kt）：
 *   - 可自由拖拽的 FAB 风格定位按钮，覆盖在地图上方
 *   - 点击调用 ViewModel.startLocation(aniMoveTime)，触发高德定位服务
 *   - 通过 DraggableBox 实现拖拽手势，初始位置自动定位到容器右下角
 * - WeatherCard（journey_map/weather/WeatherCard.kt）：
 *   - 左上角显示当前城市天气信息（温度、天气图标、城市名）
 *   - 基于 WeatherViewModel 管理高德天气 API 请求状态
 *   - 同样支持拖拽移动，避免遮挡地图
 * - Location（data/entity/Location.kt）：
 *   - Room 数据库实体，包含 id / footprintId / latitude / longitude / index 字段
 *   - 通过 index 字段分组形成多个路径段，每组内的点按顺序连成轨迹线
 *   - 通过 ForeignKey 关联 Footprint 表，级联删除
 *
 * 实现逻辑：
 * 1. remember 创建/复用 MapView：优先获取 ViewModel 中缓存的 MapView，否则新建并调用 onCreate(null)
 * 2. LaunchedEffect(mapView) → 仅在 isInitialized=false 时调用 ViewModel.initializeMap(mapView)
 * 3. DisposableEffect(mapView) → 页面显示时 onResume()，页面离开时 onPause()
 * 4. Box 容器中 AndroidView 嵌入 MapView + WeatherCard（左上角）+ LocationButton（可拖拽覆盖层）
 * 5. LaunchedEffect(isInitialized) → 初始化完成后自动启动定位，aniMoveTime=2000ms
 * 6. LaunchedEffect(isInitialized, locationList) → locationList 变化时：
 *    - 有数据 → updateRoutesWithAnimation 逐点动画绘制轨迹
 *    - 无数据 → clearAllRoutes 清除所有路线
 */

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.TextureMapView
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel
import com.example.travel_footprint_android.presentation.viewmodel.RecordingViewModel
import kotlinx.coroutines.delay

@Composable
fun JourneyMap(
    modifier: Modifier = Modifier,
    locationList: List<Location> = emptyList(), // 足迹位置点列表，按 index 分组形成轨迹路线
    journeyMapViewModel: JourneyMapViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        key = "JourneyMap3"
    ), // Hilt 注入地图 ViewModel（Activity 级作用域，页面切换时不销毁）
    recordingViewModel: RecordingViewModel = hiltViewModel(),
) {
    // 监听地图初始化状态（ViewModel 中 initializeMap 完成后设为 true）
    val isInitialized by journeyMapViewModel.isInitialized.collectAsState()
    // 监听网络错误状态
    val networkError by journeyMapViewModel.networkError.collectAsState()
    // 监听录制状态，用于切换地图跟随模式
    val recordingState by recordingViewModel.uiState.collectAsState()
    // 获取当前 Activity Context，用于创建 MapView（TextureMapView 需要 Activity Context）
    val context = LocalContext.current

    // ========== 创建/复用 TextureMapView 实例 ==========
    // 优先复用 ViewModel 中缓存的 MapView（页面重新进入时避免重建）
    // 首次进入则新建实例并调用 onCreate(null) 完成 MapView 初始化
    val mapView = remember {
        journeyMapViewModel.getMapView() ?: TextureMapView(context).apply {
            onCreate(null)
        }
    }

    // ========== 首次初始化地图（一次性） ==========
    // LaunchedEffect 以 mapView 为 key，仅当 mapView 首次创建时触发
    // 二次进入时 ViewModel 的 isInitialized 已为 true，跳过初始化节省资源
    LaunchedEffect(mapView) {
        if (!journeyMapViewModel.isInitialized.value) {
            journeyMapViewModel.initializeMap(mapView)
        }
    }

    // ========== 管理 MapView 的 onResume/onPause 生命周期 ==========
    // DisposableEffect：页面进入时 onResume()，页面离开/组件销毁时 onPause()
    // 必须配对调用，否则地图将失去触摸响应能力
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
        }
    }

    // ========== 地图容器布局 ==========
    // Box 容器：AndroidView 嵌入原生 TextureMapView，填满容器
    // 通过 factory 返回 remember 创建的 mapView 实例，避免重组时重建
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
        )

        // 网络错误提示浮层：顶部半透明横幅，网络恢复后自动消失
        AnimatedVisibility(
            visible = networkError != null,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC333333))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = null,
                    tint = Color.White,
                )
                Text(
                    text = networkError ?: "",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }

    // ========== 初始化后启动定位 ==========
    // 地图初始化完成（isInitialized = true）后自动启动高精度单次定位
    // aniMoveTime=2000 表示相机以 2000ms 动画平滑移动到定位位置
    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            delay(500)
            journeyMapViewModel.startLocation(aniMoveTime = 1500)
        }
    }

    // ========== 监听位置列表变化 → 更新路线 ==========
    // isInitialized 确保地图就绪后才操作，locationList 变化时：
    //   - 有数据 → 调用 updateRoutesWithAnimation 首次逐点动画/后续增量绘制轨迹
    //   - 无数据 → 调用 clearAllRoutes 清除地图上所有已有路线
    LaunchedEffect(isInitialized, locationList) {
        if (isInitialized) {
            if(locationList.size == 0) {
                Log.d("JourneyMap3Routes", "locationList = ${locationList}")
            } else {
                Log.d("JourneyMap3Routes", "locationList = ${locationList.first()}, size = ${locationList.size}")
            }
            if (locationList.isNotEmpty()) {
                journeyMapViewModel.updateRoutesWithAnimation(locationList)
            } else {
                journeyMapViewModel.clearAllRoutes()
            }
        }
    }

    // ========== 录制状态变化时切换地图跟随模式 ==========
    // 录制开始 → 持续跟随用户（带面板补偿），录制结束 → 恢复为单次定位模式
    LaunchedEffect(isInitialized, recordingState.isRecording) {
        if (isInitialized) {
            if (recordingState.isRecording) {
                journeyMapViewModel.startFollowUser()
            } else {
                journeyMapViewModel.stopFollowUser()
            }
        }
    }
}