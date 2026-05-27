package com.example.travel_footprint_android.presentation2.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.ani_shade.AniShade
import com.example.travel_footprint_android.presentation2.components.journey_map3.JourneyMap3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.JourneyPanel7
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun JourneyScreen2(
    journeyViewModel: JourneyViewModel = hiltViewModel()
) {
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val journeys = journeyUiState.journeys
    val footprintCounts = journeyUiState.footprintCounts

    val aniTime = 400
    var sizeChange by remember { mutableStateOf(false) } // 容器大小改变

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f),
        ) {
            // 动画遮罩
//            JourneyShade(aniTime, sizeChange, { bool -> sizeChange = bool})
            // 高德地图
            JourneyMap3()
        }

        // 旅程面板
        JourneyPanel7(
            modifier = Modifier
                .onSizeChanged { newSize ->
                    // 每次尺寸变化时，这个 lambda 都会被调用
                    if(!sizeChange) {
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