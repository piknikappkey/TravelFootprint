package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey

@Composable
internal fun JourneyItemRightContent(
    journey: Journey,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 5.dp)
    ) {
        JourneyItemTitle(
            title = journey.title,
        )

        Spacer(Modifier.padding(1.dp))

        JourneyItemDescription(
            description = journey.description,
        )

        Spacer(Modifier.padding(2.dp))

        JourneyItemAddressDate(
            startDate = journey.startDate.time,
            address = journey.address,
        )
    }
}
