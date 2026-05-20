// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/JourneyViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.dao.FootprintDao
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.repository.FootprintRepository
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Address
import java.util.Date
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val appService: AppService,
    private val FootprintRepository: FootprintRepository,
    private val FootprintDao: FootprintDao
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
     * title: String,
     *     coverStyle: String = "BlackAndWhite",
     *     description: String = "这是一个旅程",
     *     startDate: Date = Date(),
     *     endDate: Date = Date(),
     *     coverImagePath: String = "这里需要图片地址",
     *     journeyImagePaths: List<String> = emptyList(),
     *     address: String = "这里需要旅程地址",
     *     longitude: Double = 0.0,
     *     latitude: Double = 0.0
     */
    fun createJourney(
        title: String,
        description: String,
        startDate: Date,
        endDate: Date,
        coverStyle: String,
        coverImagePath: String = "",
        journeyImagePaths: List<String> = emptyList(),
        address:String,
        longitude: Double,
        latitude: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.createJourney(
                    title = title,
                    coverStyle = coverStyle,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    coverImagePath = coverImagePath,
                    journeyImagePaths = journeyImagePaths,
                    address=address,
                    longitude=longitude,
                    latitude=latitude
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
     * title: String,
     *     coverStyle: String = "BlackAndWhite",
     *     description: String = "这是一个旅程",
     *     startDate: Date = Date(),
     *     endDate: Date = Date(),
     *     coverImagePath: String = "这里需要图片地址",
     *     journeyImagePaths: List<String> = emptyList(),
     *     address: String = "这里需要旅程地址",
     *     longitude: Double = 0.0,
     *     latitude: Double = 0.0
     */
    fun createJourney(journey: Journey) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.createJourney(
                    title = journey.title,
                    coverStyle = journey.coverStyle,
                    description = journey.description,
                    startDate = journey.startDate,
                    endDate = journey.endDate,
                    coverImagePath = journey.coverImagePath,
                    journeyImagePaths = journey.journeyImagePaths,
                    address=journey.address,
                    longitude=journey.longitude,
                    latitude=journey.latitude
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
     * 给旅程添加足迹
     *  lat: Double,
     *  lng: Double,
     *  photos: List<String>?,
     *  notes: String,//旅程描述
     *  title:String, //旅程标题
     *  rating:Int //评分
     */
    fun addFootprintsForJourney(
        journey: Journey,
        lat: Double,
        lng: Double,
        photos: List<String>?,
        notes: String,
        title: String,
        rating: Int
    ) {
        viewModelScope.launch {
            // 验证旅程ID
            if (journey.id <= 0) {
                _uiState.update { it.copy(error = "无效的旅程") }
                return@launch
            }

            try {
                // 直接添加，不改变全局 loading 状态
                val footprintId = FootprintRepository.addFootprint(
                    journeyId = journey.id,
                    lat = lat,
                    lng = lng,
                    photos = photos,
                    notes = notes,
                    title = title,
                    rating = rating
                )

                // 只刷新足迹数量，不重新加载整个列表
                val newCounts = appService.getAllFootprintCounts()
                _uiState.update { state ->
                    state.copy(
                        footprintCounts = newCounts,
                        error = null
                    )
                }

                // 可选：发送成功事件
                // _addFootprintSuccessEvent.emit(footprintId)

            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "添加足迹失败"
                    )
                }
            }
        }
    }

    /**
     * 添加足迹 - 使用 Journey 和 Footprint 对象
     * Footprint只能包含数据库存在的字段！！！
     */
    fun addFootprintsForJourney(
        journey: Journey,
        footprint: Footprint
    ) {
        viewModelScope.launch {
            // 验证旅程ID
            if (journey.id <= 0) {
                _uiState.update { it.copy(error = "无效的旅程") }
                return@launch
            }

            // 验证足迹对象
            if (footprint.title.isBlank()) {
                _uiState.update { it.copy(error = "足迹标题不能为空") }
                return@launch
            }


            try {
                // 通过 Repository 添加足迹（需要先扩展 Repository 方法）
                val footprintId = FootprintRepository.addFootprint(
                   footprint
                )

                // 刷新足迹数量
                val newCounts = appService.getAllFootprintCounts()
                _uiState.update { state ->
                    state.copy(
                        footprintCounts = newCounts,
                        error = null
                    )
                }


            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "添加足迹失败")
                }
            }
        }
        // 不能写 return footprintId
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
     * @param address  旅程地址
     * @param longitude 旅程经度
     * @param latitude  旅程维度
     */
    fun updateJourney(
        journeyId: Long,
        title: String,
        description: String,
        startDate: Date,
        endDate: Date,
        coverStyle: String,
        coverImagePath: String,
        journeyImagePaths: List<String>,
        address: String,
        longitude: Double,
        latitude: Double
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
                    journeyImagePaths = journeyImagePaths,
                    address = address,
                    longitude=longitude,
                    latitude=latitude
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


    //通过id删除旅程
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

    //

//    fun deleteJourney(journey: Journey) {
//        deleteJourney(journey.id)
//    }


    //通过对象删除旅程
    fun deleteJourney(journey: Journey) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.deleteJourney(journey)
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

    //删除当前旅程
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