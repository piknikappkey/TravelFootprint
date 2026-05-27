package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item.JourneyItem5
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State

@Composable
fun JourneyListView4(
    modifier: Modifier = Modifier,
    journeyList: List<Journey>,
    journeySelected: Journey?,
    updateJourney: (Journey) -> Unit,
) {
    val starTime = System.currentTimeMillis()

    LazyColumn(
        modifier = modifier
    ) {
        items(journeyList, key = { it.id }) { journey ->
            if(journeySelected == null || journeySelected.id == journey.id) {
                JourneyItem5(
                    journey = journey,
                    journeyClick = { JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, journey) },
                    showDetail = (journeySelected?.id == journey.id),
                    updateJourney = updateJourney
                )
            }
        }
        item {
            Spacer(Modifier.height(70.dp))
        }
    }
    Log.d("ComposeTime", "JourneyListView4: ${System.currentTimeMillis() - starTime}")
}