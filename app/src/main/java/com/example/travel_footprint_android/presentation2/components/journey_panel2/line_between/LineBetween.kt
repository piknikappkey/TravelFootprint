package com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.line.Line
import com.example.travel_footprint_android.ui.theme.SecondColor1

@Composable
fun LineBetween(
    color: Color = SecondColor1,
    dashLength: Float = 18f,
    gapLength: Float = 6f,
    thickness: Float = 1f,
    paddingUp: Dp = 6.dp,
    paddingDown: Dp = 6.dp,
    lineLength: Float = .95f,
) {
    Spacer(Modifier.padding(paddingUp))
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Line(
            Modifier
                .fillMaxWidth(lineLength),
            color,
            dashLength,
            gapLength,
            thickness
        )
    }
    Spacer(Modifier.padding(paddingDown))
}