package com.example.travel_footprint_android.presentation2.components.image_random

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class RainImageData(
    val id: Long,
    val offsetX: Int,
    val offsetY: Int,
    val size: Int,
)

@Composable
fun ImageRain(
    modifier: Modifier = Modifier,
    intervalMs: Long = 1000L,
    fadeInMs: Int = 500,
    maxImages: Int = 10,
    size: Int = 0,
    minSize: Int = 30,
    maxSize: Int = 50,
    minExistenceTime: Int = 10000,
    maxExistenceTime: Int = 20000,
) {
    val images = remember { mutableStateListOf<RainImageData>() }
    var nextId by remember { mutableStateOf(0L) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val boxWidth = maxWidth
        val boxHeight = maxHeight

        LaunchedEffect(Unit) {
            while (true) {
                delay(intervalMs)

                if (images.size >= maxImages) continue

                val imgSize = if (size > 0) size else Random.nextInt(minSize, maxSize + 1)
                val imgSizeDp = imgSize.dp
                val maxXDp = (boxWidth - imgSizeDp).coerceAtLeast(0.dp)
                val maxYDp = (boxHeight - imgSizeDp).coerceAtLeast(0.dp)

                val offsetX = randomDpOffset(maxXDp)
                val offsetY = randomDpOffset(maxYDp)

                val id = nextId
                nextId++

                images.add(RainImageData(id = id, offsetX = offsetX, offsetY = offsetY, size = imgSize))
            }
        }

        images.forEach { data ->
            key(data.id) {
                val alpha = remember { Animatable(0f) }
                var show by remember { mutableStateOf(true) }
                LaunchedEffect(show) {
                    if(show) {
                        alpha.animateTo(1f, animationSpec = tween(fadeInMs))
                    } else {
                        alpha.animateTo(0f, animationSpec = tween(fadeInMs))
                        images.remove(data)
                    }
                }
                Box {
                    ImageRandom(
                        minOffsetX = data.offsetX,
                        maxOffsetX = data.offsetX,
                        minOffsetY = data.offsetY,
                        maxOffsetY = data.offsetY,
                        minSize = data.size,
                        maxSize = data.size,
                        alpha = alpha.value,
                        minExistenceTime = minExistenceTime,
                        maxExistenceTime = maxExistenceTime,
                        onRemove = {
                            show = false
                        },
                    )
                }
            }
        }
    }
}

private fun randomDpOffset(maxDp: Dp): Int {
    return if (maxDp > 0.dp) Random.nextInt(0, (maxDp.value.toInt()) + 1) else 0
}
