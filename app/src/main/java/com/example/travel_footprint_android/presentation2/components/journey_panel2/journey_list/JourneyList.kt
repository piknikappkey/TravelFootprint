package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item.JourneyItem
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline

@Composable
fun JourneyList(
    journeyList: List<Journey>,
    navigate: (JourneyPanel2State, Journey?) -> Unit
) {
    // 固定标题
    Headline(
        text = "我的旅程",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
    )
    // 可滚动内容 - 双列布局
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth(.9f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(5.dp)
        ) {
            items(journeyList, key = { it.id }) { journey ->
                JourneyItem(
                    journey = journey,
                    journeyClick = { journey ->
                        navigate(JourneyPanel2State.JOURNEY_DETAILS, journey)
                    }
                )
            }
            item {
                JourneyItem(
                    journey = null,
                    journeyAdd = {
                        Log.d("JourneyList", "addJourneyClick")
                        navigate(JourneyPanel2State.JOURNEY_EDIT, null)
                    }
                )
            }
        }
    }
}