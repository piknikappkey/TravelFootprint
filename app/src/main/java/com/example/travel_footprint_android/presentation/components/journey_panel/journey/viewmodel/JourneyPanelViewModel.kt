package com.example.travel_footprint_android.presentation.components.journey_panel.journey.viewmodel

import androidx.lifecycle.ViewModel
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * JourneyPanelViewModel - 旅程面板导航状态管理
 *
 * 将 JourneyPanel 的 panelState 从组件内部 remember 提升到 ViewModel，
 * 使 JourneyScreen 层级也能控制面板导航（例如从录制指示器跳转到足迹列表）。
 *
 * @param autoExpandFootprintId 自动展开的足迹 ID（-1L 表示无自动展开）
 */
@HiltViewModel
class JourneyPanelViewModel @Inject constructor() : ViewModel() {

    private val _panelState = MutableStateFlow(JourneyPanelState())
    val panelState: StateFlow<JourneyPanelState> = _panelState.asStateFlow()

    /** 自动展开的足迹 ID（-1L 表示无自动展开） */
    private val _autoExpandFootprintId = MutableStateFlow(-1L)
    val autoExpandFootprintId: StateFlow<Long> = _autoExpandFootprintId.asStateFlow()

    /** 导航到指定页面 */
    fun navigate(page: JourneyPanel2State, journey: Journey?, footprint: Footprint?) {
        _panelState.value = JourneyPanelState(
            currentPage = page,
            selectedJourney = journey,
            selectedFootprint = footprint,
        )
    }

    /** 设置自动展开的足迹 ID */
    fun setAutoExpandFootprintId(id: Long) {
        _autoExpandFootprintId.value = id
    }

    /** 清除自动展开 */
    fun clearAutoExpandFootprintId() {
        _autoExpandFootprintId.value = -1L
    }
}
