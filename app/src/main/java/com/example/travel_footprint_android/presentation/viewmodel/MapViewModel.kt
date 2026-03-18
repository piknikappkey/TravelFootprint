// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/MapViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.repository.FootprintRepository
import com.example.travel_footprint_android.data.repository.MediaRepository
import com.example.travel_footprint_android.domain.service.HandDrawEngine
import com.example.travel_footprint_android.domain.service.HandDrawStyle
import com.example.travel_footprint_android.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val footprintRepository: FootprintRepository,
    private val mediaRepository: MediaRepository,
    private val handDrawEngine: HandDrawEngine,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    private var currentJourneyId: Long? = null

    /**
     * 加载旅程足迹
     */
    fun loadJourneyFootprints(journeyId: Long) {
        currentJourneyId = journeyId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                footprintRepository.getFootprintsForMap(journeyId).collect { footprints ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            footprints = footprints,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "加载足迹失败"
                    )
                }
            }
        }
    }

    /**
     * 长按地图点击
     */
    fun onMapLongClick(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // 逆地理编码获取地址
                val address = locationService.reverseGeocode(lat, lng)

                // 这里应该显示添加足迹的对话框
                // 暂时只更新状态
                _uiState.update { state ->
                    state.copy(
                        cameraPosition = state.cameraPosition.copy(
                            latitude = lat,
                            longitude = lng
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "获取位置失败")
                }
            }
        }
    }

    /**
     * 添加带照片的足迹
     */
    fun addFootprintWithPhoto(
        title: String,
        photoPath: String,
        lat: Double,
        lng: Double,
        notes: String = ""
    ) {
        val journeyId = currentJourneyId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 保存照片到本地
                val savedPath = mediaRepository.savePhotoToLocal(photoPath)

                // 生成缩略图
                val thumbnailPath = mediaRepository.generateThumbnail(savedPath)

                // 添加足迹
                footprintRepository.addFootprint(
                    journeyId = journeyId,
                    lat = lat,
                    lng = lng,
                    photos = listOf(savedPath),
                    notes = notes
                )

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "添加足迹失败"
                    )
                }
            }
        }
    }

    /**
     * 选择足迹
     */
    fun selectFootprint(footprint: Footprint) {
        _uiState.update { it.copy(selectedFootprint = footprint) }
    }

    /**
     * 更新相机位置
     */
    fun updateCameraPosition(lat: Double, lng: Double, zoom: Float) {
        _uiState.update { state ->
            state.copy(
                cameraPosition = state.cameraPosition.copy(
                    latitude = lat,
                    longitude = lng,
                    zoom = zoom
                )
            )
        }
    }

    /**
     * 切换地图风格
     */
    fun changeMapStyle(style: HandDrawStyle) {
        _uiState.update { it.copy(mapStyle = style) }
    }

    /**
     * 切换轨迹显示
     */
    fun toggleTrailVisibility() {
        _uiState.update { state ->
            state.copy(isTrailVisible = !state.isTrailVisible)
        }
    }

    /**
     * 生成轨迹路径
     */
    fun generateTrailPath(): List<Pair<Double, Double>> {
        return uiState.value.footprints.map { footprint ->
            // 这里需要从数据库获取footprint的坐标
            // 暂时返回空列表
            0.0 to 0.0
        }
    }

    /**
     * 清空旅程所有足迹
     */
    fun clearAllFootprints() {
        val journeyId = currentJourneyId ?: return

        viewModelScope.launch {
            try {
                footprintRepository.clearAllFootprints(journeyId)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "清空失败")
                }
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}