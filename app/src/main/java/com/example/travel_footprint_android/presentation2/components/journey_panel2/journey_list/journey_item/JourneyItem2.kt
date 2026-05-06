package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.BGLight3
import com.example.travel_footprint_android.ui.theme.FontDark4
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JourneyItem2(
    journey: Journey?,
    journeyClick: (Journey) -> Unit = {},
    journeyAdd: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 5.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp),
                clip = false
            )
            .background(
                color = BGLight3,
                shape = RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)
            )
            .clickable(onClick = { if(journey != null) journeyClick(journey) else journeyAdd() })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 10.dp)
                .alpha(if (journey == null) 0f else 1f)
        ) {
            // 标题
            TextMedium(
                text = journey?.title ?: " ",
                fontSize = 15.sp,
            )

            Spacer(Modifier.padding(1.dp))
            
            // 描述（两行以内，多余用...代替）
            val truncatedDesc = if ((journey?.description?.length ?: 0) > 14) {
                journey?.description?.substring(0, 14) + "... ..."
            } else {
                journey?.description
            }

            TextSmall(
                modifier = Modifier
                    .padding(start = 2.dp),
                text = truncatedDesc ?: " ",
                color = FontDark4,
                fontSize = 14.sp,
                minLines = 2,
                maxLines = 2,
            )

            Spacer(Modifier.padding(2.dp))
            
            // 开始日期
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val shortDateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
            
            val currentYear = yearFormat.format(Date())
            if(journey != null) {
                val startYear = yearFormat.format(journey.startDate)

                val dateStr = if (currentYear == startYear) {
                   // 同一年，只显示月日
                   shortDateFormat.format(journey.startDate)
                } else {
                   // 不同年，显示完整日期
                   fullDateFormat.format(journey.startDate)
                }
            }
            val dateStr = if (journey != null) {
                val startYear = yearFormat.format(journey.startDate)

                if (currentYear == startYear) {
                    // 同一年，只显示月日
                    shortDateFormat.format(journey.startDate)
                } else {
                    // 不同年，显示完整日期
                    fullDateFormat.format(journey.startDate)
                }
            } else {
                " "
            }
            TextSmall(
                modifier = Modifier
                    .fillMaxWidth(),
                text = dateStr,
                textAlign = TextAlign.End,
            )
        }
        if(journey == null) {
            IconAdd(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}