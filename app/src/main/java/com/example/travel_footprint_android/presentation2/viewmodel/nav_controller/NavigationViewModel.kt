package com.example.travel_footprint_android.presentation2.viewmodel.nav_controller

import androidx.lifecycle.ViewModel
import com.example.travel_footprint_android.presentation2.navigation.NavPath2
import com.example.travel_footprint_android.presentation2.navigation.NavPathObj2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    private val _currentDestination = MutableStateFlow(NavPathObj2.list.first())
    val currentDestination: StateFlow<NavPath2> = _currentDestination.asStateFlow()

    private var lastNavigateTime = 0L
    private val minInterval = 200L

    fun navigate(destination: NavPath2) {
        val now = System.currentTimeMillis()
        if (now - lastNavigateTime > minInterval) {
            lastNavigateTime = now
            _currentDestination.value = destination
        }
    }
}
