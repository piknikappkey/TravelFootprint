package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.description

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

@Composable
fun JourneyEditDescription(
    journey: Journey,
    onValueChange: (String) -> Unit,
) {
    TextMedium(
        text = "旅程描述：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    Spacer(Modifier.padding(2.dp))
    InputText3(
        value = journey.description,
        onValueChange = onValueChange,
        tipText = "请填写旅程描述",
        maxLength = 1024
    )
}