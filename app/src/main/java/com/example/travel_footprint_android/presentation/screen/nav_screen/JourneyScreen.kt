package com.example.travel_footprint_android.presentation.screen.nav_screen

/**
 * JourneyScreen2 - 旅程主界面屏幕
 *
 * 【用途】
 *  - 作为应用的核心页面，展示用户的旅程足迹地图和旅程列表面板
 *  - 提供位置权限管理、地图展示、可拖拽设置按钮和趣味图片雨特效
 *
 * 【功能】
 *  - 1. 地图展示：使用 JourneyMap3 组件集成高德地图，显示用户当前位置和足迹路线
 *  - 2. 旅程面板：使用 JourneyPanel7 展示旅程列表，支持展开/折叠和多种状态切换
 *  - 3. 权限管理：检查并申请位置权限，无权限时显示友好的权限请求界面
 *  - 4. 图片雨特效：可配置的图片雨动画效果，支持多种参数调节（数量、大小、角度、旋转等）
 *  - 5. 设置对话框：通过可拖拽按钮打开图片雨设置面板，实时调节特效参数
 *
 * 【关联组件】
 *  - JourneyViewModel：提供旅程数据（journeys）、足迹计数（footprintCounts）、位置列表（LocationList）
 *  - JourneyMap3：高德地图集成组件，负责地图渲染和定位功能
 *  - JourneyPanel7：旅程面板组件，包含旅程列表、编辑、足迹列表、足迹编辑四种状态
 *  - ImageRain：图片雨特效组件，实现随机图片的下落动画效果
 *  - ButtonDraggable：可拖拽的悬浮按钮，用于触发设置对话框
 *  - RainSettingDialog：图片雨设置对话框，提供丰富的参数配置界面
 *  - PermissionRequestContent：权限请求界面，引导用户授予位置权限
 *
 * 【简单实现逻辑】
 *  - 1. 通过 Hilt 注入 JourneyViewModel，收集 UI 状态（旅程列表、位置列表等）
 *  - 2. 管理位置权限状态，使用 Activity Result API 处理权限请求回调
 *  - 3. 使用 animateFloatAsState 实现图片雨淡入淡出动画
 *  - 4. 维护多个 mutableStateOf 变量控制图片雨参数，并传递给 ImageRain 和 RainSettingDialog
 *  - 5. 布局采用 Box 容器，内部包含 Column（地图+面板）、ImageRain（特效层）、ButtonDraggable（按钮层）
 *  - 6. 条件渲染 showRainDialog 状态决定是否显示设置对话框
 */

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.image_random.ImageRain
import com.example.travel_footprint_android.presentation.components.journey_map.JourneyMap
import com.example.travel_footprint_android.presentation.components.journey_map.JourneyMapSplashScreen
import com.example.travel_footprint_android.presentation.components.journey_map.permission_request_content.PermissionRequestContent
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel
import com.example.travel_footprint_android.presentation.components.journey_map.weather.WeatherCard
import com.example.travel_footprint_android.presentation.components.journey_panel.JourneyPanel
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel

// 旅程主界面 Composable 函数：作为应用核心页面，整合地图、面板、特效层
// 通过 Hilt 注入 JourneyViewModel（页面级作用域）和 JourneyMapViewModel（Activity 级作用域）
@Composable
fun JourneyScreen(
    // journeyViewModel：管理旅程/足迹的增删改查，key="journey" 确保同一 ViewModel 实例
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"),
    // journeyMapViewModel：管理地图实例和定位服务，Activity 级作用域避免页面切换时重建
    journeyMapViewModel: JourneyMapViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        key = "JourneyMap3"
    ),
) {
    // journeyUiState：收集旅程 UI 状态流，包含旅程列表、足迹计数、位置列表等数据
    val journeyUiState by journeyViewModel.uiState.collectAsState()

    // aniTime：页面切换动画时长（400ms），用于后续面板动画
    val aniTime = remember { 400 }
    // sizeChange：标记面板尺寸是否已获取，避免重复打印日志
    var sizeChange by remember { mutableStateOf(false) }

    // context：当前 Android 上下文，用于权限检查和创建权限请求启动器
    val context = LocalContext.current
    // locationPermissions：需要申请的位置权限数组（精确定位 + 粗略定位）
    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    // hasLocationPermission：当前是否已授予所有位置权限，初始检查权限状态
    var hasLocationPermission by remember {
        mutableStateOf(locationPermissions.all { locationPermission ->
            ContextCompat
                .checkSelfPermission(context, locationPermission) ==
                    PackageManager.PERMISSION_GRANTED
        })
    }

    // permissionLauncher：Activity Result API 权限请求启动器，处理多权限请求回调
    // granted：权限请求结果 Map，key 为权限名，value 为是否授予
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        // 所有权限都授予时才设为 true
        hasLocationPermission = granted.values.all { it }
    }

    // showSplash：是否显示地图闪屏动画，ViewModel 级别状态避免页面切换时丢失
    val showSplash by journeyMapViewModel.showSplash.collectAsState()
    // showMapScreen：是否显示地图主界面，闪屏结束后由 ViewModel 控制
    val showMapScreen by journeyMapViewModel.showMapScreen.collectAsState()

    // ===== 主布局：Box 容器实现多层叠加效果 =====
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ===== Box 容器：地图全屏 + 面板覆盖 =====
        // JourneyMap 始终填满屏幕，不受面板位置影响
        if(showMapScreen) {
            if (hasLocationPermission) {
                JourneyMap(
                    locationList = journeyUiState.LocationList
                )
            } else {
                // 权限请求页面：限制在 JourneyPanel 上方的可用区域（屏幕上方 60%）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                ) {
                    PermissionRequestContent(
                        onRequestPermission = {
                            permissionLauncher.launch(locationPermissions)
                        }
                    )
                }
            }
        }
        // 闪屏动画：限制在 JourneyPanel 上方的可用区域（屏幕上方 60%）
        if(showSplash) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                JourneyMapSplashScreen(
                    onFinished = { journeyMapViewModel.setShowSplash(false) },
                    onShowScreen = { journeyMapViewModel.setShowMapScreen(true) }
                )
            }
        }

        // ===== 旅程面板：覆盖在地图上方，通过 offset 控制 Y 轴位置 =====
        JourneyPanel(
            modifier = Modifier
                .onSizeChanged { newSize ->
                    if (!sizeChange) {
                        sizeChange = true
                        Log.d("JourneyScreen2", "新的组件尺寸: 宽度 = ${newSize.width}, 高度 = ${newSize.height}")
                    }
                },
            aniTime = aniTime,
            journeyViewModel = journeyViewModel,
        )
        // ===== 天气卡片：位于面板上方（z-order），避免被面板遮挡 =====
        WeatherCard(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(12.dp))
        // ===== 图片雨特效层：覆盖在整个页面上方，作为装饰性背景特效 =====
        ImageRain()
    }
}

