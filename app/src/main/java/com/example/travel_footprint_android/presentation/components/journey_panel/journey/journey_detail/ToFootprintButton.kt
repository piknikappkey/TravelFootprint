package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.button.button_border.ButtonBorder
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

@Composable
fun ToFootprintButton(
    journey: Journey,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    ButtonBorder(
        onClick = {
            onPanelNavigate(JourneyPanel2State.FOOTPRINT_LIST, journey, null)
        }
    ) {
        TextMedium(
            text = "前往足迹！",
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}