package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun FootprintDescription(
    footprint: Footprint,
    onValueChange: (String) -> Unit,
) {
    TextMedium(
        text = "足迹描述：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp),
        color = FontDark4,
    )
    Spacer(Modifier.padding(2.dp))
    InputText3(
        value = footprint.description,
        onValueChange = onValueChange,
        tipText = "请填写足迹描述"
    )
}