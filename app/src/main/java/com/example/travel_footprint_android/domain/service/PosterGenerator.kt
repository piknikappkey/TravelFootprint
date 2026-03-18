// app/src/main/java/com/example/travel_footprint_android/domain/service/PosterGenerator.kt
package com.example.travel_footprint_android.domain.service

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosterGenerator @Inject constructor() {

    data class PosterStyle(
        var backgroundColor: Int = Color.WHITE,
        var borderColor: Int = Color.BLACK,
        var titleColor: Int = Color.BLACK,
        var dateColor: Int = Color.GRAY,
        var borderWidth: Float = 20f,
        var padding: Float = 50f
    )

    /**
     * 创建海报
     */
    fun createPoster(
        mapImage: Bitmap,
        title: String,
        date: String,
        style: PosterStyle = PosterStyle()
    ): Bitmap {
        // 计算海报尺寸（地图 + 标题区域）
        val titleHeight = 200f
        val totalHeight = mapImage.height + titleHeight.toInt() + (style.padding * 2).toInt()

        val config = mapImage.config ?: Bitmap.Config.ARGB_8888
        val poster = Bitmap.createBitmap(mapImage.width, totalHeight, config)
        val canvas = Canvas(poster)

        // 填充背景
        canvas.drawColor(style.backgroundColor)

        // 绘制地图
        canvas.drawBitmap(mapImage, style.padding, style.padding, null)

        // 绘制标题
        val titlePaint = TextPaint().apply {
            color = style.titleColor
            textSize = 80f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val titleX = style.padding
        val titleY = mapImage.height + style.padding * 1.5f

        // 使用 StaticLayout 支持多行文本
        val staticLayout = StaticLayout.Builder.obtain(
            title,
            0,
            title.length,
            titlePaint,
            (mapImage.width - style.padding * 2).toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()

        canvas.save()
        canvas.translate(titleX, titleY)
        staticLayout.draw(canvas)
        canvas.restore()

        // 绘制日期
        val datePaint = Paint().apply {
            color = style.dateColor
            textSize = 40f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val dateY = titleY + staticLayout.height + 30f
        canvas.drawText(date, mapImage.width - style.padding, dateY, datePaint)

        // 添加手绘边框
        val borderPaint = Paint().apply {
            color = style.borderColor
            this.style = Paint.Style.STROKE
            strokeWidth = style.borderWidth
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(30f, 15f), 0f)
        }

        canvas.drawRect(
            style.borderWidth / 2,
            style.borderWidth / 2,
            poster.width - style.borderWidth / 2,
            poster.height - style.borderWidth / 2,
            borderPaint
        )

        return poster
    }

    /**
     * 添加手绘边框
     */
    fun addHandDrawnBorder(canvas: Canvas, pattern: String = "default") {
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 15f
            isAntiAlias = true
        }

        when (pattern) {
            "dashed" -> paint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), 0f)
            "dotted" -> paint.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
            else -> paint.pathEffect = DashPathEffect(floatArrayOf(40f, 10f, 10f, 10f), 0f)
        }

        canvas.drawRect(
            10f,
            10f,
            canvas.width - 10f,
            canvas.height - 10f,
            paint
        )
    }

    /**
     * 添加手写标题
     */
    fun addHandwrittenTitle(canvas: Canvas, text: String, x: Float, y: Float) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 100f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // 添加手写抖动效果
        val random = java.util.Random()
        val chars = text.toCharArray()
        var currentX = x - paint.measureText(text) / 2

        chars.forEach { char ->
            val jitterX = (random.nextFloat() - 0.5f) * 10f
            val jitterY = (random.nextFloat() - 0.5f) * 10f
            canvas.drawText(char.toString(), currentX + jitterX, y + jitterY, paint)
            currentX += paint.measureText(char.toString())
        }
    }

    /**
     * 添加日期印章
     */
    fun addDateStamp(canvas: Canvas, date: Date, format: String) {
        val dateStr = java.text.SimpleDateFormat(format, java.util.Locale.getDefault()).format(date)

        val paint = Paint().apply {
            color = Color.argb(100, 150, 150, 150)
            textSize = 60f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText(
            dateStr,
            canvas.width - 50f,
            canvas.height - 50f,
            paint
        )
    }

    /**
     * 添加贴纸元素
     */
    fun addStickerElements(canvas: Canvas, elements: List<Bitmap>) {
        elements.forEachIndexed { index, bitmap ->
            val x = 50f + (index * 100) % (canvas.width - 200)
            val y = canvas.height - 150f + (index / 5) * 80
            canvas.drawBitmap(bitmap, x, y, null)
        }
    }
}