package com.example.travel_footprint_android.presentation2.screen

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
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.button.button_draggable.ButtonDraggable
import com.example.travel_footprint_android.presentation2.components.button.button_draggable.RainSettingDialog
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.image_random.ImageRain
import com.example.travel_footprint_android.presentation2.components.journey_map3.JourneyMap3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.JourneyPanel7
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.SecondColor3
import com.example.travel_footprint_android.utils.PermissionUtils

// 旅程主界面 Composable 函数，通过 Hilt 注入 JourneyViewModel
@Composable
fun JourneyScreen2(
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey")
) {
    // 从 ViewModel 收集 UI 状态，获取旅程列表和足迹计数
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val journeys = journeyUiState.journeys
    val footprintCounts = journeyUiState.footprintCounts

    // 定义动画时长和界面状态变量
    val aniTime = 400
    var sizeChange by remember { mutableStateOf(false) }
    var rainEnabled by remember { mutableStateOf(true) }
    var showRainDialog by remember { mutableStateOf(false) }

    // 获取上下文和位置权限数组
    val context = LocalContext.current
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    // 检查初始权限状态
    var hasLocationPermission by remember {
        mutableStateOf(PermissionUtils.hasPermissions(context, locationPermissions))
    }

    // 创建权限请求启动器，处理权限请求结果
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        hasLocationPermission = granted.values.all { it }
    }

    // 图片雨透明度动画：根据 rainEnabled 状态在 1f 和 0f 之间切换
    val aniImgRainAlpha by animateFloatAsState(
        targetValue = if (rainEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "aniImgRainAlpha"
    )
    // 图片雨参数状态变量：控制图片数量、尺寸、角度、旋转等属性
    var isChaos by remember { mutableStateOf(false) }
    var rainMaxImages by remember { mutableStateOf(10) }
    var rainIntervalMs by remember { mutableStateOf(1000L) }
    var rainMinExistenceTime by remember { mutableStateOf(10000) }
    var rainMaxExistenceTime by remember { mutableStateOf(20000) }
    var rainMinSize by remember { mutableStateOf(30) }
    var rainMaxSize by remember { mutableStateOf(50) }
    var rainMinAngle by remember { mutableStateOf(0) }
    var rainMaxAngle by remember { mutableStateOf(360) }
    var rainPressScale by remember { mutableStateOf(20f) }
    var rainRotationSpeed by remember { mutableStateOf(30f) }
    var rainClearAllTrigger by remember { mutableStateOf(0) }

    // 主布局容器：使用 Box 实现多层叠加效果
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 垂直布局：上方是地图区域，下方是旅程面板
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
        ) {
            // 地图区域：占据剩余空间，根据权限状态显示地图或权限请求界面
            Box(
                modifier = Modifier.weight(1f),
            ) {
                if (hasLocationPermission) {
                    // 有权限时显示地图组件，传入位置列表用于绘制路线
                    JourneyMap3(
                        locationList = journeyUiState.LocationList
                    )
                } else {
                    // 无权限时显示权限请求界面，用户点击按钮触发权限申请
                    PermissionRequestContent(
                        onRequestPermission = {
                            permissionLauncher.launch(locationPermissions)
                        }
                    )
                }
            }

            // 旅程面板组件：显示旅程列表，支持拖拽调整高度
            JourneyPanel7(
                modifier = Modifier
                    .onSizeChanged { newSize ->
                        // 仅记录一次组件尺寸，用于调试
                        if (!sizeChange) {
                            sizeChange = true
                            Log.d("JourneyScreen2", "新的组件尺寸: 宽度 = ${newSize.width}, 高度 = ${newSize.height}")
                        }
                    },
                aniTime = aniTime,
                journeyList = journeys,
                journeyViewModel = journeyViewModel,
            )
        }

        // 图片雨特效层：覆盖整个屏幕，通过 alpha 控制可见性
        ImageRain(
            modifier = Modifier
                .fillMaxSize()
                .alpha(aniImgRainAlpha),
            isChaos = isChaos,
            maxImages = rainMaxImages,
            intervalMs = rainIntervalMs,
            minExistenceTime = rainMinExistenceTime,
            maxExistenceTime = rainMaxExistenceTime,
            minSize = rainMinSize,
            maxSize = rainMaxSize,
            minAngle = rainMinAngle,
            maxAngle = rainMaxAngle,
            pressScale = rainPressScale,
            rotationSpeed = rainRotationSpeed,
            clearAllTrigger = rainClearAllTrigger,
        )

        // 可拖拽设置按钮：点击打开图片雨设置对话框
        ButtonDraggable(
            modifier = Modifier.fillMaxSize(),
            onClick = { showRainDialog = true },
            showRainDialog = showRainDialog
        )
    }

    // 条件渲染：当 showRainDialog 为 true 时显示设置对话框
    if (showRainDialog) {
        RainSettingDialog(
            rainEnabled = rainEnabled,
            onRainEnabledChange = { rainEnabled = it },
            onClearAll = { rainClearAllTrigger++ },
            onDismiss = { showRainDialog = false },
            // 双向绑定所有图片雨参数到对话框
            isChaos = isChaos,
            onIsChaosChange = { isChaos = it },
            maxImages = rainMaxImages,
            onMaxImagesChange = { rainMaxImages = it },
            intervalMs = rainIntervalMs,
            onIntervalMsChange = { rainIntervalMs = it },
            minExistenceTime = rainMinExistenceTime,
            onMinExistenceTimeChange = { rainMinExistenceTime = it },
            maxExistenceTime = rainMaxExistenceTime,
            onMaxExistenceTimeChange = { rainMaxExistenceTime = it },
            minSize = rainMinSize,
            onMinSizeChange = { rainMinSize = it },
            maxSize = rainMaxSize,
            onMaxSizeChange = { rainMaxSize = it },
            minAngle = rainMinAngle,
            onMinAngleChange = { rainMinAngle = it },
            maxAngle = rainMaxAngle,
            onMaxAngleChange = { rainMaxAngle = it },
            pressScale = rainPressScale,
            onPressScaleChange = { rainPressScale = it },
            rotationSpeed = rainRotationSpeed,
            onRotationSpeedChange = { rainRotationSpeed = it },
        )
    }
}

// 权限请求界面组件：当用户未授予位置权限时显示
@Composable
fun PermissionRequestContent(onRequestPermission: () -> Unit) {
    // 全屏背景容器，居中对齐内容
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BGLight0),
        contentAlignment = Alignment.Center
    ) {
        // 垂直排列的权限请求内容，向上偏移 50dp
        Column(
            modifier = Modifier.offset(y = (-50).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 显示地图图标，使用主题色进行着色
            Image(
                modifier = Modifier.fillMaxSize(0.3f),
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "地图图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 显示标题文本
            Headline(
                text = "需要位置权限",
            )
            Spacer(modifier = Modifier.height(10.dp))
            // 显示说明文本，告知用户为何需要位置权限
            TextMedium(
                text = "为了在地图上显示你的位置，\n需要获取设备的位置信息权限",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 授权按钮：点击触发权限请求回调
            ButtonMain(
                onClick = onRequestPermission,
                bgColor = SecondColor3,
                paddingValues = PaddingValues(vertical = 5.dp, horizontal = 10.dp)
            ) {
                Headline(
                    text = "授予位置权限",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}