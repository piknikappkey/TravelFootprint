// app/src/main/java/com/example/travel_footprint_android/domain/service/FileStorageService.kt
package com.example.travel_footprint_android.domain.service

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.travel_footprint_android.utils.FileUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageService @Inject constructor(
    private val context: Context
) {

    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * 复制 Uri 到本地存储
     */
    fun copyUriToLocalStorage(uri: Uri, subFolder: String): String {
        return try {
            // 创建目标目录
            val photosDir = File(context.filesDir, subFolder)
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }

            // 生成唯一文件名
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val destFile = File(photosDir, fileName)

            // 复制文件
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 生成缩略图
     */
    fun generateThumbnail(imagePath: String): String {
        return try {
            val file = File(imagePath)
            if (!file.exists()) {
                return ""
            }

            // 创建缩略图目录
            val thumbDir = File(context.cacheDir, "thumbnails")
            if (!thumbDir.exists()) {
                thumbDir.mkdirs()
            }

            // 生成缩略图文件名
            val thumbName = "thumb_${file.nameWithoutExtension}.jpg"
            val thumbFile = File(thumbDir, thumbName)

            // 解码图片并缩放
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            // 计算缩放比例（缩略图最大 300px）
            val scale = maxOf(options.outWidth, options.outHeight) / 300
            val sampleSize = if (scale > 1) scale else 1

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeFile(imagePath, decodeOptions)

            // 保存缩略图
            FileOutputStream(thumbFile).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            thumbFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 删除本地文件
     */
    fun deleteLocalFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取文件大小
     */
    fun getFileSize(path: String): Long {
        return try {
            File(path).length()
        } catch (e: Exception) {
            0L
        }
    }
}