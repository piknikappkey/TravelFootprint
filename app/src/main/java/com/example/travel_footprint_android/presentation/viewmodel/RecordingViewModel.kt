package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.domain.service.RecordingStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * RecordingUiState - 录制状态数据类
 *
 * @param isRecording 是否正在录制（START 或 PAUSE 状态）
 * @param isPaused 是否处于暂停状态
 * @param recordingFootprintId 正在录制的足迹 ID（null 表示无录制）
 * @param recordingFootprintTitle 正在录制的足迹标题
 * @param recordingJourneyId 正在录制的足迹所属旅程 ID
 * @param durationTime 持续时间（毫秒）
 * @param displacementDistance 累计位移距离（米）
 * @param speed 移动速度（米/秒）
 * @param calories 消耗卡路里
 */
data class RecordingUiState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val recordingFootprintId: Long? = null,
    val recordingFootprintTitle: String = "",
    val recordingJourneyId: Long = 0L,
    val durationTime: Long = 0L,
    val displacementDistance: Double = 0.0,
    val speed: Double = 0.0,
    val calories: Double = 0.0,
)

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val stateHolder: RecordingStateHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    init {
        // 监听 RecordingStateHolder 的状态变化，同步到 uiState
        viewModelScope.launch {
            stateHolder.isRecording.collect { recording ->
                _uiState.value = _uiState.value.copy(isRecording = recording)
            }
        }
        viewModelScope.launch {
            stateHolder.isPaused.collect { paused ->
                _uiState.value = _uiState.value.copy(isPaused = paused)
            }
        }
        viewModelScope.launch {
            stateHolder.recordingFootprintId.collect { id ->
                _uiState.value = _uiState.value.copy(recordingFootprintId = id)
            }
        }
        viewModelScope.launch {
            stateHolder.recordingFootprintTitle.collect { title ->
                _uiState.value = _uiState.value.copy(recordingFootprintTitle = title)
            }
        }
        viewModelScope.launch {
            stateHolder.recordingJourneyId.collect { id ->
                _uiState.value = _uiState.value.copy(recordingJourneyId = id)
            }
        }
        viewModelScope.launch {
            stateHolder.durationTime.collect { t ->
                _uiState.value = _uiState.value.copy(durationTime = t)
            }
        }
        viewModelScope.launch {
            stateHolder.displacementDistance.collect { d ->
                _uiState.value = _uiState.value.copy(displacementDistance = d)
            }
        }
        viewModelScope.launch {
            stateHolder.speed.collect { s ->
                _uiState.value = _uiState.value.copy(speed = s)
            }
        }
        viewModelScope.launch {
            stateHolder.calories.collect { c ->
                _uiState.value = _uiState.value.copy(calories = c)
            }
        }
    }

    /** 启动录制：设置录制状态和足迹信息 */
    fun startRecording(footprintId: Long, footprintTitle: String, journeyId: Long) {
        stateHolder.setRecording(true)
        stateHolder.setPaused(false)
        stateHolder.setRecordingFootprintId(footprintId)
        stateHolder.setRecordingFootprintTitle(footprintTitle)
        stateHolder.setRecordingJourneyId(journeyId)
    }

    /** 暂停录制 */
    fun pauseRecording() {
        stateHolder.setPaused(true)
    }

    /** 恢复录制 */
    fun resumeRecording() {
        stateHolder.setPaused(false)
    }

    /** 停止录制：重置所有状态 */
    fun stopRecording() {
        stateHolder.clearAll()
    }
}
