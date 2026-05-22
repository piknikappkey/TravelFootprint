package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation.viewmodel.MapViewModel
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_details.FootprintDetails
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_edit.FootprintEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list.FootprintList
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State.FOOTPRINT_DETAILS
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State.FOOTPRINT_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State.FOOTPRINT_LIST

@Composable
fun FootprintPanel(
    mapViewModel: MapViewModel = hiltViewModel(),
    journeyViewModel: JourneyViewModel = hiltViewModel(),
    journeySelected: Journey,
) {
    // 足迹数据
    val footprintUiState by mapViewModel.uiState.collectAsState()

    // 读取足迹数据
    val footprints = footprintUiState.footprints

    // 面板状态
    val footprintPanel2State = FootprintNavController.footprintNavController.value

    val footprintData = FootprintNavController.footprintData.value

    // 初始化足迹数据
    LaunchedEffect(journeySelected) {
        mapViewModel.loadJourneyFootprints(journeySelected.id)
        FootprintNavController.init()
    }

    Box(
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(durationMillis = 300)
            )
    ) {
        when(footprintPanel2State) {
            FOOTPRINT_LIST -> {
                FootprintList(footprints, journeySelected)
            }
            FOOTPRINT_DETAILS -> {
                footprintData?.let { FootprintDetails(it, journeySelected,) }
            }
            FOOTPRINT_EDIT -> {
                FootprintEdit(footprintData, journeySelected, { footprint -> journeyViewModel.addFootprintsForJourney(journeySelected, footprint)}, { footprint -> })
            }
        }
    }
}