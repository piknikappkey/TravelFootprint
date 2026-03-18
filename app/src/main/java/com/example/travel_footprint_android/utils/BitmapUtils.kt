// app/src/main/java/com/example/travel_footprint_android/utils/BitmapUtils.kt
package com.example.travel_footprint_android.utils

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    /**
     * 从文件加载位图，自动适配尺寸
     */
    fun decodeBitmapFromFile(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            // 先获取尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            // 计算采样率
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // 重新加载
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 计算采样率
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 创建缩略图
     */
    fun createThumbnail(imagePath: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(imagePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 保存缩略图到文件
     */
    fun saveThumbnail(imagePath: String, destPath: String, maxWidth: Int, maxHeight: Int): Boolean {
        val bitmap = createThumbnail(imagePath, maxWidth, maxHeight) ?: return false
        return try {
            FileOutputStream(destPath).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.THUMBNAIL_QUALITY, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * 旋转图片（根据EXIF信息）
     */
    fun rotateImageIfNeeded(filePath: String): Bitmap? {
        try {
            val ei = ExifInterface(filePath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            var rotation = 0
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
            }

            if (rotation != 0) {
                val bitmap = BitmapFactory.decodeFile(filePath)
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 添加手绘边框
     */
    fun addHandDrawnBorder(bitmap: Bitmap, color: Int = Color.BLACK, width: Float = 20f): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width!!, bitmap.height, bitmap.config!!)
        val canvas = Canvas(result)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = width
            isAntiAlias = true
            // 添加抖动效果，模拟手绘
            pathEffect = DashPathEffect(floatArrayOf(width, width * 0.5f), 0f)
        }

        canvas.drawRect(
            width / 2,
            width / 2,
            bitmap.width.toFloat() - width / 2,
            bitmap.height.toFloat() - width / 2,
            paint
        )

        return result
    }

    /**
     * 添加水印
     */
    fun addWatermark(bitmap: Bitmap, text: String, color: Int = Color.GRAY): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!);         val canvas = Canvas(result)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val paint = Paint().apply {
            this.color = color
            textSize = 60f
            alpha = 128
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val x = bitmap.width / 2f
        val y = bitmap.height / 2f

        canvas.save()
        canvas.rotate(-30f, x, y)
        canvas.drawText(text, x, y, paint)
        canvas.restore()

        return result
    }
}