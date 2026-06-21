package com.example.travel_footprint_android.domain.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 当前正在运行的 AI 操作信息
 *
 * @param id 唯一标识，用于取消和完成操作
 * @param type 操作类型
 * @param description 操作描述（显示在浮窗中）
 */
data class AiOperationInfo(
    val id: String = UUID.randomUUID().toString(),
    val type: AiOperationType,
    val description: String,
) {
    enum class AiOperationType {
        /** AI 智能填写（旅程标题/描述/地址） */
        GENERATE_INFO,
        /** AI 智能填写（足迹信息） */
        GENERATE_FOOTPRINT,
        /** AI 封面涂鸦 */
        PAINT_COVER,
    }
}

/**
 * AiOperationStateHolder - AI 操作状态共享单例
 *
 * 桥接 AiGenerateViewModel（旅程编辑页）和 JourneyScreen（主界面）之间的 AI 操作状态。
 * 支持多个 AI 操作同时运行，每个操作有独立的取消回调。
 * 当用户关闭 AI 助手弹窗但操作仍在后台运行时，通过此单例让 JourneyScreen
 * 显示 AI 运行浮窗列表，并提供逐个取消的能力。
 */
@Singleton
class AiOperationStateHolder @Inject constructor() {

    private val _operationList = MutableStateFlow<List<AiOperationInfo>>(emptyList())
    val operationList: StateFlow<List<AiOperationInfo>> = _operationList.asStateFlow()

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    private val _completionEvent = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val completionEvent: SharedFlow<String> = _completionEvent.asSharedFlow()

    /** 按操作 ID 存储的取消回调，由 AiGenerateViewModel 注册 */
    private val cancelActions = mutableMapOf<String, () -> Unit>()

    /**
     * 设置 AI 助手弹窗的打开/关闭状态
     *
     * 弹窗打开时，浮窗隐藏；弹窗关闭时，若操作仍在运行则显示浮窗。
     */
    fun setDialogOpen(open: Boolean) {
        _isDialogOpen.value = open
    }

    /**
     * 开始一个 AI 操作
     *
     * @param info 操作信息（需包含唯一 id）
     * @param onCancel 取消操作的回调（由 AiGenerateViewModel 提供）
     */
    fun startOperation(info: AiOperationInfo, onCancel: () -> Unit) {
        _operationList.value = _operationList.value + info
        cancelActions[info.id] = onCancel
    }

    /**
     * 完成指定 AI 操作
     *
     * @param id 操作 ID
     * @param message 完成提示消息
     */
    fun finishOperation(id: String, message: String) {
        cancelActions.remove(id)
        _operationList.value = _operationList.value.filter { it.id != id }
        _completionEvent.tryEmit(message)
    }

    /**
     * 取消指定 AI 操作（由浮窗的取消按钮触发）
     *
     * @param id 操作 ID
     */
    fun cancelOperation(id: String) {
        cancelActions[id]?.invoke()
        cancelActions.remove(id)
        _operationList.value = _operationList.value.filter { it.id != id }
    }
}
