package com.example.travel_footprint_android.presentation.components.bg_animotion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SnowEffect(
    modifier: Modifier = Modifier,
    isSnowing: Boolean = true,
    flakeCount: Int = 30,
    windStrength: Float = 1f,
    flakeColor: Color = Color(0xFF3382E3)
) {
    if (!isSnowing) return

    val snowflakes = remember(flakeCount) {
        List(flakeCount) {
            RealSnowflake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.0008f + Random.nextFloat() * 0.006f,
                baseSize = 4f + Random.nextFloat() * 8f,
                shape = SnowflakeShape.values()[Random.nextInt(SnowflakeShape.values().size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 1.5f,
                swingAmp = Random.nextFloat() * 20f,
                swingFreq = Random.nextFloat() * 0.04f,
                phase = Random.nextFloat() * 360f,
                opacity = 0.5f + Random.nextFloat() * 0.4f
            )
        }
    }

    // 用 InfiniteTransition 驱动时间，运行在渲染管线中，不受重组阻塞影响
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "time"
    )

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { _ ->
                snowflakes.forEach { flake ->
                    flake.y += flake.speed
                    if (flake.y > 1f) {
                        flake.y = 0f
                        flake.x = Random.nextFloat()
                    }
                    flake.x += (windStrength * 0.0015f) +
                            sin(time * flake.swingFreq + flake.phase).toFloat() * 0.0015f
                    if (flake.x > 1f) flake.x = 0f
                    if (flake.x < 0f) flake.x = 1f

                    flake.rotation += flake.rotationSpeed
                    if (flake.rotation > 360f) flake.rotation -= 360f
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        time // 确保 Canvas 依赖 time 状态，每帧重绘
        snowflakes.forEach { flake ->
            drawRealSnowflake(
                x = flake.x * size.width,
                y = flake.y * size.height,
                size = flake.baseSize,
                shape = flake.shape,
                rotation = flake.rotation,
                opacity = flake.opacity,
                color = flakeColor
            )
        }
    }
}

// 雪花形状枚举
enum class SnowflakeShape {
    STAR,      // 星形（经典六角星）
    DENDRITE,  // 树枝状（复杂精致）
    NEEDLE,    // 针状（细长形）
    PLATE,     // 片状（六边形薄片）
    SECTOR,    // 扇形（有分叉）
    SIMPLE     // 简单六角形
}

data class RealSnowflake(
    var x: Float,
    var y: Float,
    val speed: Float,
    val baseSize: Float,
    val shape: SnowflakeShape,
    var rotation: Float,
    val rotationSpeed: Float,
    val swingAmp: Float,
    val swingFreq: Float,
    val phase: Float,
    val opacity: Float
)

private fun DrawScope.drawRealSnowflake(
    x: Float,
    y: Float,
    size: Float,
    shape: SnowflakeShape,
    rotation: Float,
    opacity: Float,
    color: Color
) {
    rotate(degrees = rotation, pivot = Offset(x, y)) {
        val whiteColor = Color(0xFF3382E3).copy(alpha = opacity * 0.8f)
        val brightColor = Color(0xFF3382E3).copy(alpha = opacity)
        val softColor = Color(0xFFE8F0FF).copy(alpha = opacity * 0.6f)

        when (shape) {
            SnowflakeShape.STAR -> drawStarSnowflake(x, y, size, whiteColor, brightColor)
            SnowflakeShape.DENDRITE -> drawDendriteSnowflake(x, y, size, whiteColor, softColor)
            SnowflakeShape.NEEDLE -> drawNeedleSnowflake(x, y, size, whiteColor)
            SnowflakeShape.PLATE -> drawPlateSnowflake(x, y, size, whiteColor, softColor)
            SnowflakeShape.SECTOR -> drawSectorSnowflake(x, y, size, whiteColor, brightColor)
            SnowflakeShape.SIMPLE -> drawSimpleSnowflake(x, y, size, whiteColor)
        }

        // 添加微光晕
        drawCircle(
            color = Color.White.copy(alpha = opacity * 0.15f),
            radius = size * 0.8f,
            center = Offset(x, y)
        )
    }
}

// 1. 星形雪花（经典六角星）
private fun DrawScope.drawStarSnowflake(x: Float, y: Float, size: Float, mainColor: Color, brightColor: Color) {
    val arms = 6
    val angleStep = 360f / arms

    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep).toDouble())
        val dx = (cos(angle) * size).toFloat()
        val dy = (sin(angle) * size).toFloat()

        // 主臂
        drawLine(
            color = mainColor,
            start = Offset(x, y),
            end = Offset(x + dx, y + dy),
            strokeWidth = size * 0.25f,
            cap = StrokeCap.Round
        )

        // 分支1（左侧）
        val branchAngle1 = angle + Math.toRadians(30.0)
        val branchDx1 = (cos(branchAngle1) * size * 0.5f).toFloat()
        val branchDy1 = (sin(branchAngle1) * size * 0.5f).toFloat()
        drawLine(
            color = brightColor,
            start = Offset(x + dx * 0.6f, y + dy * 0.6f),
            end = Offset(x + dx * 0.6f + branchDx1, y + dy * 0.6f + branchDy1),
            strokeWidth = size * 0.15f,
            cap = StrokeCap.Round
        )

        // 分支2（右侧）
        val branchAngle2 = angle - Math.toRadians(30.0)
        val branchDx2 = (cos(branchAngle2) * size * 0.5f).toFloat()
        val branchDy2 = (sin(branchAngle2) * size * 0.5f).toFloat()
        drawLine(
            color = brightColor,
            start = Offset(x + dx * 0.6f, y + dy * 0.6f),
            end = Offset(x + dx * 0.6f + branchDx2, y + dy * 0.6f + branchDy2),
            strokeWidth = size * 0.15f,
            cap = StrokeCap.Round
        )
    }

    // 中心点
    drawCircle(color = mainColor, radius = size * 0.2f, center = Offset(x, y))
}

