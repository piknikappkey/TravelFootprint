package com.example.travel_footprint_android.presentation2.viewmodel.NavController

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.travel_footprint_android.presentation2.navigation.NavPath2
import com.example.travel_footprint_android.presentation2.navigation.NavPathObj2

class CustomNavController {
    private val _currentDestination = mutableStateOf<NavPath2>(NavPathObj2.lighten)
    val currentDestination: MutableState<NavPath2> = _currentDestination

    fun navigate(destination: NavPath2) {
        _currentDestination.value = destination
    }
}