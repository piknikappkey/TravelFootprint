/*
 * ============================================================================
 * ImageRain.kt - 图片雨（涂鸦雨）特效组件
 * ============================================================================
 *
 * 【用途】
 *   - 在指定容器中以固定间隔随机生成可拖拽的涂鸦图片，营造"下雨"般的视觉特效
 *   - 用于旅程页面的装饰层（JourneyScreen2），作为背景动效增强视觉趣味性
 *
 * 【功能】
 *   1. 定时生成：每隔 intervalMs 毫秒在容器随机位置生成一张涂鸦图片，最多 maxImages 张
 *   2. 淡入淡出：每张图片生成时伴随透明度淡入动画，移除时伴随淡出动画
 *   3. 可拖拽旋转：每张图片（ImageRandom）支持用户拖拽移动和按压旋转
 *   4. 自动消失：每张图片到达随机存活时长后自动移除
 *   5. 手动清除：通过 clearAllTrigger 增量触发一键清除所有图片
 *   6. 参数日志：所有配置参数变化时打印 Logcat 日志，便于调试 RainSettingDialog
 *
 * 【关联组件】
 *   - ImageRandom: 单张随机涂鸦图片组件，支持随机位置/尺寸/角度，拖拽、按压缩放、旋转、点击移除
 *   - getRandomScrawlDrawable() (ImageUtils.kt): 通过反射扫描 R.drawable 中 ic_scrawl* 资源，随机返回一个资源 ID
 *   - RainImageData: 本文件内私有数据类，存储单张图片的 id、偏移位置、尺寸
 *   - JourneyScreen2: 调用方，在旅程页面中使用 ImageRain 作为背景特效层
 *   - RainSettingDialog: 配置面板，通过回调调整 ImageRain 的各项参数
 *
 * 【实现逻辑简述】
 *   - 使用 BoxWithConstraints 获取容器实际尺寸，作为图片随机位置的范围
 *   - mutableStateListOf<RainImageData> 管理所有活跃图片的数据列表
 *   - LaunchedEffect 协程循环：每隔 intervalMs 检查数量是否达上限，未达上限则生成新图片数据
 *   - 通过 key(data.id) 确保每张图片的 Compose 状态独立，移除后正确回收
 *   - clearCallbacks(Map<Long, () -> Unit>) 存储每张图片的清除回调，clearAllTrigger 变化时遍历执行
 *   - 所有参数变化通过 prev* 状态检测并打印 Log.d 日志
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation2.components.image_random

// Android 注解
import android.annotation.SuppressLint                     // 抑制特定 Lint 警告
import android.util.Log                                   // Android 日志工具

// Compose 动画
import androidx.compose.animation.core.Animatable          // 可手动控制的可变动画值
import androidx.compose.animation.core.tween               // 线性缓动动画规格

// Compose 布局
import androidx.compose.foundation.layout.Box              // 层叠布局容器
import androidx.compose.foundation.layout.BoxWithConstraints // 可获取约束信息的 Box 容器
import androidx.compose.foundation.layout.fillMaxSize      // 填充父容器全部尺寸

// Compose 运行时
import androidx.compose.runtime.Composable                 // 声明 Composable 函数
import androidx.compose.runtime.LaunchedEffect             // 在组合时启动协程
import androidx.compose.runtime.getValue                   // 读取 State 值
import androidx.compose.runtime.key                        // 为 Composable 提供稳定 key 标识
import androidx.compose.runtime.mutableStateListOf         // 创建 Compose 可观察的可变列表
import androidx.compose.runtime.mutableStateOf             // 创建可变状态
import androidx.compose.runtime.remember                   // 记住值避免重组时丢失
import androidx.compose.runtime.setValue                   // 修改 State 值

// Compose UI 修饰符
import androidx.compose.ui.Modifier                        // UI 修饰符链
import androidx.compose.ui.unit.Dp                         // 密度无关像素类型
import androidx.compose.ui.unit.dp                         // dp 扩展属性

// 协程工具
import kotlinx.coroutines.delay                            // 协程延迟函数
import kotlin.random.Random                                // Kotlin 随机数工具

/**
 * 单张图片雨的数据类
 *
 * @param id 唯一标识符，用于 key() 和 clearCallbacks 映射
 * @param offsetX 图片在容器中的水平偏移量（dp 原始值 = Int）
 * @param offsetY 图片在容器中的垂直偏移量（dp 原始值 = Int）
 * @param size 图片显示尺寸（dp 原始值 = Int）
 */
private data class RainImageData(
    val id: Long,
    val offsetX: Int,
    val offsetY: Int,
    val size: Int,
)

