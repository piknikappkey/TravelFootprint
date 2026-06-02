package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun FootprintEditCover(
    footprint: Footprint,
    onValueChange: (String) -> Unit,
) {
    TextMedium(
        text = "足迹标题：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp),
        color = FontDark4
    )
    Spacer(Modifier.padding(2.dp))
    InputText3(
        value = footprint.title,
        onValueChange = onValueChange,
        tipText = "请填写足迹标题",
        maxLength = 20
    )
}