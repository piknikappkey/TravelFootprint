package com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.example.travel_footprint_android.data.entity.Journey

object JourneyNavController {
    private val _journeyNavController = mutableStateOf(JourneyPanel2State.JOURNEY_LIST)
    val journeyNavController = _journeyNavController

    private val _journeyData = mutableStateOf<Journey?>(null)
    val journeyData = _journeyData

    fun navigate(destination: JourneyPanel2State, journeyData: Journey? = null) {
        _journeyData.value = journeyData
        _journeyNavController.value = destination
    }
}

enum class JourneyPanel2State {
    JOURNEY_LIST, // 旅程列表
    JOURNEY_DETAILS, // 旅程详情
    JOURNEY_EDIT, // 旅程新增/修改
}