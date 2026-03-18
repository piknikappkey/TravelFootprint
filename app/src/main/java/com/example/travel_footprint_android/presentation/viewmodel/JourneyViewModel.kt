// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/JourneyViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.domain.service.HandDrawStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val journeyRepository: JourneyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyListState())
    val uiState: StateFlow<JourneyListState> = _uiState.asStateFlow()

    init {
        loadJourneys()
    }

    /**
     * 加载所有旅程
     */
    fun loadJourneys() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                journeyRepository.getAllJourneys().collect { journeys ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            journeys = journeys,
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

    /**
     * 创建新旅程
     */
    fun createNewJourney(title: String, style: String = HandDrawStyle.WATERCOLOR.name) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                journeyRepository.createJourney(title, style)
                // 不需要手动刷新，Flow会自动更新
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

    /**
     * 删除旅程
     */
    fun deleteJourney(journeyId: Long) {
        viewModelScope.launch {
            try {
                journeyRepository.deleteJourneyWithAllData(journeyId)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }

    /**
     * 搜索旅程
     */
    fun searchJourneys(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            loadJourneys()
            return
        }

        viewModelScope.launch {
            try {
                val results = journeyRepository.searchJourneys(query)
                _uiState.update { state ->
                    state.copy(
                        journeys = results,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "搜索失败")
                }
            }
        }
    }

    /**
     * 选择旅程
     */
    fun selectJourney(journey: Journey) {
        _uiState.update { it.copy(selectedJourney = journey) }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}