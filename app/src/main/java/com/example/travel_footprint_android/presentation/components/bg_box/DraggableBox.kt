/**
 * DraggableBox - 可拖动容器组件
 *
 * 用途：
 * - 为任意子内容提供自由拖拽能力，子内容可在父容器范围内随意拖动
 * - 在旅程地图页的天气卡片（WeatherCard）等需要浮窗拖动的场景中使用
 *
 * 功能：
 * - 自由拖拽：通过 detectDragGestures 实现单指拖拽，实时更新偏移量
 * - 边界约束：拖拽过程中自动将子内容约束在父容器范围内，不越界
 * - 边缘吸附：松开手指后自动平滑吸附到最近的父容器边缘（支持开关和动画自定义）
 * - 尺寸自适应：通过 LaunchedEffect 监听容器和内容的尺寸变化，自动校准位置
 * - 拖拽回调：通过 onDragStart / onDragEnd / onDragCancel / onDrag 向外暴露拖拽生命周期
 * - 完全解耦：不依赖任何业务状态，仅通过 state 回调暴露位置信息
 *
 * 使用方式：
 * ```
 * DraggableBox(
 *     initialOffsetX = 10f,
 *     initialOffsetY = 10f,
 * ) {
 *     Text("可拖动的内容")
 * }
 * ```
 */
package com.example.travel_footprint_android.presentation.components.bg_box

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 可拖动容器组件
 *
 * @param modifier             外部传入的修饰符，应用于外层容器
 * @param initialOffsetX       初始 X 轴偏移量（px），默认 0f
 * @param initialOffsetY       初始 Y 轴偏移量（px），默认 0f
 * @param enableSnap           是否启用边缘吸附，默认 true
 * @param snapAnimationSpec    吸附动画规格，默认 tween(300)
 * @param shape                裁剪形状，传 null 则不裁剪，默认 RoundedCornerShape(12.dp)
 * @param onDragStart          拖拽开始回调
 * @param onDragEnd            拖拽结束回调，返回最终偏移量
 * @param onDragCancel         拖拽取消回调
 * @param onDrag               拖拽进行中回调，返回当前偏移量
 * @param onPositionChanged    位置变化回调，可选，用于外部监听当前偏移量
 * @param onOuterSizeChanged   父容器尺寸变化回调，可选，用于外部获取容器尺寸
 * @param composable           子内容
 */
@Composable
fun DraggableBox(
    modifier: Modifier = Modifier,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    enableSnap: Boolean = true,
    snapAnimationSpec: androidx.compose.animation.core.AnimationSpec<Float> = tween(300),
    shape: androidx.compose.ui.graphics.Shape? = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    onDragStart: (() -> Unit)? = null,
    onDragEnd: ((offsetX: Float, offsetY: Float) -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    onDrag: ((offsetX: Float, offsetY: Float) -> Unit)? = null,
    onPositionChanged: ((offsetX: Float, offsetY: Float) -> Unit)? = null,
    onOuterSizeChanged: ((width: Float, height: Float) -> Unit)? = null,
    composable: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // 当前偏移量（使用 Animatable 以支持吸附动画）
    val offsetX = remember { Animatable(initialOffsetX) }
    val offsetY = remember { Animatable(initialOffsetY) }

    // 当 initialOffsetX/Y 变化时重新定位
    LaunchedEffect(initialOffsetX) { offsetX.snapTo(initialOffsetX) }
    LaunchedEffect(initialOffsetY) { offsetY.snapTo(initialOffsetY) }

    // 父容器尺寸
    var outerWidth by remember { mutableFloatStateOf(0f) }
    var outerHeight by remember { mutableFloatStateOf(0f) }

    // 子内容尺寸
    var contentWidth by remember { mutableFloatStateOf(0f) }
    var contentHeight by remember { mutableFloatStateOf(0f) }

    // 外部容器：占满可用空间，用于测量父容器尺寸
    Box(
        modifier = modifier.onSizeChanged { size ->
            outerWidth = size.width.toFloat()
            outerHeight = size.height.toFloat()
            onOuterSizeChanged?.invoke(size.width.toFloat(), size.height.toFloat())
        },
    ) {
        // 可拖动的内容层
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .onSizeChanged { size ->
                    contentWidth = size.width.toFloat()
                    contentHeight = size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart?.invoke() },
                        onDragEnd = {
                            onDragEnd?.invoke(offsetX.value, offsetY.value)
                            if (enableSnap) {
                                val maxX = (outerWidth - contentWidth).coerceAtLeast(0f)
                                val maxY = (outerHeight - contentHeight).coerceAtLeast(0f)
                                // 计算到四个边缘的距离，找出最近的那条边，仅吸附到该边
                                val leftDist = offsetX.value
                                val rightDist = maxX - offsetX.value
                                val topDist = offsetY.value
                                val bottomDist = maxY - offsetY.value
                                val minDist = minOf(leftDist, rightDist, topDist, bottomDist)
                                var targetX = offsetX.value
                                var targetY = offsetY.value
                                when (minDist) {
                                    leftDist  -> targetX = 0f
                                    rightDist -> targetX = maxX
                                    topDist   -> targetY = 0f
                                    bottomDist -> targetY = maxY
                                }
                                scope.launch {
                                    offsetX.animateTo(targetX, snapAnimationSpec)
                                    offsetY.animateTo(targetY, snapAnimationSpec)
                                }
                                onDragEnd?.invoke(targetX, targetY)
                                onPositionChanged?.invoke(targetX, targetY)
                            }
                        },
                        onDragCancel = { onDragCancel?.invoke() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val maxX = (outerWidth - contentWidth).coerceAtLeast(0f)
                            val maxY = (outerHeight - contentHeight).coerceAtLeast(0f)
                            val newX = (offsetX.value + dragAmount.x).coerceIn(0f, maxX)
                            val newY = (offsetY.value + dragAmount.y).coerceIn(0f, maxY)
                            scope.launch {
                                offsetX.snapTo(newX)
                                offsetY.snapTo(newY)
                            }
                            onDrag?.invoke(newX, newY)
                            onPositionChanged?.invoke(newX, newY)
                        },
                    )
                }
                .let { modifierChain ->
                    if (shape != null) {
                        modifierChain.clip(shape)
                    } else {
                        modifierChain
                    }
                },
        ) {
            composable()
        }
    }

    // 容器或内容尺寸变化时，自动校准位置不超出边界
    LaunchedEffect(outerWidth, outerHeight, contentWidth, contentHeight) {
        if (outerWidth > 0f && outerHeight > 0f && contentWidth > 0f && contentHeight > 0f) {
            val maxX = (outerWidth - contentWidth).coerceAtLeast(0f)
            val maxY = (outerHeight - contentHeight).coerceAtLeast(0f)
            offsetX.snapTo(offsetX.value.coerceIn(0f, maxX))
            offsetY.snapTo(offsetY.value.coerceIn(0f, maxY))
        }
    }
}
