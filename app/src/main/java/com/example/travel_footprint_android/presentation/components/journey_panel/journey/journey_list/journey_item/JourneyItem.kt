package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list.journey_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.image_square.ImageSquare2

@Composable
fun JourneyItem(
    journey: Journey,
    journeyClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 25.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
            .clickable(onClick = journeyClick)
    ) {
        BGImgBox(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(3.dp))
                if(journey.coverImagePath != "") {
                    ImageSquare2(
                        imgPath = journey.coverImagePath,
                        modifier = Modifier.width(110.dp),
                        aspectRatio = 1.2f,
                        addIconSize = .3f,
                        shape = RoundedCornerShape(5.dp),
                    )
                }
                JourneyItemRightContent(journey)
            }
        }
    }
}
