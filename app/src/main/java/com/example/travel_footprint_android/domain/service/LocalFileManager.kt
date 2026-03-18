// app/src/main/java/com/example/travel_footprint_android/domain/service/LocalFileManager.kt
package com.example.travel_footprint_android.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.travel_footprint_android.utils.FileUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFileManager @Inject constructor(
    private val context: Context
) {

    /**
     * 保存位图到相册
     */
    fun saveBitmapToGallery(bitmap: Bitmap, name: String): Uri? {
        val fileName = if (name.endsWith(".jpg")) name else "$name.jpg"
        return FileUtils.saveBitmapToGallery(context, bitmap, fileName)
    }

    /**
     * 获取旅程备份路径
     */
    fun getJourneyBackupPath(journeyId: Long): String {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return File(backupDir, "journey_${journeyId}_${System.currentTimeMillis()}.zip").absolutePath
    }

    /**
     * 清理缓存
     */
    fun clearCache(): Boolean {
        return FileUtils.clearCacheDir(context)
    }

    /**
     * 获取存储使用量
     */
    fun getStorageUsage(): Long {
        val filesDir = context.filesDir
        return calculateFolderSize(filesDir)
    }

    /**
     * 获取缓存大小
     */
    fun getCacheSize(): Long {
        val cacheDir = context.cacheDir
        return calculateFolderSize(cacheDir)
    }

    /**
     * 计算文件夹大小
     */
    private fun calculateFolderSize(file: File): Long {
        if (!file.exists()) return 0L

        return if (file.isFile) {
            file.length()
        } else {
            file.listFiles()?.sumOf { calculateFolderSize(it) } ?: 0L
        }
    }

    /**
     * 创建临时文件
     */
    fun createTempFile(prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }

    /**
     * 获取应用目录
     */
    fun getAppDirectory(): File = context.filesDir

    /**
     * 获取缓存目录
     */
    fun getCacheDirectory(): File = context.cacheDir

    /**
     * 获取导出目录
     */
    fun getExportDirectory(): File = FileUtils.getExportsDir(context)
}