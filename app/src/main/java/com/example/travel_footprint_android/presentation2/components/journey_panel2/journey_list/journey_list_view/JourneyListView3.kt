package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item.JourneyItem4
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State

@Composable
fun JourneyListView3(
    modifier: Modifier = Modifier,
    journeyList: List<Journey>,
    journeySelected: Journey?,
    updateJourney: (Journey) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .padding(bottom = if(journeySelected == null) 70.dp else 0.dp)
    ) {
        itemsIndexed(journeyList, key = { i, it -> it.id}) { i, it ->
            if(journeySelected == null || journeySelected.id == it.id) {
                JourneyItem4(
                    journey = it,
                    journeyClick = {
                        JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, it)
                    },
                    showDetail = (journeySelected?.id == it.id),
                    updateJourney
                )
            }
        }
    }
}