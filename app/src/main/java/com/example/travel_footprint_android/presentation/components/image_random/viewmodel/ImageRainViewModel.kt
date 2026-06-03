package com.example.travel_footprint_android.presentation.components.image_random.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ImageRainSettings(
    val rainEnabled: Boolean = true,
    val isChaos: Boolean = false,
    val maxImages: Int = 10,
    val intervalMs: Long = 1000L,
    val minExistenceTime: Int = 10000,
    val maxExistenceTime: Int = 20000,
    val minSize: Int = 30,
    val maxSize: Int = 50,
    val minAngle: Int = 0,
    val maxAngle: Int = 360,
    val pressScale: Float = 20f,
    val rotationSpeed: Float = 30f,
    val clearAllTrigger: Int = 0,
)

@HiltViewModel
class ImageRainViewModel @Inject constructor() : ViewModel() {

    private val _settings = MutableStateFlow(ImageRainSettings())
    val settings: StateFlow<ImageRainSettings> = _settings.asStateFlow()

    fun updateRainEnabled(value: Boolean) {
        _settings.update { it.copy(rainEnabled = value) }
    }

    fun updateIsChaos(value: Boolean) {
        _settings.update { it.copy(isChaos = value) }
    }

    fun updateMaxImages(value: Int) {
        _settings.update { it.copy(maxImages = value) }
    }

    fun updateIntervalMs(value: Long) {
        _settings.update { it.copy(intervalMs = value) }
    }

    fun updateMinExistenceTime(value: Int) {
        _settings.update { it.copy(minExistenceTime = value) }
    }

    fun updateMaxExistenceTime(value: Int) {
        _settings.update { it.copy(maxExistenceTime = value) }
    }

    fun updateMinSize(value: Int) {
        _settings.update { it.copy(minSize = value) }
    }

    fun updateMaxSize(value: Int) {
        _settings.update { it.copy(maxSize = value) }
    }

    fun updateMinAngle(value: Int) {
        _settings.update { it.copy(minAngle = value) }
    }

    fun updateMaxAngle(value: Int) {
        _settings.update { it.copy(maxAngle = value) }
    }

    fun updatePressScale(value: Float) {
        _settings.update { it.copy(pressScale = value) }
    }

    fun updateRotationSpeed(value: Float) {
        _settings.update { it.copy(rotationSpeed = value) }
    }

    fun clearAll() {
        _settings.update { it.copy(clearAllTrigger = it.clearAllTrigger + 1) }
    }
}
