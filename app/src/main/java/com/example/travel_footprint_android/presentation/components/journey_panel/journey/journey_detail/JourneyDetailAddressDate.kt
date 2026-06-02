package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.journey_panel.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun JourneyDetailAddressDate(
    startDate: Long,
    address: String,
) {
    val fullDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val addressParts = remember(address) { address.split("\n") }
    val region = addressParts.firstOrNull() ?: ""
    val location = addressParts.lastOrNull() ?: ""

    Column {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            val dateStr = fullDateFormat.format(startDate)
            Spacer(Modifier.width(10.dp))
            TextSmall(
                text = dateStr,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(0.dp)
                    .offset(y = 5.dp)
            )
            Spacer(Modifier.weight(1f))
            Column {
                TextSmall(
                    text = location,
                    firstLine = 0,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
                TextSmall(
                    text = region,
                    firstLine = 2,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 15.dp),
                )
            }
        }
        LineBetween()
    }
}