// 2. 树枝状雪花（最精致）
private fun DrawScope.drawDendriteSnowflake(x: Float, y: Float, size: Float, mainColor: Color, softColor: Color) {
    val arms = 6
    val angleStep = 360f / arms

    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep).toDouble())
        val dx = (cos(angle) * size).toFloat()
        val dy = (sin(angle) * size).toFloat()

        // 主臂（渐变粗细）
        for (j in 1..4) {
            val t = j / 4f
            val segmentStartX = x + dx * (j - 1) * 0.25f
            val segmentStartY = y + dy * (j - 1) * 0.25f
            val segmentEndX = x + dx * j * 0.25f
            val segmentEndY = y + dy * j * 0.25f
            drawLine(
                color = mainColor.copy(alpha = mainColor.alpha * (1f - t * 0.3f)),
                start = Offset(segmentStartX, segmentStartY),
                end = Offset(segmentEndX, segmentEndY),
                strokeWidth = size * 0.2f * (1f - t * 0.5f),
                cap = StrokeCap.Round
            )
        }

        // 复杂分支
        val branchAngles = listOf(20.0, -20.0, 40.0, -40.0)
        branchAngles.forEach { branchOffset ->
            val branchAngle = angle + Math.toRadians(branchOffset)
            val branchDx = (cos(branchAngle) * size * 0.6f).toFloat()
            val branchDy = (sin(branchAngle) * size * 0.6f).toFloat()
            drawLine(
                color = softColor,
                start = Offset(x + dx * 0.45f, y + dy * 0.45f),
                end = Offset(x + dx * 0.45f + branchDx, y + dy * 0.45f + branchDy),
                strokeWidth = size * 0.1f,
                cap = StrokeCap.Round
            )
        }
    }

    // 中心装饰
    drawCircle(color = mainColor, radius = size * 0.15f, center = Offset(x, y))
}

