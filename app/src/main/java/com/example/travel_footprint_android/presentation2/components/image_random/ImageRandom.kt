package com.example.travel_footprint_android.presentation2.components.image_random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

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
) {
    // 当前是否处于拖动中
    var isPress by remember { mutableStateOf(false) }

    val drawableResId = remember {
        if (img == 0) {
            val fields = R.drawable::class.java.declaredFields
            val scrawlFields = fields.filter { it.name.startsWith("ic_scrawl") }
            if (scrawlFields.isNotEmpty()) {
                scrawlFields[Random.nextInt(scrawlFields.size)].getInt(null)
            } else {
                R.drawable.ic_scrawl0
            }
        } else {
            img
        }
    }

    val offsetX = remember {
        val low = minOf(minOffsetX, maxOffsetX)
        val high = maxOf(minOffsetX, maxOffsetX)
        if (low == high) low else Random.nextInt(low, high + 1)
    }
    val offsetY = remember {
        val low = minOf(minOffsetY, maxOffsetY)
        val high = maxOf(minOffsetY, maxOffsetY)
        if (low == high) low else Random.nextInt(low, high + 1)
    }
    val imgSize = remember {
        val low = minOf(minSize, maxSize)
        val high = maxOf(minSize, maxSize)
        if (low == high) low else Random.nextInt(low, high + 1)
    }
    val aniImgSize by animateFloatAsState(
        targetValue = if (isPress) imgSize.toFloat() + 20f else imgSize.toFloat(),
        animationSpec = tween(durationMillis = 200),
    )

    val angle = remember {
        val low = minOf(minAngle, maxAngle)
        val high = maxOf(minAngle, maxAngle)
        val initial = if (low == high) low.toFloat() else Random.nextInt(low, high + 1).toFloat()
        Animatable(initial)
    }

    val existenceMs = remember {
        if (minExistenceTime == 0 && maxExistenceTime == 0) {
            -1L
        } else {
            val low = minOf(minExistenceTime, maxExistenceTime)
            val high = maxOf(minExistenceTime, maxExistenceTime)
            Random.nextLong(low.toLong(), (high + 1).toLong())
        }
    }

    // 用于支持拖动功能
    val density = LocalDensity.current
    val initialMarginPxX = with(density) { offsetX.dp.toPx() }
    val initialMarginPxY = with(density) { offsetY.dp.toPx() }
    var offsetRealX by remember { mutableStateOf(initialMarginPxX) }
    var offsetRealY by remember { mutableStateOf(initialMarginPxY) }

    val rotationSpeed = 30f // 度/秒

    LaunchedEffect(isPress) {
        if (!isPress) return@LaunchedEffect
        while (true) {
            delay(16)
            angle.snapTo(angle.value + rotationSpeed * 0.016f)
        }
    }

    if (existenceMs >= 0L && onRemove != null) {
        LaunchedEffect(Unit) {
            delay(existenceMs)
            onRemove()
        }
    }

    val dragModifier = if (isChaos) {
        Modifier
            .rotate(angle.value)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isPress = true
                    },
                    onDragEnd = {
                        isPress = false
                    },
                    onDragCancel = {
                        isPress = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetRealX += dragAmount.x
                        offsetRealY += dragAmount.y
                    }
                )
            }
    } else {
        Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isPress = true
                    },
                    onDragEnd = {
                        isPress = false
                    },
                    onDragCancel = {
                        isPress = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetRealX += dragAmount.x
                        offsetRealY += dragAmount.y
                    }
                )
            }
            .rotate(angle.value)
    }

    val imgModifier = Modifier
        .offset { IntOffset(offsetRealX.roundToInt(), offsetRealY.roundToInt()) }
        .size(width = aniImgSize.dp, height = aniImgSize.dp)
        .alpha(alpha)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (onRemove != null) {
                    onRemove()
                }
            })
        .then(dragModifier)

    Image(
        painter = painterResource(id = drawableResId),
        contentDescription = null,
        modifier = imgModifier,
        contentScale = ContentScale.Fit,
    )
}
