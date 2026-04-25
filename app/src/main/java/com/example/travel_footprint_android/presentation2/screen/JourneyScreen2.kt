package com.example.travel_footprint_android.presentation2.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.journey_map3.JourneyMap3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.JourneyPanel2

@Composable
fun JourneyScreen2(
    journeyViewModel: JourneyViewModel = hiltViewModel(), // 自动注入
) {
    // 获取数据库数据
    val journeyUiState by journeyViewModel.uiState.collectAsState()

    // 读取旅程数据
    val journeys = journeyUiState.journeys

    // 读取足迹数据
    val footprintCounts = journeyUiState.footprintCounts

    Log.d("JourneyScreen2", "JourneyScreen2 start")
//    LaunchedEffect(journeys) {
//        val journey = Journey(
//            title = "旅程示例",
//            description = "旅程描述。。。。。。。。。。。。。。。。。。",
//            startDate = Date(),
//            endDate = Date(),
//            coverStyle = "",
//            coverImagePath = "",
//            journeyImagePaths = List<String>(0, { i -> "" })
//        )
//        Log.d("JourneyPanel2", "journeys.size = ${journeys.size}")
//        journeys.forEachIndexed { index, it ->
//            Log.d("JourneyPanel2", "journey = $it")
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        JourneyMap3(
            modifier = Modifier.weight(1f)
        )
//        JourneyMap2(Modifier.weight(1f))
        journeys.forEachIndexed { index, journey ->
            if(index == 0) {
                JourneyPanel2(Modifier, journey, { journey -> journeyViewModel.updateJourney(journey)})
            }
        }
    }
}