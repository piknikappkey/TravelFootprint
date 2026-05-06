package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_details.FootprintDetails
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_edit.FootprintEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list.FootprintList
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State.FOOTPRINT_DETAILS
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State.FOOTPRINT_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State.FOOTPRINT_LIST

@Composable
fun FootprintPanel() {
    // 面板状态
    val footprintPanel2State = FootprintNavController.footprintNavController.value

    // 当前选中的旅程
    val footprintSelected = FootprintNavController.footprintData.value

    Box {
        when(footprintPanel2State) {
            FOOTPRINT_LIST -> {
                FootprintList()
            }
            FOOTPRINT_DETAILS -> {
                FootprintDetails()
            }
            FOOTPRINT_EDIT -> {
                FootprintEdit()
            }
        }
    }
}