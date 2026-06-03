package com.example.travel_footprint_android.presentation.components.opengl_rain

import java.nio.FloatBuffer
import kotlin.random.Random

class BackgroundRainStreaks(
    private val screenWidth: Int,
    private val screenHeight: Int
) {

    companion object {
        private const val STREAK_COUNT = 100

        private const val VERTS_PER_STREAK = 6

        private const val FLOATS_PER_VERT = 3

        const val TOTAL_FLOATS = STREAK_COUNT * VERTS_PER_STREAK * FLOATS_PER_VERT

        private val LAYER_FAR   = Layer(400f,  700f,  2.8f, 30f..60f,   0.5f)
        private val LAYER_MID   = Layer(700f,  1100f, 4.0f, 45f..90f,   0.7f)
        private val LAYER_NEAR  = Layer(1100f, 1600f, 5.5f, 60f..120f,  0.9f)

        private val LAYERS = arrayOf(LAYER_FAR, LAYER_MID, LAYER_NEAR)
    }

    private data class Layer(
        val speedMin: Float,
        val speedMax: Float,
        val halfWidth: Float,
        val lengthRange: ClosedFloatingPointRange<Float>,
        val alphaMax: Float
    )

    private val x       = FloatArray(STREAK_COUNT)
    private val y       = FloatArray(STREAK_COUNT)
    private val speed   = FloatArray(STREAK_COUNT)
    private val length  = FloatArray(STREAK_COUNT)
    private val hw      = FloatArray(STREAK_COUNT)
    private val alpha   = FloatArray(STREAK_COUNT)

    init {
        for (i in 0 until STREAK_COUNT) {
            resetStreak(i, randomizeY = true)
        }
    }

    fun update(dt: Float) {
        val extendedH = screenHeight * 1.3f
        for (i in 0 until STREAK_COUNT) {
            y[i] += speed[i] * dt
            if (y[i] - length[i] > extendedH) {
                resetStreak(i, randomizeY = false)
            }
        }
    }

    fun fillVertices(buf: FloatBuffer) {
        val hInv = 1f / screenHeight.coerceAtLeast(1)
        for (i in 0 until STREAK_COUNT) {
            val px = x[i]
            val py = y[i]
            val halfW = hw[i]
            val len = length[i]

            val hx = px
            val hy = py
            val tx = px
            val ty = py - len
            val tailHW = halfW * 0.2f

            val headA = ((hy * hInv).coerceIn(0f, 1f) * 0.45f) * alpha[i]
            val tailA = ((ty * hInv).coerceIn(0f, 1f) * 0.45f) * alpha[i]

            buf.put(hx - halfW); buf.put(hy); buf.put(headA)
            buf.put(hx + halfW); buf.put(hy); buf.put(headA)
            buf.put(tx + tailHW); buf.put(ty); buf.put(tailA)

            buf.put(hx - halfW); buf.put(hy); buf.put(headA)
            buf.put(tx + tailHW); buf.put(ty); buf.put(tailA)
            buf.put(tx - tailHW); buf.put(ty); buf.put(tailA)
        }
    }

    private fun resetStreak(i: Int, randomizeY: Boolean) {
        val layer = LAYERS[i % LAYERS.size]
        x[i] = Random.nextFloat() * screenWidth
        y[i] = if (randomizeY) {
            Random.nextFloat() * screenHeight * 1.5f
        } else {
            -Random.nextFloat() * screenHeight * 0.3f
        }
        speed[i] = layer.speedMin + Random.nextFloat() * (layer.speedMax - layer.speedMin)
        val lr = layer.lengthRange
        length[i] = lr.start + Random.nextFloat() * (lr.endInclusive - lr.start)
        hw[i] = layer.halfWidth * (0.8f + Random.nextFloat() * 0.4f)
        alpha[i] = layer.alphaMax * (0.6f + Random.nextFloat() * 0.4f)
    }
}
