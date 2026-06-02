package com.example.travel_footprint_android.presentation2.components.map_preloader

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.MapView
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun MapPreloader(
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        key = "JourneyMap3"
    )
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (journeyMap3ViewModel.getMapView() == null) {
            val mapView = MapView(context).apply {
                onCreate(null)
            }
            journeyMap3ViewModel.initializeMap(mapView)
        }
    }
}
