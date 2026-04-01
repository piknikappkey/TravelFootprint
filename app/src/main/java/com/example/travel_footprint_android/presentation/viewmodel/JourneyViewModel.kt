// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/JourneyViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.domain.service.HandDrawStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val journeyRepository: JourneyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyListState())
    val uiState: StateFlow<JourneyListState> = _uiState.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _footprintCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val footprintCounts: StateFlow<Map<Long, Int>> = _footprintCounts.asStateFlow()

    init {
        loadJourneys()
    }

    fun loadJourneys() {
        // 在 IO 线程执行数据库操作
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("JourneyVM", "开始加载旅程")

            try {
                // 获取旅程列表（在 IO 线程）
                val journeys = journeyRepository.getAllJourneys().first()
                android.util.Log.d("JourneyVM", "获取到 ${journeys.size} 个旅程")

                // 获取足迹数量（在 IO 线程）
                android.util.Log.d("JourneyVM", "开始加载足迹数量")
                val counts = journeyRepository.getFootprintCounts()
                android.util.Log.d("JourneyVM", "足迹数量: $counts")

                // 切换到主线程更新 UI
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            journeys = journeys,
                            error = null
                        )
                    }
                    _footprintCounts.value = counts
                }

            } catch (e: Exception) {
                android.util.Log.e("JourneyVM", "加载失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = e.message ?: "加载失败"
                        )
                    }
                }
            }
        }
    }

    fun showAddDialog() {
        _showAddDialog.update { true }
    }

    fun hideAddDialog() {
        _showAddDialog.update { false }
    }

    fun createNewJourney(title: String, description: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            try {
                journeyRepository.createJourney(
                    title = title,
                    style = HandDrawStyle.WATERCOLOR.name,
                    description = description
                )
                hideAddDialog()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = e.message ?: "创建失败"
                        )
                    }
                }
            }
        }
    }

    fun deleteJourney(journey: Journey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                journeyRepository.deleteJourneyWithAllData(journey.id)
                val counts = journeyRepository.getFootprintCounts()
                withContext(Dispatchers.Main) {
                    _footprintCounts.value = counts
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(error = e.message ?: "删除失败")
                    }
                }
            }
        }
    }

    fun selectJourney(journey: Journey) {
        _uiState.update { it.copy(selectedJourney = journey) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun getFootprintCountForJourney(journeyId: Long): Int {
        return _footprintCounts.value[journeyId] ?: 0
    }
}