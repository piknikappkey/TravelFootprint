package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark5
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun FootprintRating(
    footprint: Footprint,
    onRatingChange: (Int) -> Unit
) {
    TextMedium(
        text = "个人评分：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp),
        fontSize = 15.sp,
        color = FontDark5,
        )
    Spacer(Modifier.padding(2.dp))
    RatingSelector(
        rating = footprint.rating,
        onRatingChange = onRatingChange
    )
}

@Composable
fun RatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()                    // 1. 先填满父容器宽度
            .padding(horizontal = 24.dp),     // 2. 再留出左右内边距
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.wrapContentWidth()
        ) {
            for (i in 1..5) {
                Text(
                    text = if (i <= rating) "★" else "☆",
                    color = if (i <= rating) SecondColor3 else SecondColor3.copy(alpha = 0.5f),
                    fontSize = 32.sp,
                    modifier = Modifier
                        .clickable { onRatingChange(i) }
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}