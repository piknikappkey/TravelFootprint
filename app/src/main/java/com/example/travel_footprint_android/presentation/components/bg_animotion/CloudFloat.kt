/*
 * ============================================================================
 * CloudFloat.kt - 云朵浮动特效组件（性能优化版）
 * ============================================================================
 *
 * 【用途】
 *   - 在指定容器顶部区域以固定间隔随机生成云朵图片，营造云朵在天空中自然浮动的视觉效果
 *   - 用于页面背景装饰层，作为背景动效增强视觉氛围
 *
 * 【功能】
 *   1. 定时生成：每隔 intervalMs 毫秒在容器顶部区域随机位置生成一张云朵图片
 *   2. 横向漂移：云朵从生成位置缓慢向一侧漂移（模拟风吹效果）
 *   3. 上下浮动：云朵在漂移过程中伴有轻微的上下浮动，模拟真实云朵状态
 *   4. 淡入淡出：每张云朵生成时伴随透明度淡入动画，飘出边界后淡出移除
 *   5. 景深效果：不同云朵尺寸不同，配合不同透明度，营造层次感
 *   6. 自动消失：云朵飘出屏幕边界后自动移除
 *
 * 【性能优化要点】
 *   1. 所有动画状态通过 Animatable 在协程中驱动，不依赖 recomposition
 *   2. 位置计算在 graphicsLayer 中执行，仅触发 draw pass，不触发 measure/layout
 *   3. 父组件重组（面板切换等）不会导致云朵重新计算，实现动画与 UI 状态隔离
 *   4. 使用 withFrameNanos 驱动帧更新，保证动画流畅度
 *
 * 【关联组件】
 *   - bg_cloud_1/2/3: 云朵图片资源
 *   - SnowRain: 类似的雪花飘落效果组件，参考其架构设计
 *
 * 【实现逻辑简述】
 *   - 使用 BoxWithConstraints 获取容器实际尺寸
 *   - mutableStateListOf<CloudData> 管理所有活跃云朵的数据列表
 *   - LaunchedEffect 协程循环：每隔 intervalMs 生成新云朵数据
 *   - 通过 key(data.id) 确保每张云朵的 Compose 状态独立，移除后正确回收
 *   - 位置和透明度通过 graphicsLayer 在 draw 阶段更新，不影响子组件 measure/layout
 *   - 帧驱动使用 withFrameNanos 而非 delay(16)，与系统帧回调精确同步
 * ============================================================================
 */

package com.example.travel_footprint_android.presentation.components.bg_animotion

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * 云朵移动方向枚举
 *
 * 用于控制云朵的整体漂移行为，影响云朵的起始位置和移动方向。
 */
enum class CloudDirection {
    /** 从左向右漂移：云朵从屏幕左侧外进入，向右缓慢移动 */
    LEFT_TO_RIGHT,
    /** 从右向左漂移：云朵从屏幕右侧外进入，向左缓慢移动 */
    RIGHT_TO_LEFT,
    /** 静止不动：云朵在屏幕内随机位置生成，仅在原地做轻微上下浮动 */
    STATIC
}

/**
 * 单张云朵的数据类
 *
 * 存储一张云朵的所有静态配置参数，由生成协程创建后不可变。
 * 动画状态（位置、透明度）通过 Compose 状态在运行时动态更新。
 *
 * @param id 唯一标识符，用于 key() 和 mutableStateListOf 管理
 * @param imageRes 云朵图片资源ID（bg_cloud_1/2/3 中随机选取）
 * @param startX 起始水平位置（像素），漂移模式为屏幕外，静止模式为屏幕内随机
 * @param startY 起始垂直位置（像素），在 [cloudAreaTopRatio, cloudAreaBottomRatio] 范围内随机
 * @param size 云朵显示尺寸（dp），在 [minSize, maxSize] 范围内随机
 * @param driftDirection 漂移方向：1=向右，-1=向左，0=静止
 * @param driftSpeed 水平漂移速度（像素/秒），大云朵更慢以营造景深感
 * @param bobAmplitude 上下浮动幅度（dp），静止模式幅度更大（4-10dp），漂移模式更小（2-6dp）
 * @param bobSpeed 上下浮动速度（弧度/秒），控制正弦波频率
 * @param alphaValue 云朵目标透明度，由尺寸因子和 baseAlpha 共同决定
 * @param fadeInDuration 淡入动画持续时间（毫秒）
 * @param isStatic 是否为静止模式，决定使用漂移动画还是浮动动画
 */
private data class CloudData(
    val id: Long,
    val imageRes: Int,
    val startX: Float,
    val startY: Float,
    val size: Int,
    val driftDirection: Int,
    val driftSpeed: Float,
    val bobAmplitude: Float,
    val bobSpeed: Float,
    val alphaValue: Float,
    val fadeInDuration: Int,
    val isStatic: Boolean,
)

