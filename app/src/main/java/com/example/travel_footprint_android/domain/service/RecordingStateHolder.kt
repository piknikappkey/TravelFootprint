package com.example.travel_footprint_android.domain.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RecordingStateHolder - 录制状态共享单例
 *
 * 桥接 RecordingForegroundService 和 RecordingViewModel 之间的状态。
 * Service 无法直接注入 ViewModel，因此通过此单例作为中间层传递状态。
 *
 * 同时负责将录制状态持久化到 SharedPreferences，用于应用重启后恢复。
 */
@Singleton
class RecordingStateHolder @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    companion object {
        private const val PREFS_NAME = "recording_prefs"
        private const val KEY_RECORDING_FOOTPRINT_ID = "recording_footprint_id"
        private const val KEY_RECORDING_FOOTPRINT_TITLE = "recording_footprint_title"
        private const val KEY_RECORDING_JOURNEY_ID = "recording_journey_id"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordingFootprintId = MutableStateFlow<Long?>(null)
    val recordingFootprintId: StateFlow<Long?> = _recordingFootprintId.asStateFlow()

    private val _recordingFootprintTitle = MutableStateFlow("")
    val recordingFootprintTitle: StateFlow<String> = _recordingFootprintTitle.asStateFlow()

    private val _recordingJourneyId = MutableStateFlow(0L)
    val recordingJourneyId: StateFlow<Long> = _recordingJourneyId.asStateFlow()

    private val _durationTime = MutableStateFlow(0L)
    val durationTime: StateFlow<Long> = _durationTime.asStateFlow()

    private val _displacementDistance = MutableStateFlow(0.0)
    val displacementDistance: StateFlow<Double> = _displacementDistance.asStateFlow()

    private val _speed = MutableStateFlow(0.0)
    val speed: StateFlow<Double> = _speed.asStateFlow()

    private val _calories = MutableStateFlow(0.0)
    val calories: StateFlow<Double> = _calories.asStateFlow()

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }

    fun setRecordingFootprintId(id: Long?) {
        _recordingFootprintId.value = id
        // 持久化到 SharedPreferences
        prefs.edit().putLong(KEY_RECORDING_FOOTPRINT_ID, id ?: -1L).apply()
    }

    fun setRecordingFootprintTitle(title: String) {
        _recordingFootprintTitle.value = title
        prefs.edit().putString(KEY_RECORDING_FOOTPRINT_TITLE, title).apply()
    }

    fun setRecordingJourneyId(id: Long) {
        _recordingJourneyId.value = id
        prefs.edit().putLong(KEY_RECORDING_JOURNEY_ID, id).apply()
    }

    fun updateRecordingData(
        durationTime: Long,
        displacementDistance: Double,
        speed: Double,
        calories: Double,
    ) {
        _durationTime.value = durationTime
        _displacementDistance.value = displacementDistance
        _speed.value = speed
        _calories.value = calories
    }

    fun clearAll() {
        _isRecording.value = false
        _isPaused.value = false
        _recordingFootprintId.value = null
        _recordingFootprintTitle.value = ""
        _recordingJourneyId.value = 0L
        _durationTime.value = 0L
        _displacementDistance.value = 0.0
        _speed.value = 0.0
        _calories.value = 0.0
        // 清除持久化
        prefs.edit().clear().apply()
    }

}
