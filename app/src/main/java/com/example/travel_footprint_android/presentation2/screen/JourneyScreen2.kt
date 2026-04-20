package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.journey_map2.JourneyMap2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.JourneyPanel2

@Composable
fun JourneyScreen2() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        JourneyMap2(Modifier.weight(1f))
        JourneyPanel2()
    }
}