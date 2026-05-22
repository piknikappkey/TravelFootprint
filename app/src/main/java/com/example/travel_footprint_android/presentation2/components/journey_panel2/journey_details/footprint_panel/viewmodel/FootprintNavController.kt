package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.example.travel_footprint_android.data.entity.Footprint

object FootprintNavController {
    private val _footprintNavController = mutableStateOf(FootprintPanel2State.FOOTPRINT_LIST)
    val footprintNavController = _footprintNavController

    private val _footprintData = mutableStateOf<Footprint?>(null)
    val footprintData = _footprintData

    fun navigate(destination: FootprintPanel2State, footprintData: Footprint? = null) {
        _footprintData.value = footprintData
        _footprintNavController.value = destination
    }

    fun init() {
        _footprintData.value = null
        _footprintNavController.value = FootprintPanel2State.FOOTPRINT_LIST
    }
}

enum class FootprintPanel2State {
    FOOTPRINT_LIST, // 足迹列表
    FOOTPRINT_DETAILS, // 足迹详情
    FOOTPRINT_EDIT, // 足迹新增/修改
}