/**
 * 图片雨（涂鸦雨）特效 Composable 函数
 *
 * 在指定容器中以固定间隔随机生成可拖拽的涂鸦图片，支持拖拽、旋转、自动消失和手动清除。
 *
 * @param modifier 外部 Modifier，用于整体布局修饰，最终作用于 BoxWithConstraints
 * @param intervalMs 图片生成间隔时间（毫秒）
 * @param fadeInMs 淡入/淡出动画持续时间（毫秒）
 * @param maxImages 同时存在的最大图片数量
 * @param size 图片固定尺寸（dp），为 0 时在 [minSize, maxSize] 范围内随机取值
 * @param minSize 图片最小随机尺寸（dp）
 * @param maxSize 图片最大随机尺寸（dp）
 * @param minAngle 图片最小随机初始旋转角度（度）
 * @param maxAngle 图片最大随机初始旋转角度（度）
 * @param minExistenceTime 图片最小存活时长（毫秒），为 0 且 maxExistenceTime 也为 0 时永不自动消失
 * @param maxExistenceTime 图片最大存活时长（毫秒）
 * @param isChaos 是否启用混沌旋转模式（按住拖拽时持续旋转图片）
 * @param pressScale 按压时图片放大的像素量（dp）
 * @param rotationSpeed 旋转速度（度/秒）
 * @param clearAllTrigger 一键清除触发器，每次自增时清除所有活跃图片
 */
