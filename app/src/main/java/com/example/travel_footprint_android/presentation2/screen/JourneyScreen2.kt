package com.example.travel_footprint_android.presentation2.screen

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

@Composable
fun JourneyScreen2(
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey")
) {
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val journeys = journeyUiState.journeys
    val footprintCounts = journeyUiState.footprintCounts

    val aniTime = 400
    var sizeChange by remember { mutableStateOf(false) }
    var rainEnabled by remember { mutableStateOf(true) }
    var showRainDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var hasLocationPermission by remember {
        mutableStateOf(PermissionUtils.hasPermissions(context, locationPermissions))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        hasLocationPermission = granted.values.all { it }
    }

    // 图片雨透明度
    val aniImgRainAlpha by animateFloatAsState(
        targetValue = if (rainEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "aniImgRainAlpha"
    )
    // 随机图片拖拽混乱模式
    var isChaos by remember { mutableStateOf(false) }
    var rainMaxImages by remember { mutableStateOf(10) }
    var rainIntervalMs by remember { mutableStateOf(1000L) }
    var rainMinExistenceTime by remember { mutableStateOf(10000) }
    var rainMaxExistenceTime by remember { mutableStateOf(20000) }
    var rainMinSize by remember { mutableStateOf(30) }
    var rainMaxSize by remember { mutableStateOf(50) }
    var rainMinAngle by remember { mutableStateOf(0) }
    var rainMaxAngle by remember { mutableStateOf(360) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
        ) {
            Box(
                modifier = Modifier.weight(1f),
            ) {
                if (hasLocationPermission) {
                    JourneyMap3(
                        locationList = journeyUiState.LocationList
                    )
                } else {
                    PermissionRequestContent(
                        onRequestPermission = {
                            permissionLauncher.launch(locationPermissions)
                        }
                    )
                }
            }

            JourneyPanel7(
                modifier = Modifier
                    .onSizeChanged { newSize ->
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
        )

        ButtonDraggable(
            modifier = Modifier.fillMaxSize(),
            onClick = { showRainDialog = true },
            showRainDialog = showRainDialog
        ) {
            Headline(
                text = "\u2699",
                fontSize = 24.sp,
                color = Color.White,
                letterSpacing = 0.sp,
            )
        }
    }

    if (showRainDialog) {
        RainSettingDialog(
            rainEnabled = rainEnabled,
            onRainEnabledChange = { rainEnabled = it },
            onDismiss = { showRainDialog = false },
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
        )
    }
}

@Composable
fun PermissionRequestContent(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BGLight0),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.offset(y = (-50).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.fillMaxSize(0.3f),
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "地图图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Headline(
                text = "需要位置权限",
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextMedium(
                text = "为了在地图上显示你的位置，\n需要获取设备的位置信息权限",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
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