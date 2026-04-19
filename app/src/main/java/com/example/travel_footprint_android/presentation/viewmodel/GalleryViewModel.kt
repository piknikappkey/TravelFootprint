// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/GalleryViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {

    data class GalleryUiState(
        val isLoading: Boolean = false,
        val photos: List<MediaItem> = emptyList(),
        val selectedPhoto: MediaItem? = null,
        val groupedByJourney: Map<Long, List<MediaItem>> = emptyMap(),
        val error: String? = null
    )

    data class MediaItem(
        val id: Long,
        val path: String,
        val thumbnailPath: String,
        val journeyId: Long,
        val journeyTitle: String,
        val footprintId: Long,
        val createTime: String,
        val caption: String
    )

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadAllPhotos()
    }

    fun loadAllPhotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.getAllPhotos().collect { mediaList ->
                    val mediaItems = mediaList.map { media ->
                        MediaItem(
                            id = media.id,
                            path = media.localPath,
                            thumbnailPath = media.thumbnailPath,
                            journeyId = media.footprintId,
                            journeyTitle = "",
                            footprintId = media.footprintId,
                            createTime = media.createTime.toString(),
                            caption = media.caption
                        )
                    }
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

    fun deletePhoto(mediaId: Long) {
        viewModelScope.launch {
            try {
                appService.deletePhoto(mediaId)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }

    fun selectPhoto(mediaItem: MediaItem) {
        _uiState.update { it.copy(selectedPhoto = mediaItem) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}