package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item.JourneyItem3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State

@Composable
fun JourneyListView2(
    modifier: Modifier = Modifier,
    journeyList: List<Journey>,
    navigate: (JourneyPanel2State, Journey?) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(journeyList, key = { it.id }) { journey ->
            JourneyItem3(
                journey = journey,
                journeyClick = { journey ->
                    navigate(JourneyPanel2State.JOURNEY_LIST, journey)
                }
            )
        }
    }
}