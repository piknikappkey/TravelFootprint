package com.example.travel_footprint_android.presentation.state

import com.example.travel_footprint_android.data.entity.Journey

data class JourneyListState(
    val isLoading: Boolean = false,
    val journeys: List<Journey> = emptyList(),
    val searchQuery: String = "",
    val selectedJourney: Journey? = null,
    val error: String? = null
)