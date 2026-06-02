package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor1
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun ToFootprintButton(
    journey: Journey,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 1.dp,                 // 阴影高度
                shape = RoundedCornerShape(8.dp), // 圆角
                clip = true                        // 同时按照该形状裁剪内容
            )
            .background(
                color = SecondColor1,
            )
            .border(
                width = 1.dp,
                color = SecondColor3,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = {
                onPanelNavigate(JourneyPanel2State.FOOTPRINT_LIST, journey, null)
            }),
    ) {
        TextMedium(
            text = "前往足迹！",
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 12.dp)
        )
    }
}