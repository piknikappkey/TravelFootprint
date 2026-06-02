package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun JourneyItemTitle(
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TextMedium(
            text = title,
            fontSize = 15.sp,
            color = FontDark4,
        )
    }
}