/**
 * 云朵浮动特效 Composable 函数
 *
 * 在指定容器顶部区域以固定间隔随机生成云朵图片，模拟云朵在天空中自然浮动。
 * 所有动画通过协程 + graphicsLayer 驱动，父组件重组不会导致动画卡顿。
 *
 * @param modifier 外部 Modifier，用于整体布局修饰，最终作用于 BoxWithConstraints
 * @param intervalMs 云朵生成间隔时间（毫秒），值越小云朵生成越密集
 * @param maxClouds 同时存在的最大云朵数量，达到上限后暂停生成
 * @param minSize 云朵最小随机尺寸（dp）
 * @param maxSize 云朵最大随机尺寸（dp），大云朵透明度更低（更清晰），移动更慢
 * @param minDriftSpeed 最小水平漂移速度（dp/秒）
 * @param maxDriftSpeed 最大水平漂移速度（dp/秒）
 * @param cloudAreaTopRatio 云朵分布区域顶部占容器高度的比例，值越小云朵越靠近顶部（0.0f=最顶端）
 * @param cloudAreaBottomRatio 云朵分布区域底部占容器高度的比例，控制云朵在页面上半部分的显示范围
 * @param baseAlpha 整体透明度系数（0f~1f），值越大云朵越清晰；默认1f为原始效果，0.5f为半透明
 * @param direction 云朵移动方向：LEFT_TO_RIGHT=从左到右，RIGHT_TO_LEFT=从右到左，STATIC=静止仅微微浮动
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CloudFloat(
    modifier: Modifier = Modifier,
    intervalMs: Long = 2500,
    maxClouds: Int = 6,
    minSize: Int = 80,
    maxSize: Int = 200,
    minDriftSpeed: Float = 8f,
    maxDriftSpeed: Float = 25f,
    cloudAreaTopRatio: Float = 0.02f,
    cloudAreaBottomRatio: Float = 0.35f,
    baseAlpha: Float = 1f,
    direction: CloudDirection = CloudDirection.LEFT_TO_RIGHT,
) {
    // ============================================================
    // 1. 初始化资源和状态
    // ============================================================

    // 云朵图片资源列表，remember 确保只创建一次
    val cloudImages = remember {
        listOf(R.drawable.bg_cloud_1, R.drawable.bg_cloud_2, R.drawable.bg_cloud_3)
    }

    // 活跃云朵数据列表（Compose 可观察可变列表，添加/移除自动触发重组）
    val clouds = remember { mutableStateListOf<CloudData>() }

    // 自增 ID 计数器，确保每张云朵有唯一标识
    var nextId by remember { mutableStateOf(0L) }

    // ============================================================
    // 2. 获取容器尺寸（BoxWithConstraints 提供实际可用宽高）
    // ============================================================

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        // 将 Dp 转换为像素，供后续位置计算使用
        val boxWidthPx = with(density) { maxWidth.toPx() }
        val boxHeightPx = with(density) { maxHeight.toPx() }
        // 屏幕密度值，用于在协程内将 dp 转换为 px（协程内无法直接调用 Dp.toPx()）
        val densityValue = density.density

        // ============================================================
        // 3. 云朵生成协程
        //    - 每隔 intervalMs 毫秒尝试生成一张新云朵
        //    - 达到 maxClouds 上限时跳过本轮
        //    - 生成参数（尺寸、位置、速度等）全部随机化
        // ============================================================

        LaunchedEffect(intervalMs, maxClouds) {
            while (true) {
                delay(intervalMs)
                // 数量达上限则跳过本轮生成
                if (clouds.size >= maxClouds) continue

                // 随机选择云朵图片（bg_cloud_1/2/3）
                val imageRes = cloudImages[Random.nextInt(cloudImages.size)]

                // 随机云朵尺寸（dp）
                val cloudSize = Random.nextInt(minSize, maxSize + 1)
                // 将 dp 转换为像素，供位置计算使用
                val cloudSizePx = cloudSize * densityValue

                // 云朵在顶部区域内随机垂直位置（像素）
                val topBoundPx = boxHeightPx * cloudAreaTopRatio
                val bottomBoundPx = boxHeightPx * cloudAreaBottomRatio
                val startY = Random.nextFloat() * (bottomBoundPx - topBoundPx) + topBoundPx

                // 云朵水平起始位置与漂移方向，根据 direction 模式决定
                val driftDirection: Int
                val startX: Float
                when (direction) {
                    // 从左侧外进入，向右漂移
                    CloudDirection.LEFT_TO_RIGHT -> {
                        driftDirection = 1
                        startX = -(cloudSizePx * 0.5f) // 起始位置在屏幕左侧外（刚好不可见）
                    }
                    // 从右侧外进入，向左漂移
                    CloudDirection.RIGHT_TO_LEFT -> {
                        driftDirection = -1
                        startX = boxWidthPx + (cloudSizePx * 0.5f) // 起始位置在屏幕右侧外
                    }
                    // 屏幕内随机位置，不漂移
                    CloudDirection.STATIC -> {
                        driftDirection = 0
                        startX = Random.nextFloat() * boxWidthPx // 屏幕内随机水平位置
                    }
                }

                // sizeFactor：大云朵=0，小云朵=1（用于景深效果计算）
                // 大云朵（近景）更不透明、移动更慢；小云朵（远景）更透明、移动更快
                val sizeFactor = 1f - (cloudSize - minSize).toFloat() / (maxSize - minSize).toFloat()

                // 漂移速度：大云朵（sizeFactor 小）速度更慢，营造景深感
                // 乘以 densityValue 转换为像素/秒
                val driftSpeed = (minDriftSpeed + (maxDriftSpeed - minDriftSpeed) * (0.3f + sizeFactor * 0.7f)) * densityValue

                // 上下浮动参数：静止模式幅度更大（4-10dp），漂移模式幅度更小（2-6dp）
                val bobAmplitude = if (direction == CloudDirection.STATIC) {
                    Random.nextFloat() * 6f + 4f // 静止时浮动更明显，增强视觉存在感
                } else {
                    Random.nextFloat() * 4f + 2f // 漂移时浮动更轻柔，不喧宾夺主
                }
                // 浮动速度（弧度/秒），控制正弦波频率
                val bobSpeed = Random.nextFloat() * 0.5f + 0.3f

                // 透明度：小云朵（远景）0.35，大云朵（近景）0.8，再乘以 baseAlpha 系数
                val alphaValue = (0.35f + sizeFactor * 0.45f) * baseAlpha

                // 淡入时间随机化，让每朵云出现时机略有不同
                val fadeInDuration = Random.nextInt(1500, 3000)

                val id = nextId
                nextId++

                // 将新云朵数据添加到列表，触发 Compose 渲染
                clouds.add(
                    CloudData(
                        id = id, imageRes = imageRes, startX = startX, startY = startY,
                        size = cloudSize, driftDirection = driftDirection, driftSpeed = driftSpeed,
                        bobAmplitude = bobAmplitude, bobSpeed = bobSpeed, alphaValue = alphaValue,
                        fadeInDuration = fadeInDuration, isStatic = direction == CloudDirection.STATIC,
                    )
                )
            }
        }

        // ============================================================
        // 4. 渲染所有活跃云朵
        //    - 每张云朵通过 key(data.id) 确保独立的 Compose 状态
        //    - 位置通过 Animatable + graphicsLayer 在 draw 阶段更新
        //    - 父组件重组不影响云朵动画（动画完全隔离在协程中）
        // ============================================================

        clouds.forEach { data ->
            // key(data.id) 确保 Compose 根据唯一 ID 跟踪每张云朵
            // 云朵移除后自动回收其 Compose 状态
            key(data.id) {

                // --- 动画状态 ---

                // 透明度动画控制器（淡入/淡出）
                val alpha = remember { Animatable(0f) }

                // 控制云朵是否显示（true=正常显示，false=触发淡出后移除）
                var show by remember { mutableStateOf(true) }

                // 云朵开始动画的时间戳（毫秒），0 表示尚未开始
                val startTime = remember { mutableStateOf(0L) }

                // 云朵尺寸（像素），预计算避免重复转换
                val cloudSizePx = data.size * densityValue

                // 动画位置状态（在协程中通过 withFrameNanos 更新，不在 composition 中计算）
                // offsetX/offsetY 通过 graphicsLayer 读取，仅触发 draw pass
                val offsetX = remember { Animatable(0f) }
                val offsetY = remember { Animatable(0f) }

                // --- 延迟开始 + 淡入动画 ---
                // 每张云朵随机延迟 0-1 秒后开始淡入，错开出现时间更自然

                LaunchedEffect(Unit) {
                    val delayStart = Random.nextLong(0, 1000)
                    if (delayStart > 0) delay(delayStart)
                    // 记录开始时间，触发后续漂移/浮动动画
                    startTime.value = System.currentTimeMillis()
                    // 淡入动画：从透明过渡到目标透明度
                    alpha.animateTo(data.alphaValue, animationSpec = tween(data.fadeInDuration))
                }

                // --- 漂移/浮动动画 ---

                if (!data.isStatic) {
                    // ========== 漂移模式 ==========
                    // 云朵从屏幕一侧进入，线性漂移到另一侧
                    // Y 轴同时做正弦浮动（模拟真实云朵的轻微上下飘动）

                    LaunchedEffect(startTime.value) {
                        if (startTime.value == 0L) return@LaunchedEffect

                        // 设置起始位置
                        offsetX.snapTo(data.startX)
                        offsetY.snapTo(data.startY)

                        // 计算总漂移距离和持续时间
                        // 总距离 = 屏幕宽度 + 云朵宽度 * 2（确保完全飞出屏幕）
                        val totalDriftDistance = boxWidthPx + cloudSizePx * 2
                        val driftDuration = ((totalDriftDistance / data.driftSpeed) * 1000f).toInt().coerceAtLeast(3000)

                        // 帧驱动循环：使用 withFrameNanos 与系统帧回调精确同步
                        // 相比 delay(16) 更精准，不会因 recomposition 延迟导致动画跳帧
                        var elapsedMs = 0L
                        var lastFrameNanos = 0L

                        while (true) {
                            // 等待下一帧，获取精确的时间戳（纳秒）
                            val frameNanos = withFrameNanos { it }
                            if (lastFrameNanos == 0L) lastFrameNanos = frameNanos
                            // 计算帧间时间差（毫秒）
                            val deltaMs = (frameNanos - lastFrameNanos) / 1_000_000f
                            lastFrameNanos = frameNanos
                            elapsedMs += deltaMs.toLong()

                            // X 位置：线性漂移（从 startX 匀速移动到另一侧）
                            val driftProgress = elapsedMs.toFloat() / driftDuration
                            val currentX = data.startX + driftProgress * totalDriftDistance * data.driftDirection
                            offsetX.snapTo(currentX)

                            // Y 位置：正弦浮动（在基础 Y 位置上做上下摆动）
                            // sin 函数产生平滑的周期性运动
                            val elapsed = elapsedMs / 1000f
                            val bobY = data.startY + sin(elapsed * data.bobSpeed) * data.bobAmplitude * densityValue
                            offsetY.snapTo(bobY)

                            // 漂移完成（进度 >= 100%），触发淡出移除
                            if (driftProgress >= 1f) {
                                show = false
                                break
                            }
                        }
                    }
                } else {
                    // ========== 静止模式 ==========
                    // 云朵在固定位置生成，仅做轻微上下浮动
                    // 存活一段时间后自动淡出移除，防止无限累积

                    LaunchedEffect(startTime.value) {
                        if (startTime.value == 0L) return@LaunchedEffect

                        // 设置固定位置
                        offsetX.snapTo(data.startX)
                        offsetY.snapTo(data.startY)

                        // 静止云朵存活 25-45 秒后自动移除
                        val staticLifetime = Random.nextLong(25000, 45000)
                        var elapsedMs = 0L
                        var lastFrameNanos = 0L

                        // 帧驱动浮动循环：仅 Y 轴做正弦浮动，X 轴不动
                        while (elapsedMs < staticLifetime) {
                            val frameNanos = withFrameNanos { it }
                            if (lastFrameNanos == 0L) lastFrameNanos = frameNanos
                            val deltaMs = (frameNanos - lastFrameNanos) / 1_000_000f
                            lastFrameNanos = frameNanos
                            elapsedMs += deltaMs.toLong()

                            // Y 位置：正弦浮动
                            val elapsed = elapsedMs / 1000f
                            val bobY = data.startY + sin(elapsed * data.bobSpeed) * data.bobAmplitude * densityValue
                            offsetY.snapTo(bobY)
                        }

                        // 存活时间结束，触发淡出移除
                        show = false
                    }
                }

                // --- 淡出动画 ---
                // show 变为 false 时，执行 1.5 秒淡出动画，然后从列表移除

                LaunchedEffect(show) {
                    if (!show) {
                        alpha.animateTo(0f, animationSpec = tween(1500))
                        clouds.remove(data) // 从活跃列表移除，触发 Compose 回收
                    }
                }

                // --- 渲染云朵图片（性能优化版） ---
                //
                // 【关键性能设计】
                // 所有动画状态（offsetX, offsetY, alpha）的读取全部放在 graphicsLayer
                // lambda 内部，graphicsLayer 在 draw 阶段执行，仅触发 re-draw，
                // 不会触发 recomposition。这避免了协程中 snapTo() 每帧调用导致的
                // per-frame recomposition，从而在面板切换等父组件重组场景下不会掉帧。
                //
                // 云朵始终渲染（不根据位置做条件渲染），由 graphicsLayer 控制位置和
                // 透明度。当 alpha=0 或云朵在屏幕外时，draw 开销极低可忽略。

                Image(
                    painter = painterResource(id = data.imageRes),
                    contentDescription = "云朵",
                    modifier = Modifier
                        .size(data.size.dp)
                        .graphicsLayer {
                            // 所有动画值的读取都在 draw 阶段，不触发 composition
                            translationX = offsetX.value
                            translationY = offsetY.value
                            this.alpha = alpha.value
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
