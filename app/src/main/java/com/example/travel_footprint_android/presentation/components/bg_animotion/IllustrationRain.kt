package com.example.travel_footprint_android.presentation.components.bg_animotion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

// 雨滴粒子数据类
private data class RainParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var length: Float,
    var width: Float,
    var alpha: Float,
    var angle: Float,
    var waveOffset: Float,
    var layer: Int  // 0:远层, 1:中层, 2:近层
)

// 溅射粒子数据类
private data class SplashParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var size: Float,
    var alpha: Float
)

// 对象池，避免频繁创建对象
private class ParticlePool<T>(private val factory: () -> T, private val poolSize: Int) {
    private val pool = mutableListOf<T>()
    private val active = mutableSetOf<T>()

    fun obtain(): T {
        val particle = if (pool.isNotEmpty()) pool.removeAt(pool.lastIndex) else factory()
        active.add(particle)
        return particle
    }

    fun recycle(particle: T) {
        if (active.remove(particle)) {
            pool.add(particle)
        }
    }

    fun recycleAll() {
        pool.addAll(active)
        active.clear()
    }
}

@Composable
fun IllustrationRain(
    modifier: Modifier = Modifier,
    count: Int = 150,  // 雨滴数量
    intensity: Float = 1f,  // 雨势强度 0.5-1.5
    enableSplash: Boolean = true  // 是否启用溅射效果
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // 性能优化：缓存计算结果
    val layerConfigs = remember {
        arrayOf(
            LayerConfig(0.4f, 0.6f, 1.5f, Color(0xFF7BA8E8)),  // 远层：慢，淡
            LayerConfig(0.7f, 0.9f, 2.2f, Color(0xFFAAC9FF)),  // 中层
            LayerConfig(1.0f, 1.0f, 3f, Color(0xFFC8DDFF))     // 近层：快，亮
        )
    }

    // 雨滴池 - 使用对象池优化
    val rainDrops = remember(count) {
        mutableStateListOf<RainParticle>().apply {
            repeat(count) {
                val layer = Random.nextInt(3)
                val config = layerConfigs[layer]
                add(
                    RainParticle(
                        x = Random.nextFloat() * screenWidth,
                        y = Random.nextFloat() * screenHeight,
                        speed = (Random.nextFloat() * 8f + 8f) * config.speedMultiplier * intensity,
                        length = Random.nextFloat() * 30f + 20f,
                        width = Random.nextFloat() * 2f + 1f,
                        alpha = (Random.nextFloat() * 0.4f + 0.3f) * config.alphaMultiplier,
                        angle = Random.nextFloat() * 12f - 6f,
                        waveOffset = Random.nextFloat() * 100f,
                        layer = layer
                    )
                )
            }
        }
    }

    // 溅射粒子池
    val splashPool = remember { ParticlePool({ SplashParticle(0f, 0f, 0f, 0f, 0f, 0f, 0f) }, 200) }
    val activeSplashes = remember { mutableStateListOf<SplashParticle>() }

    // 性能优化：使用预定义的画笔和颜色
    val rainColors = remember(layerConfigs) {
        layerConfigs.map { it.color }
    }

    val splashColors = remember {
        listOf(
            Color(0xFFAAC9FF),
            Color(0xFF7BA8E8),
            Color.White.copy(alpha = 0.8f)
        )
    }

    // 动画状态
    var frameTime by remember { mutableStateOf(0L) }
    var lastFrameTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // 性能优化：批量更新雨滴位置
    LaunchedEffect(Unit, intensity) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastFrameTime).coerceIn(8, 33) / 16f // 限制delta范围
            lastFrameTime = currentTime
            frameTime++

            // 批量更新雨滴位置
            rainDrops.forEach { drop ->
                val config = layerConfigs[drop.layer]

                // 更新位置
                drop.y += drop.speed * deltaTime
                drop.x += sin(frameTime * 0.015f + drop.waveOffset) * 0.3f * deltaTime

                // 边界检测和重置
                if (drop.y > screenHeight + 100) {
                    drop.y = -100f
                    drop.x = Random.nextFloat() * screenWidth

                    // 重置随机属性
                    drop.speed = (Random.nextFloat() * 8f + 8f) * config.speedMultiplier * intensity
                    drop.length = Random.nextFloat() * 30f + 20f
                    drop.alpha = (Random.nextFloat() * 0.4f + 0.3f) * config.alphaMultiplier

                    // 产生溅射效果
                    if (enableSplash && drop.layer == 2) { // 仅近层雨滴产生溅射
                        createSplashes(drop.x, screenHeight, splashPool, activeSplashes)
                    }
                }

                // 左右边界循环
                if (drop.x > screenWidth + 50) drop.x = -50f
                if (drop.x < -50f) drop.x = screenWidth + 50f
            }

            // 更新溅射粒子
            if (enableSplash) {
                val iterator = activeSplashes.iterator()
                while (iterator.hasNext()) {
                    val splash = iterator.next()
                    splash.x += splash.vx * deltaTime
                    splash.y += splash.vy * deltaTime
                    splash.vy += 0.3f * deltaTime // 重力
                    splash.life -= 0.02f * deltaTime
                    splash.alpha = splash.life * 0.6f

                    if (splash.life <= 0f || splash.y > screenHeight + 50) {
                        iterator.remove()
                        splashPool.recycle(splash)
                    }
                }
            }

            delay(16) // 60fps
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. 绘制雨雾背景
        drawRect(
            color = Color(0xFF4D7DC0).copy(alpha = 0.03f),
            size = size
        )

        // 2. 批量绘制雨滴（按层分组减少状态切换）
        for (layer in 0..2) {
            val layerRainDrops = rainDrops.filter { it.layer == layer }
            val config = layerConfigs[layer]

            layerRainDrops.forEach { drop ->
                drawRainDrop(drop, config.color, frameTime)
            }
        }

        // 3. 绘制溅射粒子
        if (enableSplash) {
            activeSplashes.forEach { splash ->
                drawSplashParticle(splash, splashColors)
            }
        }
    }
}

