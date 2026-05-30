package com.example.travel_footprint_android.presentation2.components.journey_panel2.location_button

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(key = "JourneyMap3"),
) {
    FloatingActionButton(
        onClick = {
            Log.d("JourneyPanel2", "LocationButton clicked, requesting new location")
            journeyMap3ViewModel.startLocation()
        },
        modifier = modifier
            .size(50.dp)
            .background(Color.Transparent)
            .padding(5.dp)
    ) {
        Icon(
            modifier = Modifier.padding(1.dp),
            imageVector = Icons.Default.MyLocation,
            contentDescription = "定位到当前位置"
        )
    }
}