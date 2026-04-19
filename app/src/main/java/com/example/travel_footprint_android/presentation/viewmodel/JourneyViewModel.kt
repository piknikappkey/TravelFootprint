// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/JourneyViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {

    // UI 状态
    data class JourneyUiState(
        val isLoading: Boolean = false,
        val journeys: List<Journey> = emptyList(),
        val footprintCounts: Map<Long, Int> = emptyMap(),
        val showAddDialog: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 并行加载数据
                val journeys = appService.getAllJourneys()
                val counts = appService.getAllFootprintCounts()

                journeys.collect { journeyList ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            journeys = journeyList,
                            footprintCounts = counts,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun createJourney(title: String, description: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.createJourney(title, "watercolor", description)
                hideAddDialog()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "创建失败"
                    )
                }
            }
        }
    }

    fun deleteJourney(journeyId: Long) {
        viewModelScope.launch {
            try {
                appService.deleteJourney(journeyId)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}