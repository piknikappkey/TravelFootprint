package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list.journey_item

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JourneyItemAddressDate(
    startDate: Long,
    address: String,
) {
    val fullDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val yearFormat = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
    val shortDateFormat = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }
    val currentYear = remember { yearFormat.format(Date()) }

    val location = remember(address) { address.split("\n").lastOrNull() ?: "" }

    val dateStr by remember {
        derivedStateOf {
            val startYear = yearFormat.format(startDate)
            if (currentYear == startYear) {
                shortDateFormat.format(startDate)
            } else {
                fullDateFormat.format(startDate)
            }
        }
    }
    val loc by remember {
        derivedStateOf {
            val l = location
            if (l.length > 10) l.substring(0, 10) + "..." else l
        }
    }

    Row {
        Spacer(Modifier.width(10.dp))
        TextSmall(text = loc)
        Spacer(Modifier.weight(1f))
        TextSmall(text = dateStr)
        Spacer(Modifier.width(5.dp))
    }
}
