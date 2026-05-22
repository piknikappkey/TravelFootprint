package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JourneyItem3(
    journey: Journey,
    journeyClick: (Journey) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 5.dp) // 外边距
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
            .clickable(onClick = { journeyClick(journey) })
    ) {
        BGImgBox(
            listOf(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(3.dp))
                ImageSquare2(
                    imgPath = journey.coverImagePath,
                    modifier = Modifier.width(100.dp).padding(horizontal = 5.dp),
                    aspectRatio = 1.2f,
                    addIconSize = .3f,
                    shape = RoundedCornerShape(5.dp)
                )
                RightContent(journey)
            }
        }
    }
}

@Composable
fun RightContent(
    journey: Journey,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 5.dp)
    ) {
        // 标题
        TextMedium(
            text = journey.title,
            fontSize = 16.sp,
        )

        Spacer(Modifier.padding(1.dp))

        // 描述（两行以内，多余用...代替）

        val truncatedDesc = if (journey.description.length > 14) {
            journey.description.substring(0, 14) + "... ..."
        } else {
            journey.description
        }

        TextSmall(
            modifier = Modifier
                .padding(start = 2.dp),
            text = truncatedDesc,
            color = FontDark5,
            firstLine = 1,
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
        val startYear = yearFormat.format(journey.startDate)

        val dateStr = if (currentYear == startYear) {
            // 同一年，只显示月日
            shortDateFormat.format(journey.startDate)
        } else {
            // 不同年，显示完整日期
            fullDateFormat.format(journey.startDate)
        }

        // 显示地址与日期
        Row {
            Spacer(Modifier.width(10.dp))
            TextSmall(
                text = journey.address.split("\n").last()
            )
            Spacer(Modifier.weight(1f))
            TextSmall(
                text = dateStr,
            )
            Spacer(Modifier.width(5.dp))
        }
    }
}