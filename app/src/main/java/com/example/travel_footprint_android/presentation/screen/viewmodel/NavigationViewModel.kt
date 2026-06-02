package com.example.travel_footprint_android.presentation.screen.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travel_footprint_android.presentation.navigation.NavPath
import com.example.travel_footprint_android.presentation.navigation.NavPathObj
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    private val _currentDestination = MutableStateFlow(NavPathObj.list.first())
    val currentDestination: StateFlow<NavPath> = _currentDestination.asStateFlow()

    private var lastNavigateTime = 0L
    private val minInterval = 200L

    fun navigate(destination: NavPath) {
        val now = System.currentTimeMillis()
        if (now - lastNavigateTime > minInterval) {
            lastNavigateTime = now
            _currentDestination.value = destination
        }
    }
}