package com.example.travel_footprint_android.presentation.components.image_random.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.local.ImageRainSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val clickEnabled: Boolean = true,
    val pressEnabled: Boolean = true,
)

@HiltViewModel
class ImageRainViewModel @Inject constructor(
    private val settingsStore: ImageRainSettingsStore
) : ViewModel() {

    private val _settings = MutableStateFlow(ImageRainSettings())
    val settings: StateFlow<ImageRainSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.settingsFlow.collect { persisted ->
                _settings.value = persisted.copy(clearAllTrigger = _settings.value.clearAllTrigger)
            }
        }
    }

    fun updateRainEnabled(value: Boolean) {
        _settings.update { it.copy(rainEnabled = value) }
        saveSettings()
    }

    fun updateIsChaos(value: Boolean) {
        _settings.update { it.copy(isChaos = value) }
        saveSettings()
    }

    fun updateMaxImages(value: Int) {
        _settings.update { it.copy(maxImages = value) }
        saveSettings()
    }

    fun updateIntervalMs(value: Long) {
        _settings.update { it.copy(intervalMs = value) }
        saveSettings()
    }

    fun updateMinExistenceTime(value: Int) {
        _settings.update { it.copy(minExistenceTime = value) }
        saveSettings()
    }

    fun updateMaxExistenceTime(value: Int) {
        _settings.update { it.copy(maxExistenceTime = value) }
        saveSettings()
    }

    fun updateMinSize(value: Int) {
        _settings.update { it.copy(minSize = value) }
        saveSettings()
    }

    fun updateMaxSize(value: Int) {
        _settings.update { it.copy(maxSize = value) }
        saveSettings()
    }

    fun updateMinAngle(value: Int) {
        _settings.update { it.copy(minAngle = value) }
        saveSettings()
    }

    fun updateMaxAngle(value: Int) {
        _settings.update { it.copy(maxAngle = value) }
        saveSettings()
    }

    fun updatePressScale(value: Float) {
        _settings.update { it.copy(pressScale = value) }
        saveSettings()
    }

    fun updateRotationSpeed(value: Float) {
        _settings.update { it.copy(rotationSpeed = value) }
        saveSettings()
    }

    fun updateClickEnabled(value: Boolean) {
        _settings.update { it.copy(clickEnabled = value) }
        saveSettings()
    }

    fun updatePressEnabled(value: Boolean) {
        _settings.update { it.copy(pressEnabled = value) }
        saveSettings()
    }

    fun clearAll() {
        _settings.update { it.copy(clearAllTrigger = it.clearAllTrigger + 1) }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            settingsStore.saveSettings(_settings.value)
        }
    }
}
