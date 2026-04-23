// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/JourneyViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import android.net.Uri
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
import java.util.Date
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
        val showEditDialog: Boolean = false,
        val editingJourney: Journey? = null,
        val showDeleteConfirmDialog: Boolean = false,
        val deletingJourney: Journey? = null,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    // ==================== 数据加载 ====================

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val journeysFlow = appService.getAllJourneys()
                val counts = appService.getAllFootprintCounts()

                journeysFlow.collect { journeyList ->
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

    fun refresh() {
        loadData()
    }

    // ==================== 添加旅程 ====================

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    /**
     * 创建旅程（使用独立参数）
     * @param title 旅程标题
     * @param description 旅程描述
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param coverStyle 封面风格
     * @param coverImagePath 封面图片路径
     * @param journeyImagePaths 旅程图片路径列表
     */
    fun createJourney(
        title: String,
        description: String,
        startDate: Date,
        endDate: Date,
        coverStyle: String = "watercolor",
        coverImagePath: String = "",
        journeyImagePaths: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.createJourney(
                    title = title,
                    style = coverStyle,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    coverImagePath = coverImagePath,
                    journeyImagePaths = journeyImagePaths
                )
                hideAddDialog()
                loadData()
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
     * 创建旅程（使用 Journey 对象）
     * @param journey Journey 实体对象
     */
    fun createJourney(journey: Journey) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.createJourney(
                    title = journey.title,
                    style = journey.coverStyle,
                    description = journey.description,
                    startDate = journey.startDate,
                    endDate = journey.endDate,
                    coverImagePath = journey.coverImagePath,
                    journeyImagePaths = journey.journeyImagePaths
                )
                hideAddDialog()
                loadData()
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

    // ==================== 编辑旅程 ====================

    fun showEditDialog(journey: Journey) {
        _uiState.update { it.copy(
            showEditDialog = true,
            editingJourney = journey
        ) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(
            showEditDialog = false,
            editingJourney = null
        ) }
    }

    /**
     * 更新旅程（使用独立参数）
     * @param journeyId 旅程ID
     * @param title 新标题
     * @param description 新描述
     * @param startDate 新开始日期
     * @param endDate 新结束日期
     * @param coverStyle 新封面风格
     * @param coverImagePath 新封面图片路径
     * @param journeyImagePaths 新旅程图片列表
     */
    fun updateJourney(
        journeyId: Long,
        title: String,
        description: String,
        startDate: Date,
        endDate: Date,
        coverStyle: String,
        coverImagePath: String,
        journeyImagePaths: List<String>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val updatedJourney = Journey(
                    id = journeyId,
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    coverStyle = coverStyle,
                    coverImagePath = coverImagePath,
                    journeyImagePaths = journeyImagePaths
                )
                appService.updateJourney(updatedJourney)
                hideEditDialog()
                loadData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "更新失败"
                    )
                }
            }
        }
    }

    /**
     * 更新旅程（使用 Journey 对象）
     * @param journey Journey 实体对象（必须包含有效的 id）
     */
    fun updateJourney(journey: Journey) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                require(journey.id > 0) { "旅程ID不能为空" }
                appService.updateJourney(journey)
                hideEditDialog()
                loadData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "更新失败"
                    )
                }
            }
        }
    }

    /**
     * 更新旅程部分字段（只更新非空字段）
     * @param journeyId 旅程ID
     * @param title 新标题（可选）
     * @param description 新描述（可选）
     * @param startDate 新开始日期（可选）
     * @param endDate 新结束日期（可选）
     * @param coverStyle 新封面风格（可选）
     * @param coverImagePath 新封面图片路径（可选）
     * @param journeyImagePaths 新旅程图片列表（可选）
     */
    fun updateJourneyPartial(
        journeyId: Long,
        title: String? = null,
        description: String? = null,
        startDate: Date? = null,
        endDate: Date? = null,
        coverStyle: String? = null,
        coverImagePath: String? = null,
        journeyImagePaths: List<String>? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentJourney = _uiState.value.journeys.find { it.id == journeyId }
                    ?: throw IllegalArgumentException("旅程不存在: $journeyId")

                val updatedJourney = currentJourney.copy(
                    title = title ?: currentJourney.title,
                    description = description ?: currentJourney.description,
                    startDate = startDate ?: currentJourney.startDate,
                    endDate = endDate ?: currentJourney.endDate,
                    coverStyle = coverStyle ?: currentJourney.coverStyle,
                    coverImagePath = coverImagePath ?: currentJourney.coverImagePath,
                    journeyImagePaths = journeyImagePaths ?: currentJourney.journeyImagePaths
                )
                appService.updateJourney(updatedJourney)
                loadData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "更新失败"
                    )
                }
            }
        }
    }

    /**
     * 更新旅程封面图片
     */
    fun updateJourneyCover(journeyId: Long, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val localPath = appService.copyImageToLocal(imageUri)
                if (localPath.isNotEmpty()) {
                    appService.updateJourneyCover(journeyId, localPath)
                    loadData()
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "更新封面失败"
                    )
                }
            }
        }
    }

    /**
     * 更新旅程封面图片（使用本地路径）
     */
    fun updateJourneyCoverWithPath(journeyId: Long, imagePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.updateJourneyCover(journeyId, imagePath)
                loadData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "更新封面失败"
                    )
                }
            }
        }
    }

    // ==================== 删除旅程 ====================

    fun showDeleteConfirmDialog(journey: Journey) {
        _uiState.update { it.copy(
            showDeleteConfirmDialog = true,
            deletingJourney = journey
        ) }
    }

    fun hideDeleteConfirmDialog() {
        _uiState.update { it.copy(
            showDeleteConfirmDialog = false,
            deletingJourney = null
        ) }
    }

    fun deleteJourney(journeyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.deleteJourney(journeyId)
                hideDeleteConfirmDialog()
                loadData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "删除失败"
                    )
                }
            }
        }
    }

    fun deleteJourney(journey: Journey) {
        deleteJourney(journey.id)
    }

    fun deleteCurrentJourney() {
        _uiState.value.deletingJourney?.let { journey ->
            deleteJourney(journey.id)
        }
    }

    // ==================== 搜索旅程 ====================

    fun searchJourneys(keyword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (keyword.isBlank()) {
                    loadData()
                } else {
                    val results = appService.searchJourneys(keyword)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            journeys = results,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "搜索失败"
                    )
                }
            }
        }
    }

    fun clearSearch() {
        loadData()
    }

    // ==================== 获取旅程详情 ====================

    fun getJourneyById(journeyId: Long): Journey? {
        return _uiState.value.journeys.find { it.id == journeyId }
    }

    fun getFootprintCount(journeyId: Long): Int {
        return _uiState.value.footprintCounts[journeyId] ?: 0
    }

    // ==================== 错误处理 ====================

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun closeAllDialogs() {
        _uiState.update { it.copy(
            showAddDialog = false,
            showEditDialog = false,
            showDeleteConfirmDialog = false,
            editingJourney = null,
            deletingJourney = null
        ) }
    }
}