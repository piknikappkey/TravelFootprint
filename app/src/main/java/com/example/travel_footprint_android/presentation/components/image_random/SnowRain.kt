/*
 * ============================================================================
 * SnowRain.kt - 雪花飘落特效组件
 * ============================================================================
 *
 * 【用途】
 *   - 在指定容器中以固定间隔随机生成雪花图片，营造"下雪"般的视觉特效
 *   - 用于旅程页面的装饰层，作为背景动效增强视觉氛围
 *
 * 【功能】
 *   1. 定时生成：每隔 intervalMs 毫秒在容器顶部随机位置生成一张雪花图片，最多 maxImages 张
 *   2. 飘落动画：雪花从顶部向下飘落，支持水平摆动和旋转效果
 *   3. 淡入淡出：每张雪花生成时伴随透明度淡入动画，移除时伴随淡出动画
 *   4. 自动消失：每张雪花到达底部或随机存活时长后自动移除
 *   5. 手动清除：通过 clearAllTrigger 增量触发一键清除所有雪花
 *
 * 【关联组件】
 *   - ImageRain: 类似的图片雨效果组件，参考其架构设计
 *   - bg_snow_1/2/3/4: 雪花图片资源
 *
 * 【实现逻辑简述】
 *   - 使用 BoxWithConstraints 获取容器实际尺寸，作为雪花随机位置的范围
 *   - mutableStateListOf<SnowImageData> 管理所有活跃雪花的数据列表
 *   - LaunchedEffect 协程循环：每隔 intervalMs 检查数量是否达上限，未达上限则生成新雪花数据
 *   - 通过 key(data.id) 确保每张雪花的 Compose 状态独立，移除后正确回收
 *   - clearCallbacks(Map<Long, () -> Unit>) 存储每张雪花的清除回调，clearAllTrigger 变化时遍历执行
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation.components.image_random

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * 单张雪花的数据类
 *
 * @param id 唯一标识符，用于 key() 和 clearCallbacks 映射
 * @param imageRes 雪花图片资源ID
 * @param startX 雪花起始水平位置（dp 原始值）
 * @param size 雪花显示尺寸（dp 原始值）
 * @param fallDuration 飘落动画持续时间（毫秒）
 * @param swayAmplitude 水平摆动幅度（dp）
 * @param swaySpeed 摆动速度（弧度/秒）
 * @param rotationSpeed 旋转速度（度/秒）
 * @param initialRotation 初始旋转角度（度）
 * @param delayStart 延迟开始时间（毫秒）
 */
private data class SnowImageData(
    val id: Long,
    val imageRes: Int,
    val startX: Int,
    val size: Int,
    val fallDuration: Int,
    val swayAmplitude: Float,
    val swaySpeed: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val delayStart: Int,
)

