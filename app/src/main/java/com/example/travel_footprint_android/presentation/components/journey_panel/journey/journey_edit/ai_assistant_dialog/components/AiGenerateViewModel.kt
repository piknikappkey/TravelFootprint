package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.network.AiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * AI 自动生成旅程标题和描述的 UI 状态
 */
data class AiGenerateState(
    val isLoading: Boolean = false,              // 是否正在生成
    val isPaintLoading: Boolean = false,         // 是否正在涂鸦
    val error: String? = null,                   // 错误信息
    val paintError: String? = null,              // 涂鸦错误信息
    val locationName: String? = null,            // 获取到的地址名称
    val generatedTitle: String? = null,          // AI 生成的标题
    val generatedDescription: String? = null,    // AI 生成的描述
    val paintedImagePath: String? = null         // 涂鸦后的图片路径
)

/**
 * AI 自动生成 ViewModel
 *
 * 职责：
 * 1. 通过高德 SDK 获取用户当前位置（经纬度）
 * 2. 逆地理编码获取地址名称
 * 3. 调用 AiService 生成标题和描述
 * 4. 管理加载状态和错误处理
 */
@HiltViewModel
class AiGenerateViewModel @Inject constructor(
    private val application: Application,
    private val aiService: AiService
) : ViewModel() {

    // 私有可变状态流
    private val _state = MutableStateFlow(AiGenerateState())

    // 对外只读状态流
    val state: StateFlow<AiGenerateState> = _state.asStateFlow()

    companion object {
        private const val TAG = "AiGenerateViewModel"
    }

    /**
     * 通过高德 SDK 获取当前位置（suspend 版本）
     *
     * 使用 suspendCancellableCoroutine 将高德的回调式 API 转为协程挂起函数
     * 直接使用高德返回的详细地址信息（精确到门牌号级别）
     *
     * @return Triple(latitude, longitude, address) 或 null（定位失败时）
     */
    private suspend fun getAmapLocation(): Triple<Double, Double, String>? = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "创建高德定位客户端...")
            val locationClient = AMapLocationClient(application.applicationContext)

            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocation = true           // 单次定位
                httpTimeOut = 20000             // 网络请求超时 20 秒
                isNeedAddress = true            // 需要地址信息
            }
            locationClient.setLocationOption(option)

            locationClient.setLocationListener { location ->
                Log.d(TAG, "高德定位回调: errorCode=${location.errorCode}, lat=${location.latitude}, lng=${location.longitude}")
                if (location.errorCode == 0) {
                    val address = location.address ?: "未知地址"
                    Log.d(TAG, "高德定位成功: $address")
                    Log.d(TAG, "高德详细信息: province=${location.province}, city=${location.city}, district=${location.district}, street=${location.street}, streetNum=${location.streetNum}, poiName=${location.poiName}")
                    continuation.resume(Triple(location.latitude, location.longitude, address))
                } else {
                    Log.e(TAG, "高德定位失败: errorCode=${location.errorCode}, errorInfo=${location.errorInfo}")
                    continuation.resume(null)
                }
                // 定位完成后销毁客户端
                locationClient.onDestroy()
            }

            // 注册取消回调，协程取消时销毁客户端
            continuation.invokeOnCancellation {
                Log.d(TAG, "协程取消，销毁定位客户端")
                locationClient.onDestroy()
            }

            Log.d(TAG, "启动高德定位...")
            locationClient.startLocation()

        } catch (e: Exception) {
            Log.e(TAG, "创建高德定位客户端异常", e)
            continuation.resume(null)
        }
    }

    /**
     * 执行 AI 自动生成
     *
     * @param journey 当前编辑中的旅程数据
     * @param selectedFields 用户选择要生成的字段集合
     * @param onResult 回调函数，返回更新后的地址信息和 AI 生成结果
     */
    fun generate(
        journey: Journey,
        selectedFields: Set<AiFillField>,
        customPrompt: String? = null,
        onResult: (locationName: String, latitude: Double, longitude: Double, title: String, description: String) -> Unit
    ) {
        // 如果正在加载中，直接返回防止重复请求
        if (_state.value.isLoading) {
            Log.w(TAG, "正在加载中，忽略重复请求")
            return
        }

        // 如果没有选中任何字段，直接返回（由调用方处理提示）
        if (selectedFields.isEmpty()) {
            Log.w(TAG, "未选择任何字段，忽略请求")
            return
        }

        Log.d(TAG, "========== AI 生成流程开始 ==========")
        Log.d(TAG, "选中的字段: $selectedFields")
        Log.d(TAG, "当前旅程标题: '${journey.title}'")
        Log.d(TAG, "当前旅程描述: '${journey.description}'")

        viewModelScope.launch {
            // 1. 设置加载状态
            Log.d(TAG, "步骤1: 设置加载状态 isLoading=true")
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // 2. 如果选中了地址字段，通过高德 SDK 获取当前位置和详细地址
                var latitude = 0.0
                var longitude = 0.0
                var addressName = ""

                if (AiFillField.ADDRESS in selectedFields) {
                    Log.d(TAG, "步骤2: 开始通过高德 SDK 获取当前位置...")
                    val locationResult = getAmapLocation()

                    if (locationResult == null) {
                        Log.e(TAG, "步骤2失败: 无法获取位置")
                        _state.update {
                            it.copy(isLoading = false, error = "无法获取位置，请检查定位权限和网络")
                        }
                        return@launch
                    }

                    latitude = locationResult.first
                    longitude = locationResult.second
                    addressName = locationResult.third
                    Log.d(TAG, "步骤2成功: 获取到位置 lat=$latitude, lng=$longitude")
                    Log.d(TAG, "高德返回的详细地址: '$addressName'")

                    // 3. 更新状态（保存地址信息）
                    Log.d(TAG, "步骤3: 更新状态保存地址信息")
                    _state.update { it.copy(locationName = addressName) }
                } else {
                    Log.d(TAG, "步骤2: 跳过定位（未选中地址字段）")
                    // 使用已有的地址信息
                    latitude = journey.latitude
                    longitude = journey.longitude
                    // 解析已有的 address 字段获取地址名称
                    addressName = journey.address.split("\n").firstOrNull() ?: ""
                }

                // 4. 构造 prompt 并调用 AI 服务（如果有封面图片则一并上传）
                Log.d(TAG, "步骤4: 开始调用 AI 服务...")
                Log.d(TAG, "传入参数: lat=$latitude, lng=$longitude, address='$addressName'")
                Log.d(TAG, "封面图片路径: '${journey.coverImagePath}'")
                val startTime = System.currentTimeMillis()

                val result = aiService.generateJourneyInfo(
                    latitude = latitude,
                    longitude = longitude,
                    addressName = addressName,
                    existingTitle = journey.title.takeIf { it.isNotBlank() },
                    existingDescription = journey.description.takeIf { it.isNotBlank() },
                    coverImagePath = journey.coverImagePath.takeIf { it.isNotBlank() },
                    customPrompt = customPrompt
                )

                val endTime = System.currentTimeMillis()
                Log.d(TAG, "步骤4完成: AI 服务调用耗时 ${endTime - startTime}ms")

                // 5. 处理结果
                Log.d(TAG, "步骤5: 处理 AI 返回结果")
                result.fold(
                    onSuccess = { aiResult ->
                        Log.d(TAG, "步骤5成功: AI 生成成功")
                        Log.d(TAG, "生成的标题: '${aiResult.title}'")
                        Log.d(TAG, "生成的描述: '${aiResult.description}'")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                generatedTitle = aiResult.title,
                                generatedDescription = aiResult.description
                            )
                        }
                        // 通过回调返回结果（调用方会根据 selectedFields 决定哪些字段写入）
                        Log.d(TAG, "调用 onResult 回调...")
                        onResult(addressName, latitude, longitude, aiResult.title, aiResult.description)
                        Log.d(TAG, "========== AI 生成流程完成 ==========")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "步骤5失败: AI 生成失败", e)
                        Log.e(TAG, "错误类型: ${e.javaClass.simpleName}")
                        Log.e(TAG, "错误信息: ${e.message}")
                        _state.update {
                            it.copy(isLoading = false, error = "AI 生成失败: ${e.message}")
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "生成过程发生异常", e)
                Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
                Log.e(TAG, "异常信息: ${e.message}")
                Log.e(TAG, "异常堆栈: ${e.stackTraceToString()}")
                _state.update {
                    it.copy(isLoading = false, error = "生成失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * 清除涂鸦错误信息
     */
    fun clearPaintError() {
        _state.update { it.copy(paintError = null) }
    }

    /**
     * AI 封面涂鸦：将封面图片发送到服务端生成指定风格图片
     *
     * @param coverImagePath 封面图片的本地路径
     * @param prompt AI 提示词（由风格选项组合生成）
     * @param onResult 回调函数，返回涂鸦后的图片本地路径
     */
    fun paintCover(
        coverImagePath: String,
        prompt: String,
        onResult: (newImagePath: String) -> Unit
    ) {
        // 如果正在加载中，直接返回防止重复请求
        if (_state.value.isPaintLoading) {
            Log.w(TAG, "正在涂鸦中，忽略重复请求")
            return
        }

        Log.d(TAG, "========== AI 封面涂鸦流程开始 ==========")
        Log.d(TAG, "封面图片路径: $coverImagePath")

        viewModelScope.launch {
            // 1. 设置加载状态
            Log.d(TAG, "步骤1: 设置加载状态 isPaintLoading=true")
            _state.update { it.copy(isPaintLoading = true, paintError = null) }

            try {
                // 2. 调用服务端生成图片
                Log.d(TAG, "步骤2: 开始调用服务端生成图片...")
                val startTime = System.currentTimeMillis()

                val urlResult = aiService.generateImage(
                    imagePath = coverImagePath,
                    prompt = prompt
                )

                val endTime = System.currentTimeMillis()
                Log.d(TAG, "步骤2完成: 服务端调用耗时 ${endTime - startTime}ms")

                urlResult.fold(
                    onSuccess = { imageUrl ->
                        Log.d(TAG, "步骤2成功: 获取到图片 URL: $imageUrl")

                        // 3. 下载图片到本地
                        Log.d(TAG, "步骤3: 开始下载图片到本地...")
                        val savePath = generateSavePath()
                        Log.d(TAG, "保存路径: $savePath")

                        val downloadStartTime = System.currentTimeMillis()
                        val downloadResult = aiService.downloadImage(imageUrl, savePath)
                        val downloadEndTime = System.currentTimeMillis()
                        Log.d(TAG, "步骤3完成: 下载耗时 ${downloadEndTime - downloadStartTime}ms")

                        downloadResult.fold(
                            onSuccess = { localPath ->
                                Log.d(TAG, "步骤3成功: 图片保存到: $localPath")
                                _state.update {
                                    it.copy(isPaintLoading = false, paintedImagePath = localPath)
                                }
                                onResult(localPath)
                                Log.d(TAG, "========== AI 封面涂鸦流程完成 ==========")
                            },
                            onFailure = { e ->
                                Log.e(TAG, "步骤3失败: 下载图片失败", e)
                                _state.update {
                                    it.copy(isPaintLoading = false, paintError = "下载图片失败: ${e.message}")
                                }
                            }
                        )
                    },
                    onFailure = { e ->
                        Log.e(TAG, "步骤2失败: AI 涂鸦失败", e)
                        Log.e(TAG, "错误类型: ${e.javaClass.simpleName}")
                        Log.e(TAG, "错误信息: ${e.message}")
                        _state.update {
                            it.copy(isPaintLoading = false, paintError = "AI 涂鸦失败: ${e.message}")
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "涂鸦过程发生异常", e)
                Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
                Log.e(TAG, "异常信息: ${e.message}")
                Log.e(TAG, "异常堆栈: ${e.stackTraceToString()}")
                _state.update {
                    it.copy(isPaintLoading = false, paintError = "涂鸦失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 生成涂鸦图片的保存路径
     *
     * 使用时间戳确保文件名唯一
     */
    private fun generateSavePath(): String {
        val timestamp = System.currentTimeMillis()
        val dir = File(application.filesDir, "ai_painted_covers")
        dir.mkdirs()
        return File(dir, "cover_$timestamp.jpg").absolutePath
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _state.value = AiGenerateState()
    }
}
