package com.example.travel_footprint_android.presentation2.components.journey_map3

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.MapView
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun JourneyMap3(
    modifier: Modifier = Modifier,
    viewModel: JourneyMap3ViewModel = hiltViewModel(),
) {
    val isInitialized by viewModel.isInitialized.collectAsState()
    val mapView = viewModel.getMapView()

    // 管理 MapView 的生命周期
    DisposableEffect(key1 = mapView) {
        mapView?.onCreate(null)
        mapView?.onResume()
        
        onDispose {
            mapView?.onPause()
        }
    }

    AndroidView(
        factory = { ctx ->
            mapView ?: MapView(ctx)
        },
        modifier = modifier.fillMaxSize()
    )

    LaunchedEffect(Unit) {
        if (isInitialized) {
            viewModel.startLocation()
        }
    }
}