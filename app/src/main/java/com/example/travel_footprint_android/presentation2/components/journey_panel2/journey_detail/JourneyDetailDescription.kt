package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun JourneyDetailDescription(
    description: String,
) {
    Column {
        TextMedium(
            text = "旅程描述：",
            firstLine = 0,
            modifier = Modifier.padding(horizontal = 15.dp),
            fontSize = 17.sp
        )
        Spacer(Modifier.padding(2.dp))
    }

    TextSmall(
        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
        text = description,
        color = FontDark4,
        firstLine = 1,
        fontSize = 16.sp,
        minLines = 1,
        maxLines = Int.MAX_VALUE,
    )
}
