// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/MapViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.repository.FootprintRepository
import com.example.travel_footprint_android.data.repository.MediaRepository
import com.example.travel_footprint_android.domain.service.HandDrawEngine
import com.example.travel_footprint_android.domain.service.HandDrawStyle
import com.example.travel_footprint_android.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val footprintRepository: FootprintRepository,
    private val mediaRepository: MediaRepository,
    private val handDrawEngine: HandDrawEngine,
    private val locationService: LocationService,
    @ApplicationContext private val context: Context  // 添加 Context 注入
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    // 控制添加足迹对话框
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    // 当前选中的经纬度
    private var _selectedLat = 0.0
    private var _selectedLng = 0.0

    // 图片相关状态
    private val _selectedImagePath = MutableStateFlow<String?>(null)
    val selectedImagePath: StateFlow<String?> = _selectedImagePath.asStateFlow()

    private val _isImageLoading = MutableStateFlow(false)
    val isImageLoading: StateFlow<Boolean> = _isImageLoading.asStateFlow()

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
     * 显示添加足迹对话框
     */
    fun showAddFootprintDialog(lat: Double, lng: Double) {
        _selectedLat = lat
        _selectedLng = lng
        _showAddDialog.value = true
    }

    /**
     * 隐藏添加足迹对话框
     */
    fun hideAddFootprintDialog() {
        _showAddDialog.value = false
        _selectedImagePath.value = null
    }

    /**
     * 处理选择的图片（从相册或相机返回的 Uri）
     */
    fun onImageSelected(uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _isImageLoading.value = true
            try {
                val savedPath = saveImageToLocal(uri)
                withContext(Dispatchers.Main) {
                    _selectedImagePath.value = savedPath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isImageLoading.value = false
            }
        }
    }

    /**
     * 保存图片到本地（修复版）
     */
    private suspend fun saveImageToLocal(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver: ContentResolver = context.contentResolver

                val fileName = "footprint_photo_${System.currentTimeMillis()}.jpg"
                val photosDir = File(context.filesDir, "footprint_photos")

                // 创建目录
                if (!photosDir.exists()) {
                    photosDir.mkdirs()
                }

                val destFile = File(photosDir, fileName)

                // 复制文件
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    /**
     * 长按地图点击
     */
    fun onMapLongClick(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val address = locationService.reverseGeocode(lat, lng)
                showAddFootprintDialog(lat, lng)
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
     * 添加足迹（带图片）
     */
    fun addFootprintWithImage(
        title: String,
        notes: String,
        imagePath: String? = null
    ) {
        val journeyId = currentJourneyId ?: return
        val lat = _selectedLat
        val lng = _selectedLng

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val photos = if (!imagePath.isNullOrEmpty()) listOf(imagePath) else null

                footprintRepository.addFootprint(
                    journeyId = journeyId,
                    lat = lat,
                    lng = lng,
                    photos = photos,
                    notes = if (title.isNotEmpty()) "$title: $notes" else notes
                )

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                    hideAddFootprintDialog()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = e.message ?: "添加足迹失败"
                        )
                    }
                }
            }
        }
    }

    /**
     * 添加带照片的足迹（兼容旧方法）
     */
    fun addFootprintWithPhoto(
        title: String,
        photoPath: String,
        lat: Double,
        lng: Double,
        notes: String = ""
    ) {
        val journeyId = currentJourneyId ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            try {
                footprintRepository.addFootprint(
                    journeyId = journeyId,
                    lat = lat,
                    lng = lng,
                    photos = listOf(photoPath),
                    notes = if (title.isNotEmpty()) "$title: $notes" else notes
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