/**
 * 雪花飘落特效 Composable 函数
 *
 * 在指定容器中以固定间隔随机生成雪花图片，支持飘落、摆动、旋转、淡入淡出和手动清除。
 *
 * @param modifier 外部 Modifier，用于整体布局修饰，最终作用于 BoxWithConstraints
 * @param intervalMs 雪花生成间隔时间（毫秒）
 * @param fadeInMs 淡入/淡出动画持续时间（毫秒）
 * @param maxImages 同时存在的最大雪花数量
 * @param minSize 雪花最小随机尺寸（dp）
 * @param maxSize 雪花最大随机尺寸（dp）
 * @param minFallDuration 最小飘落时长（毫秒）
 * @param maxFallDuration 最大飘落时长（毫秒）
 * @param minSwayAmplitude 最小水平摆动幅度（dp）
 * @param maxSwayAmplitude 最大水平摆动幅度（dp）
 * @param minRotationSpeed 最小旋转速度（度/秒）
 * @param maxRotationSpeed 最大旋转速度（度/秒）
 * @param minDelayStart 最小延迟开始时间（毫秒）
 * @param maxDelayStart 最大延迟开始时间（毫秒）
 * @param snowEnabled 是否启用雪花效果
 * @param clearAllTrigger 一键清除触发器，每次自增时清除所有活跃雪花
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SnowRain(
    modifier: Modifier = Modifier,
    intervalMs: Long = 300,
    fadeInMs: Int = 500,
    maxImages: Int = 50,
    minSize: Int = 20,
    maxSize: Int = 60,
    minFallDuration: Int = 8000,
    maxFallDuration: Int = 15000,
    minSwayAmplitude: Int = 5,
    maxSwayAmplitude: Int = 25,
    minRotationSpeed: Float = 10f,
    maxRotationSpeed: Float = 30f,
    minDelayStart: Int = 0,
    maxDelayStart: Int = 3000,
    snowEnabled: Boolean = true,
    clearAllTrigger: Int = 0,
    imageRainViewModel: ImageRainViewModel = hiltViewModel(key = "image-rain")
) {
    // 雪花图片资源列表
    val snowImages = remember {
        listOf(
            R.drawable.bg_snow_1,
            R.drawable.bg_snow_2,
            R.drawable.bg_snow_3,
            R.drawable.bg_snow_4
        )
    }

    // 活跃雪花数据列表
    val snowflakes = remember { mutableStateListOf<SnowImageData>() }
    // 自增 ID 计数器
    var nextId by remember { mutableStateOf(0L) }

    // 清除回调映射表
    val clearCallbacks = remember { mutableMapOf<Long, () -> Unit>() }

    // 雪花透明度动画
    var prevSnowEnabled by remember { mutableStateOf(snowEnabled) }
    var zIndex by remember { mutableStateOf(0f) }

    // 清除触发器
    var prevClearAllTrigger by remember { mutableStateOf(clearAllTrigger) }

    // 一键清除触发
    if (clearAllTrigger != prevClearAllTrigger) {
        prevClearAllTrigger = clearAllTrigger
        clearCallbacks.values.forEach { it() }
    }

    // 雪花启用状态变化时更新z-index
    LaunchedEffect(snowEnabled) {
        if (!snowEnabled) {
            delay(300)
            zIndex = -1f
        } else {
            zIndex = 0f
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().alpha(if (snowEnabled) 1f else 0f)) {
        val boxWidth = maxWidth
        val boxHeight = maxHeight
        val density = LocalDensity.current

        // 雪花生成协程
        LaunchedEffect(snowEnabled, maxImages, intervalMs, minSize, maxSize, minDelayStart, maxDelayStart) {
            if (!snowEnabled) return@LaunchedEffect
            delay(500)
            while (true) {
                delay(intervalMs)

                if (snowflakes.size >= maxImages) continue

                // 随机选择雪花图片
                val imageRes = snowImages[Random.nextInt(snowImages.size)]

                // 随机雪花尺寸
                val snowSize = Random.nextInt(minSize, maxSize + 1)

                // 随机起始水平位置（确保雪花在容器内）
                val maxX = (boxWidth - snowSize.dp).coerceAtLeast(0.dp)
                val startX = if (maxX > 0.dp) Random.nextInt(0, maxX.value.toInt() + 1) else 0

                // 随机飘落时长
                val fallDuration = Random.nextInt(minFallDuration, maxFallDuration + 1)

                // 随机摆动参数 - 使用更自然的摆动
                val swayAmplitude = Random.nextFloat() * (maxSwayAmplitude - minSwayAmplitude) + minSwayAmplitude
                val swaySpeed = Random.nextFloat() * 1.5f + 0.5f // 0.5-2.0 弧度/秒

                // 随机旋转速度 - 更慢更自然
                val rotationSpeed = Random.nextFloat() * (maxRotationSpeed - minRotationSpeed) + minRotationSpeed

                // 随机初始旋转角度
                val initialRotation = Random.nextFloat() * 360f

                // 随机延迟开始时间
                val delayStart = Random.nextInt(minDelayStart, maxDelayStart + 1)

                val id = nextId
                nextId++

                snowflakes.add(
                    SnowImageData(
                        id = id,
                        imageRes = imageRes,
                        startX = startX,
                        size = snowSize,
                        fallDuration = fallDuration,
                        swayAmplitude = swayAmplitude,
                        swaySpeed = swaySpeed,
                        rotationSpeed = rotationSpeed,
                        initialRotation = initialRotation,
                        delayStart = delayStart
                    )
                )
            }
        }

        // 渲染所有活跃雪花
        snowflakes.forEach { data ->
            key(data.id) {
                val alpha = remember { Animatable(0f) }
                var show by remember { mutableStateOf(true) }
                var startTime by remember { mutableStateOf(0L) }

                // 注册清除回调
                remember(data.id) {
                    val callback = { show = false }
                    clearCallbacks[data.id] = callback
                    callback
                }

                // 延迟开始
                LaunchedEffect(Unit) {
                    if (data.delayStart > 0) {
                        delay(data.delayStart.toLong())
                    }
                    startTime = System.currentTimeMillis()
                    alpha.animateTo(1f, animationSpec = tween(fadeInMs))
                }

                // 飘落动画 - 使用时间驱动的正弦摆动
                val fallProgress = remember { Animatable(0f) }
                LaunchedEffect(startTime) {
                    if (startTime == 0L) return@LaunchedEffect
                    fallProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = data.fallDuration,
                            easing = LinearEasing
                        )
                    )
                    // 飘落完成后淡出
                    show = false
                }

                // 旋转动画 - 缓慢旋转
                val rotation = remember { Animatable(data.initialRotation) }
                LaunchedEffect(startTime) {
                    if (startTime == 0L) return@LaunchedEffect
                    while (true) {
                        delay(16) // ~60fps
                        rotation.snapTo(rotation.value + data.rotationSpeed * 0.016f)
                    }
                }

                // 淡出动画
                LaunchedEffect(show) {
                    if (!show) {
                        alpha.animateTo(0f, animationSpec = tween(fadeInMs))
                        snowflakes.remove(data)
                        clearCallbacks.remove(data.id)
                    }
                }

                // 计算当前位置 - 使用正弦函数实现自然摆动
                val currentTime = System.currentTimeMillis()
                val elapsedTime = if (startTime > 0) currentTime - startTime else 0L
                val swayAngle = elapsedTime * data.swaySpeed * 0.001f // 转换为弧度
                val swayOffset = data.swayAmplitude * sin(swayAngle)

                val currentY = with(density) {
                    (boxHeight * fallProgress.value).toPx()
                }
                val currentX = with(density) {
                    (data.startX.dp + swayOffset.dp).toPx()
                }

                // 渲染雪花
                if (startTime > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(zIndex)
                    ) {
                        Image(
                            painter = painterResource(id = data.imageRes),
                            contentDescription = "雪花",
                            modifier = Modifier
                                .size(data.size.dp)
                                .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                                .alpha(alpha.value)
                                .rotate(rotation.value),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
