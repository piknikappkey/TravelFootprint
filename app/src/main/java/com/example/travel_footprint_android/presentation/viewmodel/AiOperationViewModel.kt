package com.example.travel_footprint_android.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.travel_footprint_android.domain.service.AiOperationInfo
import com.example.travel_footprint_android.domain.service.AiOperationStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * AiOperationViewModel - AI 操作状态 ViewModel
 *
 * 提供给 JourneyScreen 使用，观察 AiOperationStateHolder 中的 AI 操作列表，
 * 支持多个 AI 操作同时运行，每个操作可独立取消。
 * 操作完成时通过 completionEvent 发出 Toast 提示。
 */
@HiltViewModel
class AiOperationViewModel @Inject constructor(
    private val stateHolder: AiOperationStateHolder,
) : ViewModel() {

    companion object {
        private const val TAG = "AiOperationViewModel"
    }

    /** 当前正在运行的所有 AI 操作 */
    val operationList: StateFlow<List<AiOperationInfo>> = stateHolder.operationList

    /** AI 助手弹窗是否打开 */
    val isDialogOpen: StateFlow<Boolean> = stateHolder.isDialogOpen

    /** AI 操作完成事件（携带提示消息） */
    val completionEvent: SharedFlow<String> = stateHolder.completionEvent

    /**
     * 取消指定 ID 的 AI 操作
     *
     * @param id 操作 ID
     */
    fun cancelOperation(id: String) {
        Log.d(TAG, "从 JourneyScreen 取消 AI 操作: $id")
        stateHolder.cancelOperation(id)
    }
}