// 3. 针状雪花（细长形）
private fun DrawScope.drawNeedleSnowflake(x: Float, y: Float, size: Float, mainColor: Color) {
    val arms = 6
    val angleStep = 360f / arms

    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep).toDouble())
        val dx = (cos(angle) * size).toFloat()
        val dy = (sin(angle) * size).toFloat()

        // 细长臂
        drawLine(
            color = mainColor,
            start = Offset(x, y),
            end = Offset(x + dx, y + dy),
            strokeWidth = size * 0.12f,
            cap = StrokeCap.Round
        )

        // 末端小球
        drawCircle(
            color = mainColor,
            radius = size * 0.1f,
            center = Offset(x + dx, y + dy)
        )
    }
}

// 4. 片状雪花（六边形）
private fun DrawScope.drawPlateSnowflake(x: Float, y: Float, size: Float, mainColor: Color, softColor: Color) {
    val arms = 6
    val angleStep = 360f / arms
    val points = mutableListOf<Offset>()

    // 绘制六边形外框
    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep - 30).toDouble())
        val px = x + (cos(angle) * size * 0.8f).toFloat()
        val py = y + (sin(angle) * size * 0.8f).toFloat()
        points.add(Offset(px, py))
    }

    for (i in 0 until arms) {
        val start = points[i]
        val end = points[(i + 1) % arms]
        drawLine(
            color = mainColor,
            start = start,
            end = end,
            strokeWidth = size * 0.1f
        )
    }

    // 内部射线
    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep).toDouble())
        val dx = (cos(angle) * size * 0.7f).toFloat()
        val dy = (sin(angle) * size * 0.7f).toFloat()
        drawLine(
            color = softColor,
            start = Offset(x, y),
            end = Offset(x + dx, y + dy),
            strokeWidth = size * 0.12f
        )
    }
}

// 5. 扇形雪花
private fun DrawScope.drawSectorSnowflake(x: Float, y: Float, size: Float, mainColor: Color, brightColor: Color) {
    val arms = 6
    val angleStep = 360f / arms

    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep).toDouble())
        val dx = (cos(angle) * size).toFloat()
        val dy = (sin(angle) * size).toFloat()

        // 主臂
        drawLine(
            color = mainColor,
            start = Offset(x, y),
            end = Offset(x + dx, y + dy),
            strokeWidth = size * 0.2f,
            cap = StrokeCap.Round
        )

        // 扇形分支（多层）
        for (branchLevel in 1..3) {
            val branchAngle = angle + Math.toRadians((branchLevel * 15f).toDouble())
            val branchDx = (cos(branchAngle) * size * 0.5f).toFloat()
            val branchDy = (sin(branchAngle) * size * 0.5f).toFloat()
            drawLine(
                color = brightColor.copy(alpha = brightColor.alpha * (1f - branchLevel * 0.2f)),
                start = Offset(x + dx * 0.5f, y + dy * 0.5f),
                end = Offset(x + dx * 0.5f + branchDx, y + dy * 0.5f + branchDy),
                strokeWidth = size * 0.1f * (1f - branchLevel * 0.2f),
                cap = StrokeCap.Round
            )
        }
    }
}

// 6. 简单六角形
private fun DrawScope.drawSimpleSnowflake(x: Float, y: Float, size: Float, mainColor: Color) {
    val arms = 6
    val angleStep = 360f / arms

    for (i in 0 until arms) {
        val angle = Math.toRadians((i * angleStep).toDouble())
        val dx = (cos(angle) * size).toFloat()
        val dy = (sin(angle) * size).toFloat()

        drawLine(
            color = mainColor,
            start = Offset(x, y),
            end = Offset(x + dx * 0.8f, y + dy * 0.8f),
            strokeWidth = size * 0.15f,
            cap = StrokeCap.Round
        )

        // 小分支
        val branchAngle = angle + Math.toRadians(30.0)
        val branchDx = (cos(branchAngle) * size * 0.35f).toFloat()
        val branchDy = (sin(branchAngle) * size * 0.35f).toFloat()
        drawLine(
            color = mainColor.copy(alpha = 0.6f),
            start = Offset(x + dx * 0.5f, y + dy * 0.5f),
            end = Offset(x + dx * 0.5f + branchDx, y + dy * 0.5f + branchDy),
            strokeWidth = size * 0.1f,
            cap = StrokeCap.Round
        )
    }
}