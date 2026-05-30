package com.example.travel_footprint_android.presentation2.components.image_random

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import kotlinx.coroutines.delay
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
) {
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
    val angle = remember {
        val low = minOf(minAngle, maxAngle)
        val high = maxOf(minAngle, maxAngle)
        if (low == high) low.toFloat() else Random.nextInt(low, high + 1).toFloat()
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

    if (existenceMs >= 0L && onRemove != null) {
        LaunchedEffect(Unit) {
            delay(existenceMs)
            onRemove()
        }
    }

    Image(
        painter = painterResource(id = drawableResId),
        contentDescription = null,
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(width = imgSize.dp, height = imgSize.dp)
            .rotate(angle)
            .alpha(alpha),
        contentScale = ContentScale.Fit,
    )
}
