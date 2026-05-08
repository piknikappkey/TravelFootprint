package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.location

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearch
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearchViewModel
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun JourneyEditLocation(
    journey: Journey,
    setJourney: (Journey) -> Unit,
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        locationSearchViewModel.setOnLocationSelectedCallback { latLng ->
            journeyMap3ViewModel.setSelectedLocation(latLng)
        }
    }

    TextMedium(
        text = "旅程地址：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    Spacer(Modifier.padding(2.dp))
    LocationSearch(
        locationSearchViewModel = locationSearchViewModel,
        onLocationSelected = { location ->
//            setJourney(
//                journey.copy(
//                    title = location.name,
//                    description = location.address
//                )
//            )
        }
    )
}