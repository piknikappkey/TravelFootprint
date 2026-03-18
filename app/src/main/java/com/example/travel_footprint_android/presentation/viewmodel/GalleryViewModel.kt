// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/GalleryViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.data.repository.MediaRepository
import com.example.travel_footprint_android.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val journeyRepository: JourneyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryState())
    val uiState: StateFlow<GalleryState> = _uiState.asStateFlow()

    init {
        loadAllPhotos()
    }

    /**
     * 加载所有照片
     */
    fun loadAllPhotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                mediaRepository.getAllPhotos().collect { mediaList ->
                    val mediaItems = mutableListOf<MediaItem>()

                    for (media in mediaList) {
                        // 修复：使用 firstOrNull() 而不是 .value
                        val journey = journeyRepository.getJourneyById(media.footprintId).firstOrNull()

                        mediaItems.add(
                            MediaItem(
                                id = media.id,
                                path = media.localPath,
                                thumbnailPath = media.thumbnailPath,
                                journeyId = media.footprintId,
                                journeyTitle = journey?.title ?: "未知旅程",
                                footprintId = media.footprintId,
                                createTime = DateUtils.getRelativeTimeDescription(media.createTime),
                                caption = media.caption
                            )
                        )
                    }

                    // 按旅程分组
                    val grouped = mediaItems.groupBy { it.journeyId }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            photos = mediaItems,
                            groupedByJourney = grouped,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "加载照片失败"
                    )
                }
            }
        }
    }

    /**
     * 根据旅程获取照片
     */
    fun getPhotosByJourney(journeyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 修复：使用 firstOrNull() 而不是 .value
                val journeyWithFootprints = journeyRepository.getJourneyWithFootprints(journeyId).firstOrNull()
                val footprints = journeyWithFootprints?.footprints ?: emptyList()

                val footprintIds = footprints.map { it.id }

                // 获取这些足迹的照片
                val mediaList = mediaRepository.getMediaByFootprints(footprintIds)

                val mediaItems = mediaList.map { media ->
                    MediaItem(
                        id = media.id,
                        path = media.localPath,
                        thumbnailPath = media.thumbnailPath,
                        journeyId = journeyId,
                        journeyTitle = footprints.firstOrNull()?.title ?: "旅程",
                        footprintId = media.footprintId,
                        createTime = DateUtils.getRelativeTimeDescription(media.createTime),
                        caption = media.caption
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        photos = mediaItems,
                        error = null
                    )
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
     * 删除照片
     */
    fun deletePhoto(mediaId: Long) {
        viewModelScope.launch {
            try {
                mediaRepository.deleteMediaFile(mediaId)
                // 重新加载
                loadAllPhotos()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }

    /**
     * 从照片创建足迹
     */
    fun createFootprintFromPhoto(photoPath: String, lat: Double, lng: Double) {
        // 这个功能需要在MapViewModel中实现
        // 这里只是预留接口
    }

    /**
     * 选择照片
     */
    fun selectPhoto(mediaItem: MediaItem) {
        _uiState.update { it.copy(selectedPhoto = mediaItem) }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}