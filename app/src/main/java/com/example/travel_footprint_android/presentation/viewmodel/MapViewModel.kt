// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/MapViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.domain.service.HandDrawStyle
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {

    data class MapUiState(
        val isLoading: Boolean = false,
        val footprints: List<Footprint> = emptyList(),
        val selectedFootprint: Footprint? = null,
        val showAddDialog: Boolean = false,
        val selectedLat: Double = 0.0,
        val selectedLng: Double = 0.0,
        val selectedImagePath: String? = null,
        val isImageLoading: Boolean = false,
        val mapStyle: HandDrawStyle = HandDrawStyle.WATERCOLOR,
        val isTrailVisible: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var currentJourneyId: Long? = null

    fun loadJourneyFootprints(journeyId: Long) {
        currentJourneyId = journeyId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appService.getFootprintsForMap(journeyId).collect { footprints ->
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

    fun showAddFootprintDialog(lat: Double, lng: Double) {
        _uiState.update { state ->
            state.copy(
                showAddDialog = true,
                selectedLat = lat,
                selectedLng = lng
            )
        }
    }

    fun hideAddFootprintDialog() {
        _uiState.update { state ->
            state.copy(
                showAddDialog = false,
                selectedImagePath = null
            )
        }
    }

    fun onImageSelected(imagePath: String?) {
        _uiState.update { it.copy(selectedImagePath = imagePath) }
    }

    fun addFootprint(notes: String, imagePath: String? = null, title: String) {
        val journeyId = currentJourneyId ?: return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val photos = if (!imagePath.isNullOrEmpty()) listOf(imagePath) else null
                appService.addFootprint(
                    journeyId = journeyId,
                    lat = state.selectedLat,
                    lng = state.selectedLng,
                    notes = notes,
                    photos = photos,
                    title = title
                )
                _uiState.update { it.copy(isLoading = false) }
                hideAddFootprintDialog()
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

    fun selectFootprint(footprint: Footprint) {
        _uiState.update { it.copy(selectedFootprint = footprint) }
    }

    fun changeMapStyle(style: HandDrawStyle) {
        _uiState.update { it.copy(mapStyle = style) }
    }

    fun toggleTrailVisibility() {
        _uiState.update { state ->
            state.copy(isTrailVisible = !state.isTrailVisible)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}