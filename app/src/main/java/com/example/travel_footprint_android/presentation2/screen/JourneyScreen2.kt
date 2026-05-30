package com.example.travel_footprint_android.presentation2.screen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.ani_shade.AniShade
import com.example.travel_footprint_android.presentation2.components.journey_map3.JourneyMap3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.JourneyPanel7
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.fillMaxSize(0.3f),
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "地图图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "需要位置权限",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "为了在地图上显示你的位置，\n需要获取设备的位置信息权限",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondColor3
                )
            ) {
                Text("授予位置权限", color = Color.White)
            }
        }
    }
}

@Composable
fun JourneyShade(
    aniTime: Int,
    sizeChange: Boolean,
    setSizeChange: (Boolean) -> Unit,
) {
    AniShade(
        aniStart = sizeChange,
        aniOverFunc = {
            setSizeChange(false)
        },
        aniTime = aniTime.toLong() + 200,
        shadeShowTime = 0,
        shadeStopTime = aniTime.toLong(),
        shadeHideTime = 100
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = BGLight0,
                )
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(.4f),
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "地图图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
        }
    }
}
