package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation.components.journey_map.location_search.LocationSearch
import com.example.travel_footprint_android.presentation.components.journey_map.location_search.LocationSearchViewModel
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.LocationPanel
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun FootprintEditLocation(
    footprint: Footprint,
    setFootprint: (Footprint) -> Unit,
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    journeyMapViewModel: JourneyMapViewModel = hiltViewModel(),
) {
    var isSelectedLocation by remember { mutableStateOf(footprint.address != "") }

    var name by remember { mutableStateOf(if(footprint.address != "") footprint.address.split("\n")[0] else "") }

    var address by remember { mutableStateOf(if(footprint.address != "") footprint.address.split("\n").last() else "") }

    var latitude by remember { mutableStateOf(footprint.latitude) }

    var longitude by remember { mutableStateOf(footprint.longitude) }

    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        locationSearchViewModel.setOnLocationSelectedCallback { latLng ->
            journeyMapViewModel.setSelectedLocation(latLng)
        }
    }

    TextMedium(
        text = "地址：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp),
        color = FontDark4,
    )

    Spacer(Modifier.padding(2.dp))
    LocationSearch(
        locationSearchViewModel = locationSearchViewModel,
        onLocationSelected = { location ->
            Log.d("FootprintEditLocation", "location = $location")
                isSelectedLocation = true
                showButton = true
                name = location.name
                address = location.address
                latitude = location.latitude
                longitude = location.longitude
        }
    )

    Spacer(Modifier.padding(2.dp))
    if(isSelectedLocation) {
        LocationPanel(
            name = name,
            address = address,
            showButton = showButton,
            submit = {
                Log.d("FootprintEditLocation", "submit")
                Log.d("FootprintEditLocation", "showButton = $showButton")
                setFootprint(
                    footprint.copy(
                        address = "${address}\n${name}",
                        latitude = latitude,
                        longitude = longitude
                    )
                )
                showButton = false
                Log.d("FootprintEditLocation", "showButton = $showButton")
            },
            cancel = {
                Log.d("FootprintEditLocation", "cancel")
                Log.d("FootprintEditLocation", "journey.address = ${footprint.address}")
                if (footprint.address != "") {
                    Log.d("FootprintEditLocation", "footprint.address != \"\"")
                    name = footprint.address.split("\n")[0]
                    address = footprint.address.split("\n").last()
                    latitude = footprint.latitude
                    longitude = footprint.longitude
                    isSelectedLocation = true
                    Log.d("FootprintEditLocation", "name = $name")
                    Log.d("FootprintEditLocation", "address = $address")
                } else {
                    Log.d("FootprintEditLocation", "footprint.address == \"\"")
                    name = ""
                    address = ""
                    latitude = 0.0
                    longitude = 0.0
                    isSelectedLocation = false
                }
                showButton = false
            },
            delete = {
                showButton = false
                isSelectedLocation = false
                name = ""
                address = ""
                latitude = 0.0
                longitude = 0.0
                setFootprint(
                    footprint.copy(
                        address = "",
                        latitude = 0.0,
                        longitude = 0.0
                    )
                )
            },
            latitude = latitude,
            longitude = longitude
        )
    }
}