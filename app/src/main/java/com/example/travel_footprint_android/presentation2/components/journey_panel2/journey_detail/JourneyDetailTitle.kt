package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark3

@Composable
fun JourneyDetailTitle(
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextMedium(
            text = title,
            fontSize = 20.sp,
            color = FontDark3,
        )
    }
}
