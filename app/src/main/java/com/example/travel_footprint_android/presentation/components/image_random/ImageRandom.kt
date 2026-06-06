/**
 * 随机散布可拖拽图片组件
 * 
 * 用途：
 * - 在容器中生成一个随机位置、随机尺寸、随机角度的可拖拽图片
 * - 用于营造画面上的随机散布贴纸/涂鸦效果（如旅程回忆面板中的装饰元素）
 * 
 * 功能：
 * 1. 随机初始化：随机生成偏移位置、图片尺寸、旋转角度、存活时长
 * 2. 拖拽手势：支持 detectDragGestures 拖拽移动，边界受 containerWidth/Height 约束
 * 3. 按压缩放动画：按住时图片放大 pressScale（默认20dp），松开恢复，200ms 缓动过渡
 * 4. 旋转模式(isChaos)：按住拖拽时持续旋转（rotationSpeed 控制速度），旋转与拖拽顺序因 isChaos 而异
 * 5. 自动消失：到达存活时长后自动调用 onRemove 移除自身
 * 6. 点击移除：点击图片也可触发 onRemove 移除
 * 7. 默认图标：img=0 时通过 getRandomScrawlDrawable() 随机选取 ic_scrawl 系列图片
 * 
 * 关联组件：
 * - getRandomScrawlDrawable()（ImageUtils.kt）：通过反射自动扫描 R.drawable 中以 "ic_scrawl" 开头的资源，随机返回一个资源 ID
 * 
 * 实现逻辑：
 * - 使用 remember 保存随机生成的初始参数（位置、尺寸、角度、存活时长），保证重组时参数不变
 * - animateFloatAsState 控制按压缩放动画（200ms tween），目标值根据 isPress 切换
 * - Animatable 管理旋转角度状态，LaunchedEffect 在按压时每 16ms (~60fps) 更新一次角度实现持续旋转
 * - Box 外层容器：offset(IntOffset) 控制全局位置，size 固定为 maxBoxSize 预留缩放空间
 * - Image 内层：align(BottomEnd) 使图片从右下角锚定，缩放时视觉中心稳定
 * - dragModifier：isChaos=true 时 rotate 在 pointerInput 之前（先旋转再拖拽），false 时反之（先拖拽再旋转）
 * - 边界约束：拖拽偏移量累加后 coerceIn(0, 容器尺寸 - box尺寸) 防止越界
 * 
 * @param img 图片资源 ID，0 表示随机选取 scrawl 系列图片
 * @param minOffsetX/maxOffsetX 水平偏移范围（dp）
 * @param minOffsetY/maxOffsetY 垂直偏移范围（dp）
 * @param minSize/maxSize 图片尺寸范围（dp）
 * @param minAngle/maxAngle 初始旋转角度范围（度）
 * @param alpha 初始透明度
 * @param minExistenceTime/maxExistenceTime 存活时长范围（毫秒），均为 0 表示不自动消失
 * @param onRemove 移除回调（点击或超时后触发）
 * @param isChaos 是否启用混沌旋转模式（按住拖拽时持续旋转）
 * @param pressScale 按压时放大的像素量（dp）
 * @param rotationSpeed 旋转速度（度/秒）
 * @param containerWidth/containerHeight 容器尺寸（dp），用于边界约束
 * @param clickEnabled 是否启用点击移除效果，false 时禁用点击
 * @param pressEnabled 是否启用按压效果（缩放+旋转），false 时禁用按压缩放和旋转
 */
package com.example.travel_footprint_android.presentation.components.image_random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.image_random.utils.getRandomScrawlDrawable
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

