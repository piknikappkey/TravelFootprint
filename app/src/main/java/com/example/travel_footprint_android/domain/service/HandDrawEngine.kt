// app/src/main/java/com/example/travel_footprint_android/domain/service/HandDrawEngine.kt
package com.example.travel_footprint_android.domain.service

import android.graphics.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

@Singleton
class HandDrawEngine @Inject constructor() {

    data class HandDrawConfig(
        val jitter: Float = 2f,              // 抖动程度
        val sketchIntensity: Float = 0.8f,    // 素描强度
        val paperTexture: Bitmap? = null,     // 纸张纹理
        val colorPalette: List<Int> = listOf(  // 调色板
            Color.parseColor("#8B4513"),  // 棕色
            Color.parseColor("#2E8B57"),  // 海洋绿
            Color.parseColor("#4682B4"),  // 钢蓝
            Color.parseColor("#DAA520"),  // 金色
            Color.parseColor("#708090")   // 石板灰
        )
    )

    /**
     * 应用手绘滤镜
     */
    fun applyHandDrawFilter(mapSnapshot: Bitmap, style: HandDrawStyle): Bitmap {
        return when (style) {
            HandDrawStyle.WATERCOLOR -> applyWatercolorEffect(mapSnapshot)
            HandDrawStyle.PENCIL_SKETCH -> applyPencilSketchEffect(mapSnapshot)
            HandDrawStyle.VINTAGE_PAPER -> applyVintagePaperEffect(mapSnapshot)
            HandDrawStyle.INK_WASH -> applyInkWashEffect(mapSnapshot)
            HandDrawStyle.CRAYON -> applyCrayonEffect(mapSnapshot)
        }
    }

    /**
     * 生成手绘路径
     */
    fun generateSketchPath(points: List<Pair<Double, Double>>, jitter: Float): Path {
        val path = Path()
        if (points.isEmpty()) return path

        // 添加抖动，模拟手绘的不稳定性
        val jitteredPoints = points.map { (lat, lng) ->
            val random = Random()
            val jitterLat = (random.nextFloat() - 0.5f) * jitter
            val jitterLng = (random.nextFloat() - 0.5f) * jitter
            lat + jitterLat to lng + jitterLng
        }

        path.moveTo(jitteredPoints[0].first.toFloat(), jitteredPoints[0].second.toFloat())

        for (i in 1 until jitteredPoints.size) {
            val (lat, lng) = jitteredPoints[i]
            path.lineTo(lat.toFloat(), lng.toFloat())
        }

        return path
    }

    /**
     * 创建铅笔笔触
     */
    fun createPencilStroke(width: Float, hardness: Float): PathEffect {
        // 创建虚线效果模拟铅笔笔触
        val dashInterval = width * 0.5f
        val dashPattern = floatArrayOf(dashInterval, dashInterval * 0.3f)

        return ComposePathEffect(
            DashPathEffect(dashPattern, 0f),
            CornerPathEffect(width * hardness)
        )
    }

    /**
     * 加载本地风格
     */
    fun loadLocalStyle(styleName: String): HandDrawConfig {
        return when (styleName.lowercase()) {
            "watercolor" -> HandDrawConfig(
                jitter = 1.5f,
                sketchIntensity = 0.6f,
                colorPalette = listOf(
                    Color.parseColor("#87CEEB"),  // 天蓝
                    Color.parseColor("#98FB98"),  // 淡绿
                    Color.parseColor("#FFB6C1")   // 粉红
                )
            )
            "pencil" -> HandDrawConfig(
                jitter = 0.8f,
                sketchIntensity = 1.0f,
                colorPalette = listOf(Color.DKGRAY, Color.GRAY, Color.LTGRAY)
            )
            else -> HandDrawConfig()
        }
    }

    /**
     * 渲染水彩效果
     */
    fun renderWatercolorEffect(canvas: Canvas, paperTexture: Bitmap?) {
        paperTexture?.let {
            // 叠加纸张纹理
            val paint = Paint().apply {
                alpha = 80
                xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            }
            canvas.drawBitmap(it, 0f, 0f, paint)
        }

        // 添加水彩边缘扩散效果
        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        }
    }

    /**
     * 应用水彩效果
     */
    private fun applyWatercolorEffect(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(result)

        // 降低饱和度，增加透明度
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix().apply {
                    setSaturation(0.7f)
                }
            )
            alpha = 200
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // 添加水彩边缘
        val edgePaint = Paint().apply {
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            color = Color.argb(50, 100, 150, 200)
        }

        return result
    }

    /**
     * 应用铅笔素描效果
     */
    private fun applyPencilSketchEffect(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(result)

        // 转为灰度
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix().apply {
                    setSaturation(0f)
                }
            )
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // 添加铅笔纹理
        val noisePaint = Paint().apply {
            alpha = 30
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        }

        return result
    }

    /**
     * 应用复古牛皮纸效果
     */
    private fun applyVintagePaperEffect(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(result)

        // 先画牛皮纸底色
        canvas.drawColor(Color.parseColor("#F4E6C2"))

        // 叠加原图，使用叠加模式
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            alpha = 180
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * 应用水墨效果
     */
    private fun applyInkWashEffect(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(result)

        // 水墨风格：高对比度，模糊边缘
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix().apply {
                    setSaturation(0.2f)
                    setScale(1.2f, 1.2f, 1.2f, 1f)
                }
            )
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * 应用蜡笔效果
     */
    private fun applyCrayonEffect(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(result)

        // 蜡笔风格：粗糙纹理，鲜艳颜色
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix().apply {
                    setScale(1.3f, 1.2f, 1.1f, 1f)
                }
            )
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }
}