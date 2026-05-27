package com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey

object JourneyNavController {
    private val _journeyNavController = mutableStateOf(JourneyPanel2State.JOURNEY_LIST)
    val journeyNavController = _journeyNavController

    private val _journeyData = mutableStateOf<Journey?>(null)
    val journeyData = _journeyData

    private val _footprintData = mutableStateOf<Footprint?>(null)
    val footprintData = _footprintData

    fun navigate(destination: JourneyPanel2State, journeyData: Journey? = null, footprintData: Footprint? = null) {
        _footprintData.value = footprintData
        _journeyData.value = journeyData
        _journeyNavController.value = destination
    }
}

enum class JourneyPanel2State {
    JOURNEY_LIST, // 旅程列表
//    JOURNEY_DETAILS, // 旅程详情
    JOURNEY_EDIT, // 旅程新增/修改
    FOOTPRINT_LIST, // 足迹列表
    FOOTPRINT_EDIT, // 足迹新增/修改
}