// 随机散布可拖拽图片：随机位置/尺寸/角度，支持拖拽、旋转、缩放、自动消失
@Composable
fun ImageRandom(
    img: Int = 0,
    minOffsetX: Int = 0,
    maxOffsetX: Int = 0,
    minOffsetY: Int = 0,
    maxOffsetY: Int = 0,
    minSize: Int = 20,
    maxSize: Int = 40,
    minAngle: Int = 0,
    maxAngle: Int = 360,
    alpha: Float = 0f,
    minExistenceTime: Int = 10000,
    maxExistenceTime: Int = 20000,
    onRemove: (() -> Unit)? = null,
    isChaos: Boolean = false,
    pressScale: Float = 20f,
    rotationSpeed: Float = 30f,
    containerWidth: Dp = Dp.Infinity,
    containerHeight: Dp = Dp.Infinity,
    clickEnabled: Boolean = true,
    pressEnabled: Boolean = true,
) {
    // 按压状态：决定缩放动画和旋转动画的启停
    var isPress by remember { mutableStateOf(false) }

    // 图片资源：img=0 时通过反射随机选取 ic_scrawl 系列图片，否则使用指定资源
    val drawableResId = remember {
        if (img == 0) {
            getRandomScrawlDrawable()
        } else {
            img
        }
    }

    // 随机水平偏移量（dp），在 [min, max] 范围内随机取整
    val offsetX = remember {
        val low = minOf(minOffsetX, maxOffsetX)
        val high = maxOf(minOffsetX, maxOffsetX)
        if (low == high) low else Random.nextInt(low, high + 1)
    }
    // 随机垂直偏移量（dp），在 [min, max] 范围内随机取整
    val offsetY = remember {
        val low = minOf(minOffsetY, maxOffsetY)
        val high = maxOf(minOffsetY, maxOffsetY)
        if (low == high) low else Random.nextInt(low, high + 1)
    }
    // 随机图片尺寸（dp），在 [min, max] 范围内随机取整
    val imgSize = remember {
        val low = minOf(minSize, maxSize)
        val high = maxOf(minSize, maxSize)
        if (low == high) low else Random.nextInt(low, high + 1)
    }
    // 按压缩放动画：按住时放大 pressScale，松开恢复原尺寸，200ms 缓动
    // pressEnabled 为 false 时始终使用原始尺寸，不触发放大动画
    val aniImgSize by animateFloatAsState(
        targetValue = if (pressEnabled && isPress) imgSize.toFloat() + pressScale else imgSize.toFloat(),
        animationSpec = tween(durationMillis = 200),
    )

    // 旋转角度 Animatable：随机初始角度，运行时通过 snapTo 持续累加实现旋转
    val angle = remember {
        val low = minOf(minAngle, maxAngle)
        val high = maxOf(minAngle, maxAngle)
        val initial = if (low == high) low.toFloat() else Random.nextInt(low, high + 1).toFloat()
        Animatable(initial)
    }

    // 随机存活时长（毫秒）：min=0 且 max=0 时返回 -1 表示永不自动消失
    val existenceMs = remember {
        if (minExistenceTime == 0 && maxExistenceTime == 0) {
            -1L
        } else {
            val low = minOf(minExistenceTime, maxExistenceTime)
            val high = maxOf(minExistenceTime, maxExistenceTime)
            Random.nextLong(low.toLong(), (high + 1).toLong())
        }
    }

    // 将 dp 初始偏移转为像素，用于拖拽时的实时偏移量
    val density = LocalDensity.current
    val initialMarginPxX = with(density) { offsetX.dp.toPx() }
    val initialMarginPxY = with(density) { offsetY.dp.toPx() }
    var offsetRealX by remember { mutableStateOf(initialMarginPxX) }
    var offsetRealY by remember { mutableStateOf(initialMarginPxY) }

    // 容器尺寸转像素：Dp.Infinity 时设为 Float.MAX_VALUE 表示无边界限制
    val containerWidthPx = if (containerWidth == Dp.Infinity) Float.MAX_VALUE else with(density) { containerWidth.toPx() }
    val containerHeightPx = if (containerHeight == Dp.Infinity) Float.MAX_VALUE else with(density) { containerHeight.toPx() }

    // 固定 Box 容器尺寸为最大可能尺寸（原尺寸 + 按压放大），确保缩放时不被裁剪
    val maxBoxSize = imgSize + pressScale

    // 旋转动画循环：按压时每 16ms 更新一次角度（~60fps），rotationSpeed 度/秒
    LaunchedEffect(isPress, rotationSpeed) {
        if (!isPress) return@LaunchedEffect
        while (true) {
            delay(16) // ~60fps
            angle.snapTo(angle.value + rotationSpeed.toFloat() * 0.016f) // 每帧旋转 = 速度 × 0.016秒
        }
    }

    // 自动消失定时器：到达存活时长后触发 onRemove
    if (existenceMs >= 0L && onRemove != null) {
        LaunchedEffect(Unit) {
            delay(existenceMs)
            onRemove()
        }
    }

    // 拖拽修饰符：isChaos 控制旋转与拖拽的叠加顺序
    // chaos 模式：先 rotate 再 pointerInput（旋转坐标系中拖拽）
    // 普通模式：先 pointerInput 再 rotate（拖拽后旋转视觉）
    // pressEnabled 为 false 时拖拽不触发 isPress，从而禁用按压缩放和旋转
    val onDragStart: () -> Unit = { if (pressEnabled) isPress = true }
    val onDragEnd: () -> Unit = { isPress = false }
    val dragModifier = if (isChaos) {
        Modifier
            .rotate(angle.value)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val boxSizePx = with(density) { maxBoxSize.dp.toPx() }
                        // 累加偏移量并约束在容器边界内
                        offsetRealX = (offsetRealX + dragAmount.x).coerceIn(0f, containerWidthPx - boxSizePx)
                        offsetRealY = (offsetRealY + dragAmount.y).coerceIn(0f, containerHeightPx - boxSizePx)
                    }
                )
            }
    } else {
        Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val boxSizePx = with(density) { maxBoxSize.dp.toPx() }
                        offsetRealX = (offsetRealX + dragAmount.x).coerceIn(0f, containerWidthPx - boxSizePx)
                        offsetRealY = (offsetRealY + dragAmount.y).coerceIn(0f, containerHeightPx - boxSizePx)
                    }
                )
            }
            .rotate(angle.value)
    }

    // 外层 Box：固定 maxBoxSize 尺寸 + 偏移定位 + 透明度，确保缩放/旋转空间充足
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetRealX.roundToInt(), offsetRealY.roundToInt()) }
            .size(width = maxBoxSize.dp, height = maxBoxSize.dp)
            .alpha(alpha)
    ) {
        // 内层 Image：从右下角锚定，缩放时视觉稳定，支持点击移除 + 拖拽/旋转
        // clickEnabled 为 false 时禁用点击移除效果
        val imageClickableModifier = if (clickEnabled && onRemove != null) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 无点击涟漪
                onClick = { onRemove() }
            )
        } else {
            Modifier
        }
        Image(
            painter = painterResource(id = drawableResId),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd) // 右下角锚定，缩放原点稳定
                .size(width = aniImgSize.dp, height = aniImgSize.dp) // 按压缩放动画
                .then(imageClickableModifier)
                .then(dragModifier), // 叠加拖拽 + 旋转修饰符
            contentScale = ContentScale.Fit,
        )
    }
}
