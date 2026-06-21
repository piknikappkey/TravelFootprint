package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list.journey_item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark5

@Composable
fun JourneyItemDescription(
    description: String,
) {
    val truncatedDesc by remember {
        derivedStateOf {
            val desc = description
            if (desc.length > 14) desc.substring(0, 14) + "... ..." else desc
        }
    }

    TextSmall(
        text = truncatedDesc,
        color = FontDark5,
        firstLine = 1,
        fontSize = 14.sp,
        maxLines = 2,
    )
}
