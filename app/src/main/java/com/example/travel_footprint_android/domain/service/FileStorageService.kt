// app/src/main/java/com/example/travel_footprint_android/domain/service/FileStorageService.kt
package com.example.travel_footprint_android.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.travel_footprint_android.utils.BitmapUtils
import com.example.travel_footprint_android.utils.Constants
import com.example.travel_footprint_android.utils.FileUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageService @Inject constructor(
    private val context: Context
) {

    /**
     * 复制 Uri 到本地存储
     */
    fun copyUriToLocalStorage(uri: Uri, subFolder: String = ""): String {
        val fileName = FileUtils.createUniqueFileName("photo", "jpg")
        val folder = if (subFolder.isNotEmpty()) {
            File(FileUtils.getPhotosDir(context), subFolder)
        } else {
            FileUtils.getPhotosDir(context)
        }

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val destFile = File(folder, fileName)

        return if (FileUtils.copyUriToFile(context, uri, destFile)) {
            destFile.absolutePath
        } else {
            ""
        }
    }

    /**
     * 生成缩略图
     */
    fun generateThumbnail(imagePath: String): String {
        val thumbDir = FileUtils.getThumbnailsDir(context)
        val fileName = File(imagePath).nameWithoutExtension + "_thumb.jpg"
        val thumbFile = File(thumbDir, fileName)

        return if (BitmapUtils.saveThumbnail(
                imagePath,
                thumbFile.absolutePath,
                Constants.THUMBNAIL_WIDTH,
                Constants.THUMBNAIL_HEIGHT
            )
        ) {
            thumbFile.absolutePath
        } else {
            ""
        }
    }

    /**
     * 删除本地文件
     */
    fun deleteLocalFile(path: String): Boolean {
        return FileUtils.deleteFile(path)
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

    /**
     * 获取文件的可读大小
     */
    fun getReadableFileSize(path: String): String {
        val size = getFileSize(path)
        return FileUtils.getReadableFileSize(size)
    }

    /**
     * 保存位图到文件
     */
    fun saveBitmapToFile(bitmap: Bitmap, prefix: String = "image"): String {
        val fileName = FileUtils.createUniqueFileName(prefix, "jpg")
        val file = File(FileUtils.getExportsDir(context), fileName)

        return if (FileUtils.saveBitmapToFile(bitmap, file)) {
            file.absolutePath
        } else {
            ""
        }
    }

    /**
     * 检查文件是否存在
     */
    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }
}