package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.images

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

@Composable
fun JourneyEditImages(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
) {
    TextMedium(
        text = "旅程回忆：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    Spacer(Modifier.padding(2.dp))
    Reminiscence(
        journey = journey,
        updateJourney = updateJourney,
        showDelIcon = true,
    )
}