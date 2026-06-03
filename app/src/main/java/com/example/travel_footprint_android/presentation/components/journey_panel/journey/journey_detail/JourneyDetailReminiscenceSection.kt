package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

@Composable
fun JourneyDetailReminiscenceSection(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
) {
    Column {
        TextMedium(
            text = "旅程回忆",
            firstLine = 0,
            modifier = Modifier.padding(horizontal = 15.dp),
            fontSize = 17.sp
        )
        Spacer(Modifier.padding(5.dp))
        Reminiscence(
            journey = journey,
            updateJourney = updateJourney
        )
    }
}
