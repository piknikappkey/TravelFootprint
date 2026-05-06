package com.example.travel_footprint_android.presentation2.components.journey_panel2.location_button

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.maps.CameraUpdateFactory
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel

@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(),
) {
    // 保存当前位置
    val currentLocation by journeyMap3ViewModel.currentLocation.collectAsState()
    // 保存地图实例
    val aMap by journeyMap3ViewModel.aMap.collectAsState()

    // 定位按钮：点击后将用户位置移动到地图中心，缩放级别为15
    FloatingActionButton(
        onClick = {
            Log.d("JourneyPanel2", "currentLocation = $currentLocation")
            Log.d("JourneyPanel2", "aMap = $aMap")
            currentLocation?.let { latLng ->
                aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        },
        modifier = modifier
            .padding(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "定位到当前位置"
        )
    }
}