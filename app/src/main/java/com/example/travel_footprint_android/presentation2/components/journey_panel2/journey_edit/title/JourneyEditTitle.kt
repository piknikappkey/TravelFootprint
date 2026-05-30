package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.title

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

@Composable
fun JourneyEditTitle(
    journey: Journey,
    onValueChange: (String) -> Unit,
) {
    // 旅程标题编辑
    TextMedium(
        text = "旅程标题：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    Spacer(Modifier.padding(2.dp))
    InputText3(
        value = journey.title,
        onValueChange = onValueChange,
        tipText = "请填写旅程标题",
        maxLength = 20,
    )
}