package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item.JourneyItem
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State

@Composable
fun JourneyListView(
    journeyList: List<Journey>,
    navigate: (JourneyPanel2State, Journey?) -> Unit
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