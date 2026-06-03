package com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel

import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey

data class JourneyPanelState(
    val currentPage: JourneyPanel2State = JourneyPanel2State.JOURNEY_LIST,
    val selectedJourney: Journey? = null,
    val selectedFootprint: Footprint? = null,
)

enum class JourneyPanel2State {
    JOURNEY_LIST,
    JOURNEY_DETAIL,
    JOURNEY_EDIT,
    FOOTPRINT_LIST,
    FOOTPRINT_EDIT,
}