// 绘制单个雨滴
private fun DrawScope.drawRainDrop(
    drop: RainParticle,
    baseColor: Color,
    frameTime: Long
) {
    val angleRad = Math.toRadians(drop.angle.toDouble()).toFloat()
    val cosAngle = kotlin.math.cos(angleRad)
    val sinAngle = kotlin.math.sin(angleRad)

    val startX = drop.x
    val startY = drop.y
    val endX = drop.x + drop.length * cosAngle
    val endY = drop.y + drop.length * sinAngle

    // 动态透明度
    val alpha = drop.alpha * (0.8f + sin(frameTime * 0.1f + drop.x) * 0.2f)

    // 主雨滴
    drawLine(
        color = baseColor.copy(alpha = alpha),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = drop.width,
        cap = StrokeCap.Round
    )

    // 光晕效果（仅近层雨滴）
    if (drop.layer == 2) {
        drawLine(
            color = Color.White.copy(alpha = alpha * 0.3f),
            start = Offset(startX - 1f, startY - 1f),
            end = Offset(endX + 1f, endY + 1f),
            strokeWidth = drop.width + 1.5f,
            cap = StrokeCap.Round
        )

        // 头部光点
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.6f),
            radius = drop.width * 0.8f,
            center = Offset(startX, startY)
        )
    }
}

// 绘制溅射粒子
private fun DrawScope.drawSplashParticle(
    splash: SplashParticle,
    colors: List<Color>
) {
    val color = colors[Random.nextInt(colors.size)]

    drawCircle(
        color = color.copy(alpha = splash.alpha),
        radius = splash.size * (splash.life + 0.3f),
        center = Offset(splash.x, splash.y)
    )

    // 溅射轨迹
    drawLine(
        color = color.copy(alpha = splash.alpha * 0.5f),
        start = Offset(splash.x, splash.y),
        end = Offset(splash.x - splash.vx * 2, splash.y - splash.vy * 2),
        strokeWidth = 1f
    )
}

// 创建溅射粒子
private fun createSplashes(
    x: Float,
    groundY: Float,
    pool: ParticlePool<SplashParticle>,
    activeList: MutableList<SplashParticle>
) {
    val splashCount = Random.nextInt(3, 6) // 每个雨滴产生3-5个溅射粒子

    repeat(splashCount) {
        val splash = pool.obtain()
        val angle = Random.nextFloat() * Math.PI.toFloat() * 2
        val speed = Random.nextFloat() * 5f + 3f

        splash.apply {
            this.x = x + Random.nextFloat() * 20f - 10f
            this.y = groundY - 5f
            this.vx = kotlin.math.cos(angle) * speed * Random.nextFloat()
            this.vy = -kotlin.math.sin(angle) * speed * Random.nextFloat() - 2f
            this.life = 0.8f + Random.nextFloat() * 0.5f
            this.size = Random.nextFloat() * 3f + 1f
            this.alpha = 0.8f
        }
        activeList.add(splash)
    }
}

// 层配置数据类
private data class LayerConfig(
    val speedMultiplier: Float,
    val alphaMultiplier: Float,
    val widthMultiplier: Float,
    val color: Color
)