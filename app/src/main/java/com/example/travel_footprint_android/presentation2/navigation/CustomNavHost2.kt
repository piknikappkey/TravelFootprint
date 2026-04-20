package com.example.travel_footprint_android.presentation2.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.screen.JourneyScreen2
import com.example.travel_footprint_android.presentation2.screen.LightenScreen2
import com.example.travel_footprint_android.presentation2.viewmodel.nav_controller.CustomNavController

@Composable
fun CustomNavHost2(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 根据当前目的地显示不同的屏幕
        when (CustomNavController.currentDestination.value) {
            NavPathObj2.lighten -> LightenScreen2()
            NavPathObj2.journey -> JourneyScreen2()
        }
    }
}