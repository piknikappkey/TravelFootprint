package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list.journey_item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
internal fun JourneyItemRightContent(
    journey: Journey,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 5.dp)
    ) {
        TextMedium(
            text = journey.title,
            fontSize = 15.sp,
            color = FontDark4,
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
