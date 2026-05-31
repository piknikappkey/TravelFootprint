package com.example.travel_footprint_android.presentation2.components.bg_animotion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.random.Random

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    isRaining: Boolean = true,
    dropCount: Int = 150,
    dropColor: Color = Color(0xFF6CA6CD).copy(alpha = 0.6f),
    dropLength: Float = 15f,
    dropWidth: Float = 2f,
    windStrength: Float = 2f
) {
    if (!isRaining) return

    val raindrops = remember(dropCount) {
        List(dropCount) {
            Raindrop(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.005f + Random.nextFloat() * 0.015f,
                length = dropLength * (0.7f + Random.nextFloat() * 0.6f),
                headSize = dropWidth * (1.5f + Random.nextFloat() * 2f),
                tailLength = dropLength * (0.8f + Random.nextFloat() * 1.2f)
            )
        }
    }.toMutableStateList()

    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { _ ->
                frame++
                raindrops.forEach { drop ->
                    drop.y += drop.speed
                    if (drop.y > 1f) {
                        drop.y = 0f
                        drop.x = Random.nextFloat()
                    }
                }
            }
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        frame

        raindrops.forEach { drop ->
            val angle = (windStrength / 30f) * 0.5f
            val dx = angle * drop.tailLength
            val dy = drop.tailLength

            // 头部（较粗较实）在下，尾部（较细较虚）在上
            // 所以头部坐标是下方终点，尾部坐标是上方起点
            val tailX = drop.x * size.width      // 尾部（上端，细虚）
            val tailY = drop.y * size.height
            val headX = tailX + dx               // 头部（下端，粗实）
            val headY = tailY + dy

            // ===== 核心：分段绘制渐变雨滴（从上到下：细虚 → 粗实）=====
            val segments = 6

            for (i in 0 until segments) {
                val t = i / segments.toFloat()          // 0.0 ~ 0.83（从尾部到头部）
                // 尾部细虚（t=0），头部粗实（t=1）
                val alpha = 0.9f * (0.15f + t * 0.85f)   // 0.135 → 0.9
                val strokeWidth = drop.headSize * (0.3f + t * 0.7f)  // 30% → 100%

                // 从尾部到头部均匀插值
                val startX = tailX + dx * t
                val startY = tailY + dy * t
                val endX = tailX + dx * (t + 1f / segments)
                val endY = tailY + dy * (t + 1f / segments)

                val start = Offset(startX, startY)
                val end = Offset(endX, endY)

                drawLine(
                    color = dropColor.copy(alpha = alpha),
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = if (i == segments - 1) StrokeCap.Round else StrokeCap.Butt
                )
            }

            // 头部光晕（在下端，模拟水滴表面的光反射）
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = drop.headSize * 1.5f,
                center = Offset(headX, headY)
            )
        }
    }
}

data class Raindrop(
    var x: Float,
    var y: Float,
    val speed: Float,
    val length: Float,
    val headSize: Float,    // 头部粗细
    val tailLength: Float   // 尾巴长度
)