@SuppressLint("UnusedBoxWithConstraintsScope")  // 抑制 BoxWithConstraints 作用域未使用的警告
@Composable
fun ImageRain(
    modifier: Modifier = Modifier,
    intervalMs: Long = 1000L,
    fadeInMs: Int = 500,
    maxImages: Int = 10,
    size: Int = 0,
    minSize: Int = 30,
    maxSize: Int = 50,
    minAngle: Int = 0,
    maxAngle: Int = 360,
    minExistenceTime: Int = 10000,
    maxExistenceTime: Int = 20000,
    isChaos: Boolean = false,
    pressScale: Float = 20f,
    rotationSpeed: Float = 30f,
    clearAllTrigger: Int = 0,
) {
    // 活跃图片数据列表（Compose 可观察可变列表，添加/移除自动触发重组）
    val images = remember { mutableStateListOf<RainImageData>() }
    // 自增 ID 计数器，确保每张图片有唯一标识
    var nextId by remember { mutableStateOf(0L) }

    // ==================== 参数变化日志记录 ====================
    // 每组参数通过保存上一次值（prev*）并与当前值对比，变化时打印 Log
    var prevIntervalMs by remember { mutableStateOf(intervalMs) }
    var prevFadeInMs by remember { mutableStateOf(fadeInMs) }
    var prevMaxImages by remember { mutableStateOf(maxImages) }
    var prevSize by remember { mutableStateOf(size) }
    var prevMinSize by remember { mutableStateOf(minSize) }
    var prevMaxSize by remember { mutableStateOf(maxSize) }
    var prevMinAngle by remember { mutableStateOf(minAngle) }
    var prevMaxAngle by remember { mutableStateOf(maxAngle) }
    var prevMinExistenceTime by remember { mutableStateOf(minExistenceTime) }
    var prevMaxExistenceTime by remember { mutableStateOf(maxExistenceTime) }
    var prevIsChaos by remember { mutableStateOf(isChaos) }
    var prevPressScale by remember { mutableStateOf(pressScale) }
    var prevRotationSpeed by remember { mutableStateOf(rotationSpeed) }
    var prevClearAllTrigger by remember { mutableStateOf(clearAllTrigger) }

    // 清除回调映射表：key 为图片 id，value 为对应图片的清除回调
    // 用于 clearAllTrigger 触发时一键调用所有图片的淡出移除逻辑
    val clearCallbacks = remember { mutableMapOf<Long, () -> Unit>() }

    // 检测各参数变化并打印 Log 日志
    if (intervalMs != prevIntervalMs) {
        Log.d("ImageRain", "intervalMs changed to: $intervalMs")
        prevIntervalMs = intervalMs
    }
    if (fadeInMs != prevFadeInMs) {
        Log.d("ImageRain", "fadeInMs changed to: $fadeInMs")
        prevFadeInMs = fadeInMs
    }
    if (maxImages != prevMaxImages) {
        Log.d("ImageRain", "maxImages changed to: $maxImages")
        prevMaxImages = maxImages
    }
    if (size != prevSize) {
        Log.d("ImageRain", "size changed to: $size")
        prevSize = size
    }
    if (minSize != prevMinSize) {
        Log.d("ImageRain", "minSize changed to: $minSize")
        prevMinSize = minSize
    }
    if (maxSize != prevMaxSize) {
        Log.d("ImageRain", "maxSize changed to: $maxSize")
        prevMaxSize = maxSize
    }
    if (minAngle != prevMinAngle) {
        Log.d("ImageRain", "minAngle changed to: $minAngle")
        prevMinAngle = minAngle
    }
    if (maxAngle != prevMaxAngle) {
        Log.d("ImageRain", "maxAngle changed to: $maxAngle")
        prevMaxAngle = maxAngle
    }
    if (minExistenceTime != prevMinExistenceTime) {
        Log.d("ImageRain", "minExistenceTime changed to: $minExistenceTime")
        prevMinExistenceTime = minExistenceTime
    }
    if (maxExistenceTime != prevMaxExistenceTime) {
        Log.d("ImageRain", "maxExistenceTime changed to: $maxExistenceTime")
        prevMaxExistenceTime = maxExistenceTime
    }
    if (isChaos != prevIsChaos) {
        Log.d("ImageRain", "isChaos changed to: $isChaos")
        prevIsChaos = isChaos
    }
    if (pressScale != prevPressScale) {
        Log.d("ImageRain", "pressScale changed to: $pressScale")
        prevPressScale = pressScale
    }
    if (rotationSpeed != prevRotationSpeed) {
        Log.d("ImageRain", "rotationSpeed changed to: $rotationSpeed")
        prevRotationSpeed = rotationSpeed
    }

    // 一键清除触发：clearAllTrigger 变化时遍历执行所有图片的清除回调
    if (clearAllTrigger != prevClearAllTrigger) {
        Log.d("ImageRain", "clearAllTrigger triggered")
        prevClearAllTrigger = clearAllTrigger
        clearCallbacks.values.forEach { it() }  // 逐个调用淡出移除回调
    }

    // 获取容器实际尺寸（由父布局约束决定），用于计算图片随机位置范围
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val boxWidth = maxWidth   // 容器可用宽度（Dp）
        val boxHeight = maxHeight // 容器可用高度（Dp）

        // 图片生成协程：定时生成新图片数据
        // key 为 maxImages/intervalMs/minSize/maxSize，任一变化时重启协程
        LaunchedEffect(maxImages, intervalMs, minSize, maxSize) {
            while (true) {
                delay(intervalMs)  // 等待指定间隔

                // 数量达上限则跳过本轮生成
                if (images.size >= maxImages) continue

                // 确定图片尺寸：固定 size > 0 时使用固定值，否则在 [minSize, maxSize] 内随机
                val imgSize = if (size > 0) size else Random.nextInt(minSize, maxSize + 1)
                val imgSizeDp = imgSize.dp

                // 计算有效偏移范围（确保图片完全在容器内）
                val maxXDp = (boxWidth - imgSizeDp).coerceAtLeast(0.dp)
                val maxYDp = (boxHeight - imgSizeDp).coerceAtLeast(0.dp)

                // 随机生成偏移位置
                val offsetX = randomDpOffset(maxXDp)
                val offsetY = randomDpOffset(maxYDp)

                // 分配唯一 ID 并递增计数器
                val id = nextId
                nextId++

                // 将新图片数据添加到列表，触发 Compose 渲染
                images.add(RainImageData(id = id, offsetX = offsetX, offsetY = offsetY, size = imgSize))
            }
        }

        // 渲染所有活跃图片
        images.forEach { data ->
            // key(data.id) 确保 Compose 根据唯一 ID 跟踪每张图片，正确触发添加/移除动画
            key(data.id) {
                // 透明度动画控制器（淡入/淡出）
                val alpha = remember { Animatable(0f) }
                // 控制图片是否显示（true=淡入显示中，false=触发淡出后移除）
                var show by remember { mutableStateOf(true) }

                // 注册清除回调：将 data.id 映射到 { show = false } 回调
                // show = false 将触发淡出动画 + 从列表移除
                remember(data.id) {
                    val callback = { show = false }
                    clearCallbacks[data.id] = callback
                    callback
                }

                // 响应 show 状态变化的动画效果
                // show=true → 淡入到不透明；show=false → 淡出到透明后从列表移除
                LaunchedEffect(show) {
                    if(show) {
                        alpha.animateTo(1f, animationSpec = tween(fadeInMs))  // 淡入
                    } else {
                        alpha.animateTo(0f, animationSpec = tween(fadeInMs))  // 淡出
                        images.remove(data)            // 从活跃列表移除
                        clearCallbacks.remove(data.id) // 清除回调映射
                    }
                }

                // 全屏 Box 作为每张图片的渲染容器
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 单张随机涂鸦图片：使用预计算的固定位置和尺寸
                    // minOffsetX == maxOffsetX 确保图片位置由 RainImageData 决定（不自带随机性）
                    ImageRandom(
                        minOffsetX = data.offsetX,
                        maxOffsetX = data.offsetX,
                        minOffsetY = data.offsetY,
                        maxOffsetY = data.offsetY,
                        minSize = data.size,
                        maxSize = data.size,
                        minAngle = minAngle,
                        maxAngle = maxAngle,
                        alpha = alpha.value,              // 由淡入淡出动画控制
                        minExistenceTime = minExistenceTime,
                        maxExistenceTime = maxExistenceTime,
                        onRemove = {
                            show = false                   // 触发淡出移除流程
                        },
                        isChaos = isChaos,
                        pressScale = pressScale,
                        rotationSpeed = rotationSpeed,
                        containerWidth = boxWidth,         // 传递容器尺寸用于边界约束
                        containerHeight = boxHeight,
                    )
                }
            }
        }
    }
}

/**
 * 在 [0, maxDp] 范围内生成随机整数偏移值
 *
 * 用于在容器边界内随机放置图片
 *
 * @param maxDp 最大偏移值（dp），小于等于 0 时返回 0
 * @return 随机整数偏移值
 */
private fun randomDpOffset(maxDp: Dp): Int {
    return if (maxDp > 0.dp) Random.nextInt(0, (maxDp.value.toInt()) + 1) else 0
}
