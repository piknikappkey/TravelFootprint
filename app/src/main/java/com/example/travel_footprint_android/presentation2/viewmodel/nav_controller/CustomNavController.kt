package com.example.travel_footprint_android.presentation2.viewmodel.nav_controller

import androidx.compose.runtime.mutableStateOf
import com.example.travel_footprint_android.presentation2.navigation.NavPath2
import com.example.travel_footprint_android.presentation2.navigation.NavPathObj2

object CustomNavController {
    private val _currentDestination = mutableStateOf(NavPathObj2.lighten)
    val currentDestination = _currentDestination

    private var lastNavigateTime = 0L
    private val minInterval = 200L // 最小间隔（毫秒）

    fun navigate(destination: NavPath2) {
        val now = System.currentTimeMillis()
        if (now - lastNavigateTime > minInterval) {
            lastNavigateTime = now
            _currentDestination.value = destination
        }
    }
}