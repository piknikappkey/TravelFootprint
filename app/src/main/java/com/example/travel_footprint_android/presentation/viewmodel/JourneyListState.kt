// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/JourneyListState.kt
package com.example.travel_footprint_android.presentation.viewmodel

import com.example.travel_footprint_android.data.entity.Journey

data class JourneyListState(
    val isLoading: Boolean = false,
    val journeys: List<Journey> = emptyList(),
    val searchQuery: String = "",
    val selectedJourney: Journey? = null,
    val error: String? = null
)