package com.example.travel_footprint_android.data.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 服务网络请求封装
 *
 * 职责：
 * - 封装对服务端 /chat 接口的调用
 * - 构造 prompt 并发送请求
 * - 解析 AI 返回的标题和描述
 * - 处理网络异常和超时
 */
@Singleton
class AiService @Inject constructor() {

    // OkHttp 客户端，设置较长超时时间（图生图操作可能需要较长时间）
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)  // 图生图可能需要 5-10 分钟
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Gson 实例用于 JSON 序列化/反序列化
    private val gson = Gson()

    companion object {
        private const val TAG = "AiService"

        // 服务端基础地址（后续可提取到 BuildConfig 或配置文件）
        private const val SERVER_BASE_URL = "http://64.188.24.155:5000"
        private const val CHAT_URL = "$SERVER_BASE_URL/chat"

        // JSON 媒体类型
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * 请求数据类
     */
    private data class ChatRequest(
        @SerializedName("message") val message: String
    )

    /**
     * 响应数据类
     */
    private data class ChatResponse(
        @SerializedName("reply") val reply: String?
    )

    /**
     * AI 生成结果数据类
     */
    data class AiGenerateResult(
        val title: String,
        val description: String
    )

    /**
     * 足迹 AI 生成结果数据类
     */
    data class AiFootprintResult(
        val title: String,
        val description: String,
        val rating: Int
    )

    /**
     * 生成旅程标题和描述
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param addressName 地址名称（如"北京市"、"苏州市"）
     * @param existingTitle 用户已有的标题（可选）
     * @param existingDescription 用户已有的描述（可选）
     * @param coverImagePath 封面图片本地路径（可选，有则上传图片让 AI 结合图片内容生成）
     * @return Result<AiGenerateResult> 成功返回标题和描述，失败返回异常
     */
    suspend fun generateJourneyInfo(
        latitude: Double,
        longitude: Double,
        addressName: String,
        existingTitle: String? = null,
        existingDescription: String? = null,
        coverImagePath: String? = null,
        customPrompt: String? = null
    ): Result<AiGenerateResult> = withContext(Dispatchers.IO) {
        try {
            // 1. 构造 prompt
            Log.d(TAG, "===== 开始构造 prompt =====")
            val prompt = buildPrompt(
                latitude, longitude, addressName,
                existingTitle, existingDescription,
                hasImage = !coverImagePath.isNullOrBlank(),
                customPrompt = customPrompt
            )
            Log.d(TAG, "Prompt 内容:\n$prompt")

            // 2. 根据是否有封面图片选择请求方式
            val request = if (!coverImagePath.isNullOrBlank()) {
                // 有封面图片：使用 multipart/form-data 上传图片
                Log.d(TAG, "检测到封面图片，使用 multipart 请求: $coverImagePath")
                val imageFile = File(coverImagePath)
                if (!imageFile.exists()) {
                    Log.w(TAG, "封面图片文件不存在，回退到纯文本请求")
                    buildTextRequest(prompt)
                } else {
                    // 压缩图片后再上传，避免超时
                    Log.d(TAG, "原始图片大小: ${imageFile.length() / 1024} KB")
                    val compressedFile = compressImage(imageFile)
                    Log.d(TAG, "压缩后图片大小: ${compressedFile.length() / 1024} KB")

                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("message", prompt)
                        .addFormDataPart(
                            "image",
                            compressedFile.name,
                            compressedFile.asRequestBody("image/jpeg".toMediaType())
                        )
                        .build()
                    Request.Builder()
                        .url(CHAT_URL)
                        .post(requestBody)
                        .build()
                }
            } else {
                // 无封面图片：使用 JSON 纯文本请求
                Log.d(TAG, "无封面图片，使用 JSON 请求")
                buildTextRequest(prompt)
            }

            // 4. 发送请求并获取响应
            Log.d(TAG, "===== 开始发送网络请求 =====")
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "网络请求完成，耗时: ${endTime - startTime}ms")
            Log.d(TAG, "响应状态码: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "无响应体"
                Log.e(TAG, "请求失败: ${response.code} ${response.message}")
                Log.e(TAG, "错误响应体: $errorBody")
                return@withContext Result.failure(
                    IOException("请求失败: ${response.code} ${response.message} - $errorBody")
                )
            }

            // 5. 解析响应
            val responseBody = response.body?.string()
            Log.d(TAG, "响应体: $responseBody")

            if (responseBody == null) {
                Log.e(TAG, "响应体为空")
                return@withContext Result.failure(IOException("响应体为空"))
            }

            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            Log.d(TAG, "解析后的 reply: ${chatResponse.reply}")

            val reply = chatResponse.reply
            if (reply == null) {
                Log.e(TAG, "AI 返回内容为空")
                return@withContext Result.failure(IOException("AI 返回内容为空"))
            }

            // 6. 从 AI 回复中提取标题和描述
            Log.d(TAG, "===== 开始解析 AI 回复 =====")
            val result = parseAiReply(reply)
            if (result == null) {
                Log.e(TAG, "无法解析 AI 返回的内容")
                return@withContext Result.failure(
                    IOException("无法解析 AI 返回的内容，请重试")
                )
            }

            Log.d(TAG, "===== AI 生成成功 =====")
            Log.d(TAG, "标题: ${result.title}")
            Log.d(TAG, "描述: ${result.description}")
            Result.success(result)

        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON 解析错误", e)
            Result.failure(IOException("JSON 解析错误: ${e.message}"))
        } catch (e: IOException) {
            Log.e(TAG, "网络请求失败", e)
            Result.failure(IOException("网络请求失败: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "未知错误", e)
            Result.failure(Exception("未知错误: ${e.message}"))
        }
    }

    /**
     * 生成足迹标题、描述和评分
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param addressName 地址名称
     * @param journeyTitle 所属旅程标题
     * @param journeyDescription 所属旅程描述
     * @param existingTitle 用户已有的标题（可选）
     * @param existingDescription 用户已有的描述（可选）
     * @param existingRating 用户已有的评分（可选）
     * @param customPrompt 自定义提示词（可选）
     * @return Result<AiFootprintResult> 成功返回标题、描述和评分，失败返回异常
     */
    suspend fun generateFootprintInfo(
        latitude: Double,
        longitude: Double,
        addressName: String,
        journeyTitle: String,
        journeyDescription: String,
        existingTitle: String? = null,
        existingDescription: String? = null,
        existingRating: Int? = null,
        customPrompt: String? = null
    ): Result<AiFootprintResult> = withContext(Dispatchers.IO) {
        try {
            // 1. 构造 prompt
            Log.d(TAG, "===== 开始构造足迹 prompt =====")
            val prompt = buildFootprintPrompt(
                latitude, longitude, addressName,
                journeyTitle, journeyDescription,
                existingTitle, existingDescription, existingRating,
                customPrompt
            )
            Log.d(TAG, "Prompt 内容:\n$prompt")

            // 2. 构建纯文本 JSON 请求
            val request = buildTextRequest(prompt)

            // 3. 发送请求并获取响应
            Log.d(TAG, "===== 开始发送网络请求 =====")
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "网络请求完成，耗时: ${endTime - startTime}ms")
            Log.d(TAG, "响应状态码: ${response.code}")

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "无响应体"
                Log.e(TAG, "请求失败: ${response.code} ${response.message}")
                Log.e(TAG, "错误响应体: $errorBody")
                return@withContext Result.failure(
                    IOException("请求失败: ${response.code} ${response.message} - $errorBody")
                )
            }

            // 4. 解析响应
            val responseBody = response.body?.string()
            Log.d(TAG, "响应体: $responseBody")

            if (responseBody == null) {
                Log.e(TAG, "响应体为空")
                return@withContext Result.failure(IOException("响应体为空"))
            }

            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            val reply = chatResponse.reply
            if (reply == null) {
                Log.e(TAG, "AI 返回内容为空")
                return@withContext Result.failure(IOException("AI 返回内容为空"))
            }

            // 5. 从 AI 回复中提取标题、描述和评分
            Log.d(TAG, "===== 开始解析 AI 回复 =====")
            val result = parseFootprintAiReply(reply)
            if (result == null) {
                Log.e(TAG, "无法解析 AI 返回的内容")
                return@withContext Result.failure(
                    IOException("无法解析 AI 返回的内容，请重试")
                )
            }

            Log.d(TAG, "===== AI 足迹生成成功 =====")
            Log.d(TAG, "标题: ${result.title}")
            Log.d(TAG, "描述: ${result.description}")
            Log.d(TAG, "评分: ${result.rating}")
            Result.success(result)

        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON 解析错误", e)
            Result.failure(IOException("JSON 解析错误: ${e.message}"))
        } catch (e: IOException) {
            Log.e(TAG, "网络请求失败", e)
            Result.failure(IOException("网络请求失败: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "未知错误", e)
            Result.failure(Exception("未知错误: ${e.message}"))
        }
    }

    /**
     * 构造发送给 AI 的足迹 prompt
     */
    private fun buildFootprintPrompt(
        latitude: Double,
        longitude: Double,
        addressName: String,
        journeyTitle: String,
        journeyDescription: String,
        existingTitle: String?,
        existingDescription: String?,
        existingRating: Int?,
        customPrompt: String?
    ): String {
        val sb = StringBuilder()
        sb.appendLine("你是一个旅行足迹记录助手。请根据以下信息为用户的旅行足迹生成一个简洁的标题、一段生动的描述和一个评分建议。")
        sb.appendLine()

        sb.appendLine("所属旅程信息：")
        if (journeyTitle.isNotBlank()) {
            sb.appendLine("- 旅程标题：$journeyTitle")
        }
        if (journeyDescription.isNotBlank()) {
            sb.appendLine("- 旅程描述：$journeyDescription")
        }
        sb.appendLine()

        sb.appendLine("足迹位置信息：")
        sb.appendLine("- 经纬度：$latitude, $longitude")
        sb.appendLine("- 地址：$addressName")
        sb.appendLine()

        if (!existingTitle.isNullOrBlank() || !existingDescription.isNullOrBlank() || existingRating != null) {
            sb.appendLine("用户已有的信息（仅供参考，请在此基础上优化）：")
            if (!existingTitle.isNullOrBlank()) {
                sb.appendLine("- 标题：$existingTitle")
            }
            if (!existingDescription.isNullOrBlank()) {
                sb.appendLine("- 描述：$existingDescription")
            }
            if (existingRating != null) {
                sb.appendLine("- 评分：$existingRating 星")
            }
            sb.appendLine()
        }

        if (!customPrompt.isNullOrBlank()) {
            sb.appendLine("用户额外要求：")
            sb.appendLine(customPrompt)
            sb.appendLine()
        }

        sb.appendLine("要求：")
        sb.appendLine("1. 标题不超过20个字，简洁有吸引力")
        sb.appendLine("2. 描述不超过200个字，生动有趣，体现该足迹点的特色")
        sb.appendLine("3. 评分为1-5的整数，根据景点特色和体验价值给出合理建议")
        sb.appendLine()
        sb.appendLine("请严格按以下JSON格式返回，不要包含其他任何内容：")
        sb.append("{\"title\": \"生成的标题\", \"description\": \"生成的描述\", \"rating\": 4}")

        return sb.toString()
    }

    /**
     * 从 AI 回复中解析足迹的标题、描述和评分
     */
    private fun parseFootprintAiReply(reply: String): AiFootprintResult? {
        Log.d(TAG, "开始解析足迹 AI 回复，长度: ${reply.length}")

        // 尝试直接解析整个回复
        Log.d(TAG, "尝试方式1: 直接解析整个回复")
        try {
            val result = gson.fromJson(reply, AiFootprintResult::class.java)
            if (!result.title.isNullOrBlank() && !result.description.isNullOrBlank()) {
                Log.d(TAG, "方式1成功: title=${result.title}, description=${result.description}, rating=${result.rating}")
                return result
            }
            Log.d(TAG, "方式1失败: title 或 description 为空")
        } catch (e: JsonSyntaxException) {
            Log.d(TAG, "方式1失败: JSON 语法错误 - ${e.message}")
        }

        // 尝试提取 markdown 代码块中的 JSON
        Log.d(TAG, "尝试方式2: 提取 markdown 代码块")
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{.*?})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        val codeBlockMatch = codeBlockRegex.find(reply)
        if (codeBlockMatch != null) {
            try {
                val jsonStr = codeBlockMatch.groupValues[1]
                Log.d(TAG, "方式2提取的 JSON: $jsonStr")
                val result = gson.fromJson(jsonStr, AiFootprintResult::class.java)
                if (!result.title.isNullOrBlank() && !result.description.isNullOrBlank()) {
                    Log.d(TAG, "方式2成功: title=${result.title}, description=${result.description}, rating=${result.rating}")
                    return result
                }
                Log.d(TAG, "方式2失败: title 或 description 为空")
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "方式2失败: JSON 语法错误 - ${e.message}")
            }
        } else {
            Log.d(TAG, "方式2失败: 未找到 markdown 代码块")
        }

        // 尝试查找第一个 { 和最后一个 } 之间的内容
        Log.d(TAG, "尝试方式3: 查找 JSON 对象边界")
        val firstBrace = reply.indexOf('{')
        val lastBrace = reply.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            try {
                val jsonStr = reply.substring(firstBrace, lastBrace + 1)
                Log.d(TAG, "方式3提取的 JSON: $jsonStr")
                val result = gson.fromJson(jsonStr, AiFootprintResult::class.java)
                if (!result.title.isNullOrBlank() && !result.description.isNullOrBlank()) {
                    Log.d(TAG, "方式3成功: title=${result.title}, description=${result.description}, rating=${result.rating}")
                    return result
                }
                Log.d(TAG, "方式3失败: title 或 description 为空")
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "方式3失败: JSON 语法错误 - ${e.message}")
            }
        } else {
            Log.d(TAG, "方式3失败: 未找到 JSON 对象边界 (firstBrace=$firstBrace, lastBrace=$lastBrace)")
        }

        Log.e(TAG, "所有解析方式都失败")
        return null
    }

    /**
     * 压缩图片文件
     *
     * 压缩策略：
     * 1. 缩小尺寸：最长边限制在 1024px，等比缩放
     * 2. 降低质量：JPEG 质量压缩到 70%
     *
     * @param imageFile 原始图片文件
     * @return 压缩后的临时文件（调用方负责删除）
     */
    private fun compressImage(imageFile: File): File {
        // 1. 读取原始图片尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imageFile.absolutePath, options)
        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        Log.d(TAG, "原始图片尺寸: ${originalWidth}x${originalHeight}")

        // 2. 计算采样率（最长边限制在 1024px）
        val maxSide = 1024
        val sampleSize = calculateSampleSize(originalWidth, originalHeight, maxSide)
        Log.d(TAG, "采样率: $sampleSize")

        // 3. 按采样率解码图片
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, decodeOptions)
            ?: throw IOException("无法解码图片")

        // 4. 压缩为 JPEG 并保存到临时文件
        val tempFile = File.createTempFile("compressed_", ".jpg")
        val outputStream = FileOutputStream(tempFile)
        val quality = 70
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.close()
        bitmap.recycle()

        Log.d(TAG, "图片压缩完成: 原始 ${imageFile.length() / 1024} KB -> 压缩后 ${tempFile.length() / 1024} KB")
        return tempFile
    }

    /**
     * 计算采样率
     *
     * @param width 原始宽度
     * @param height 原始高度
     * @param maxSide 最长边限制
     * @return 采样率（必须是 2 的幂次）
     */
    private fun calculateSampleSize(width: Int, height: Int, maxSide: Int): Int {
        var sampleSize = 1
        val longerSide = maxOf(width, height)
        if (longerSide > maxSide) {
            sampleSize = Math.ceil(longerSide.toDouble() / maxSide).toInt()
        }
        // 采样率必须是 2 的幂次
        return when {
            sampleSize <= 1 -> 1
            sampleSize <= 2 -> 2
            sampleSize <= 4 -> 4
            sampleSize <= 8 -> 8
            else -> 16
        }
    }

    /**
     * 构造纯文本 JSON 请求体
     */
    private fun buildTextRequest(prompt: String): Request {
        val requestBodyJson = gson.toJson(ChatRequest(message = prompt))
        Log.d(TAG, "请求 JSON: $requestBodyJson")
        val requestBody = requestBodyJson.toRequestBody(JSON_MEDIA_TYPE)
        return Request.Builder()
            .url(CHAT_URL)
            .post(requestBody)
            .build()
    }

    /**
     * 构造发送给 AI 的 prompt
     *
     * @param hasImage 是否包含封面图片，若有则提示 AI 结合图片内容生成
     */
    private fun buildPrompt(
        latitude: Double,
        longitude: Double,
        addressName: String,
        existingTitle: String?,
        existingDescription: String?,
        hasImage: Boolean = false,
        customPrompt: String? = null
    ): String {
        val sb = StringBuilder()
        sb.appendLine("你是一个旅行助手。请根据以下信息为用户的旅程生成一个简洁的标题和一段生动的描述。")
        sb.appendLine()

        // 如果有封面图片，提示 AI 结合图片内容
        if (hasImage) {
            sb.appendLine("注意：用户上传了一张旅程封面图片，请结合图片内容和位置信息来生成更贴切的标题和描述。")
            sb.appendLine()
        }

        sb.appendLine("位置信息：")
        sb.appendLine("- 经纬度：$latitude, $longitude")
        sb.appendLine("- 地址：$addressName")
        sb.appendLine()

        if (!existingTitle.isNullOrBlank() || !existingDescription.isNullOrBlank()) {
            sb.appendLine("用户已有的信息（仅供参考，请在此基础上优化）：")
            if (!existingTitle.isNullOrBlank()) {
                sb.appendLine("- 标题：$existingTitle")
            }
            if (!existingDescription.isNullOrBlank()) {
                sb.appendLine("- 描述：$existingDescription")
            }
            sb.appendLine()
        }

        // 如果有自定义提示词，追加到 prompt 中
        if (!customPrompt.isNullOrBlank()) {
            sb.appendLine("用户额外要求：")
            sb.appendLine(customPrompt)
            sb.appendLine()
        }

        sb.appendLine("要求：")
        sb.appendLine("1. 标题不超过20个字，简洁有吸引力")
        sb.appendLine("2. 描述不超过200个字，生动有趣，体现旅行特色")
        sb.appendLine()
        sb.appendLine("请严格按以下JSON格式返回，不要包含其他任何内容：")
        sb.append("{\"title\": \"生成的标题\", \"description\": \"生成的描述\"}")

        return sb.toString()
    }

    /**
     * 从 AI 回复中解析标题和描述
     *
     * AI 可能返回纯 JSON，也可能在 JSON 前后添加了其他文字或 markdown 代码块标记
     * 此方法会尝试多种方式提取 JSON
     */
    private fun parseAiReply(reply: String): AiGenerateResult? {
        Log.d(TAG, "开始解析 AI 回复，长度: ${reply.length}")

        // 尝试直接解析整个回复
        Log.d(TAG, "尝试方式1: 直接解析整个回复")
        try {
            val result = gson.fromJson(reply, AiGenerateResult::class.java)
            if (!result.title.isNullOrBlank() && !result.description.isNullOrBlank()) {
                Log.d(TAG, "方式1成功: title=${result.title}, description=${result.description}")
                return result
            }
            Log.d(TAG, "方式1失败: title 或 description 为空")
        } catch (e: JsonSyntaxException) {
            Log.d(TAG, "方式1失败: JSON 语法错误 - ${e.message}")
        }

        // 尝试提取 markdown 代码块中的 JSON
        Log.d(TAG, "尝试方式2: 提取 markdown 代码块")
        val codeBlockRegex = Regex("```(?:json)?\\s*\\n?(\\{.*?})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        val codeBlockMatch = codeBlockRegex.find(reply)
        if (codeBlockMatch != null) {
            try {
                val jsonStr = codeBlockMatch.groupValues[1]
                Log.d(TAG, "方式2提取的 JSON: $jsonStr")
                val result = gson.fromJson(jsonStr, AiGenerateResult::class.java)
                if (!result.title.isNullOrBlank() && !result.description.isNullOrBlank()) {
                    Log.d(TAG, "方式2成功: title=${result.title}, description=${result.description}")
                    return result
                }
                Log.d(TAG, "方式2失败: title 或 description 为空")
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "方式2失败: JSON 语法错误 - ${e.message}")
            }
        } else {
            Log.d(TAG, "方式2失败: 未找到 markdown 代码块")
        }

        // 尝试查找第一个 { 和最后一个 } 之间的内容
        Log.d(TAG, "尝试方式3: 查找 JSON 对象边界")
        val firstBrace = reply.indexOf('{')
        val lastBrace = reply.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            try {
                val jsonStr = reply.substring(firstBrace, lastBrace + 1)
                Log.d(TAG, "方式3提取的 JSON: $jsonStr")
                val result = gson.fromJson(jsonStr, AiGenerateResult::class.java)
                if (!result.title.isNullOrBlank() && !result.description.isNullOrBlank()) {
                    Log.d(TAG, "方式3成功: title=${result.title}, description=${result.description}")
                    return result
                }
                Log.d(TAG, "方式3失败: title 或 description 为空")
            } catch (e: JsonSyntaxException) {
                Log.d(TAG, "方式3失败: JSON 语法错误 - ${e.message}")
            }
        } else {
            Log.d(TAG, "方式3失败: 未找到 JSON 对象边界 (firstBrace=$firstBrace, lastBrace=$lastBrace)")
        }

        Log.e(TAG, "所有解析方式都失败")
        return null
    }

    /**
     * 根据文件扩展名获取图片的 MIME 类型
     *
     * @param file 图片文件
     * @return MIME 类型字符串
     */
    private fun getImageMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            // 常见格式
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            
            // 专业格式
            "tiff", "tif" -> "image/tiff"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            
            // RAW 格式
            "raw" -> "image/x-raw"
            "arw" -> "image/x-sony-arw"
            "cr2" -> "image/x-canon-cr2"
            "cr3" -> "image/x-canon-cr3"
            "nef" -> "image/x-nikon-nef"
            "nrw" -> "image/x-nikon-nrw"
            "dng" -> "image/x-adobe-dng"
            "orf" -> "image/x-olympus-orf"
            "rw2" -> "image/x-panasonic-rw2"
            "pef" -> "image/x-pentax-pef"
            "srw" -> "image/x-samsung-srw"
            "raf" -> "image/x-fuji-raf"
            
            // 其他格式
            "psd" -> "image/vnd.adobe.photoshop"
            "ai" -> "application/postscript"
            "eps" -> "application/postscript"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            "avif" -> "image/avif"
            "jxl" -> "image/jxl"
            "jp2" -> "image/jp2"
            "j2k" -> "image/jp2"
            "jpf" -> "image/jp2"
            "jpm" -> "image/jpm"
            "jpx" -> "image/jpx"
            "jxr" -> "image/jxr"
            "wdp" -> "image/jxr"
            "apng" -> "image/apng"
            
            // 默认返回 JPEG
            else -> "image/jpeg"
        }
    }

    /**
     * 压缩图片用于 AI 生成
     *
     * 将图片缩放到 1920x1080 以内（保持宽高比），并转换为 WebP 格式以减小文件体积。
     * 压缩后的文件保存在应用缓存目录，文件名以 "ai_upload_" 为前缀。
     *
     * @param imageFile 原始图片文件
     * @return 压缩后的文件，失败返回 null
     */
    private fun compressImageForAI(imageFile: File): File? {
        return try {
            val MAX_WIDTH = 1920
            val MAX_HEIGHT = 1080
            val WEBP_QUALITY = 80

            // 1. 先解码获取图片尺寸（不加载完整 Bitmap 到内存）
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imageFile.absolutePath, options)

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight

            if (srcWidth <= 0 || srcHeight <= 0) {
                Log.e(TAG, "无法获取图片尺寸")
                return null
            }

            Log.d(TAG, "原始图片尺寸: ${srcWidth}x${srcHeight}")

            // 2. 计算缩放比例（保持宽高比，确保不超过最大尺寸）
            val ratio = minOf(
                MAX_WIDTH.toFloat() / srcWidth,
                MAX_HEIGHT.toFloat() / srcHeight,
                1f // 不放大，只缩小
            )

            val targetWidth = (srcWidth * ratio).toInt()
            val targetHeight = (srcHeight * ratio).toInt()

            // 3. 计算 inSampleSize（2 的幂次，减少内存占用）
            val sampleSize = calculateSampleSize(srcWidth, srcHeight, targetWidth, targetHeight)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val sampledBitmap = BitmapFactory.decodeFile(imageFile.absolutePath, decodeOptions)
                ?: return null

            // 4. 精确缩放到目标尺寸
            val scaledBitmap = if (sampledBitmap.width != targetWidth || sampledBitmap.height != targetHeight) {
                val result = Bitmap.createScaledBitmap(sampledBitmap, targetWidth, targetHeight, true)
                if (result !== sampledBitmap) sampledBitmap.recycle()
                result
            } else {
                sampledBitmap
            }

            Log.d(TAG, "缩放后尺寸: ${scaledBitmap.width}x${scaledBitmap.height}")

            // 5. 压缩为 WebP 格式写入缓存文件
            val compressedFile = File(imageFile.parent, "ai_upload_${System.currentTimeMillis()}.webp")
            FileOutputStream(compressedFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP, WEBP_QUALITY, out)
            }

            scaledBitmap.recycle()

            Log.d(TAG, "图片压缩完成: ${compressedFile.length() / 1024} KB")
            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "图片压缩失败", e)
            null
        }
    }

    /**
     * 计算 BitmapFactory 的 inSampleSize
     *
     * 返回 2 的幂次值，用于在解码阶段减少内存占用。
     */
    private fun calculateSampleSize(srcWidth: Int, srcHeight: Int, targetWidth: Int, targetHeight: Int): Int {
        var sampleSize = 1
        val halfWidth = srcWidth / 2
        val halfHeight = srcHeight / 2
        while (halfWidth / sampleSize >= targetWidth && halfHeight / sampleSize >= targetHeight) {
            sampleSize *= 2
        }
        return sampleSize
    }

    /**
     * AI 图生图：将封面图片发送到服务端生成手绘漫画风格图片
     *
     * @param imagePath 本地图片路径
     * @param prompt 提示词（如"改成手绘漫画风"）
     * @return Result<String> 成功返回生成的图片 URL，失败返回异常
     */
    suspend fun generateImage(
        imagePath: String,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "===== 开始 AI 图生图 =====")
            Log.d(TAG, "图片路径: $imagePath")
            Log.d(TAG, "提示词: $prompt")

            // 1. 读取本地图片文件
            val imageFile = File(imagePath)
            if (!imageFile.exists()) {
                Log.e(TAG, "图片文件不存在: $imagePath")
                return@withContext Result.failure(IOException("图片文件不存在"))
            }

            val originalSize = imageFile.length()
            Log.d(TAG, "原始图片大小: ${originalSize / 1024} KB")

            // 1.5 压缩图片：缩放到 1920x1080 以内 + 转换为 WebP 格式
            val compressedFile = compressImageForAI(imageFile)
                ?: imageFile // 压缩失败则使用原图
            val uploadFile = compressedFile
            Log.d(TAG, "压缩后图片大小: ${uploadFile.length() / 1024} KB, 路径: ${uploadFile.absolutePath}")

            // 2. 构建 Multipart 请求
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    uploadFile.name,
                    uploadFile.asRequestBody("image/webp".toMediaType())
                )
                .addFormDataPart("prompt", prompt)
                .build()

            // 3. 发送请求到 /generate-image
            val url = "$SERVER_BASE_URL/generate-image"
            Log.d(TAG, "请求 URL: $url")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "请求完成，耗时: ${endTime - startTime}ms")
            Log.d(TAG, "响应状态码: ${response.code}")

            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: "未知错误"
                Log.e(TAG, "请求失败: ${response.code} - $errorMsg")
                return@withContext Result.failure(IOException("请求失败: ${response.code}"))
            }

            // 4. 解析响应，返回图片 URL
            val responseBody = response.body?.string()
            Log.d(TAG, "响应体: $responseBody")

            if (responseBody == null) {
                Log.e(TAG, "响应体为空")
                return@withContext Result.failure(IOException("响应体为空"))
            }

            val result = gson.fromJson(responseBody, ImageGenerateResponse::class.java)
            Log.d(TAG, "解析结果: imageUrl=${result.imageUrl}")

            if (result.imageUrl.isNullOrBlank()) {
                Log.e(TAG, "返回的图片 URL 为空")
                return@withContext Result.failure(IOException("返回的图片 URL 为空"))
            }

            Log.d(TAG, "===== AI 图生图成功 =====")
            Result.success(result.imageUrl)

        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON 解析错误", e)
            Result.failure(IOException("JSON 解析错误: ${e.message}"))
        } catch (e: IOException) {
            Log.e(TAG, "网络请求失败", e)
            Result.failure(IOException("网络请求失败: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "未知错误", e)
            Result.failure(Exception("未知错误: ${e.message}"))
        }
    }

    /**
     * 下载图片到本地存储
     *
     * @param imageUrl 图片 URL
     * @param savePath 本地保存路径
     * @return Result<String> 成功返回本地文件路径，失败返回异常
     */
    suspend fun downloadImage(
        imageUrl: String,
        savePath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "===== 开始下载图片 =====")
            Log.d(TAG, "图片 URL: $imageUrl")
            Log.d(TAG, "保存路径: $savePath")

            val request = Request.Builder()
                .url(imageUrl)
                .build()

            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "下载完成，耗时: ${endTime - startTime}ms")

            if (!response.isSuccessful) {
                Log.e(TAG, "下载失败: ${response.code}")
                return@withContext Result.failure(IOException("下载失败: ${response.code}"))
            }

            val inputStream = response.body?.byteStream()
            if (inputStream == null) {
                Log.e(TAG, "响应体为空")
                return@withContext Result.failure(IOException("响应体为空"))
            }

            val file = File(savePath)
            file.parentFile?.mkdirs()
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            Log.d(TAG, "图片保存成功: ${file.absolutePath}")
            Log.d(TAG, "===== 图片下载完成 =====")
            Result.success(file.absolutePath)

        } catch (e: IOException) {
            Log.e(TAG, "网络请求失败", e)
            Result.failure(IOException("网络请求失败: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "下载图片失败", e)
            Result.failure(Exception("下载图片失败: ${e.message}"))
        }
    }

    /**
     * 图生图响应数据类
     */
    private data class ImageGenerateResponse(
        @SerializedName("image_url") val imageUrl: String?
    )
}
