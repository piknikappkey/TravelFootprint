package com.example.travel_footprint_android.presentation.components.image_random.setting_dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

@Composable
fun InputFieldNumber(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tipText: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextMedium(
            text = label,
            fontSize = 13.sp,
        )
        InputText3(
            value = value,
            onValueChange = onValueChange,
            tipText = tipText,
            padding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier.weight(1f).scale(.8f),
        )
    }
}
