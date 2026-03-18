// app/src/main/java/com/example/travel_footprint_android/utils/FileUtils.kt
package com.example.travel_footprint_android.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object FileUtils {

    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_FILENAME, Locale.getDefault())

    /**
     * 获取应用私有目录
     */
    fun getAppDir(context: Context): File {
        return context.filesDir
    }

    /**
     * 获取缓存目录
     */
    fun getCacheDir(context: Context): File {
        return context.cacheDir
    }

    /**
     * 获取照片目录
     */
    fun getPhotosDir(context: Context): File {
        val dir = File(context.filesDir, Constants.DIR_PHOTOS)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 获取缩略图目录
     */
    fun getThumbnailsDir(context: Context): File {
        val dir = File(context.cacheDir, Constants.DIR_THUMBNAILS)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 获取导出目录
     */
    fun getExportsDir(context: Context): File {
        val dir = File(context.filesDir, Constants.DIR_EXPORTS)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 创建唯一文件名
     */
    fun createUniqueFileName(prefix: String, extension: String): String {
        val timestamp = dateFormat.format(Date())
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return "${prefix}_${timestamp}_${uuid}.$extension"
    }

    /**
     * 复制 Uri 到本地文件
     */
    fun copyUriToFile(context: Context, uri: Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 保存位图到文件
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 90): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 保存位图到相册（Android 10+ 兼容）
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val contentResolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelFootprint")
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            }
            return uri
        } else {
            // Android 9 及以下
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "TravelFootprint")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val file = File(appDir, fileName)
            if (saveBitmapToFile(bitmap, file)) {
                // 通知相册更新
                context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                return Uri.fromFile(file)
            }
            return null
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取文件大小（可读格式）
     */
    fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            Locale.getDefault(),
            "%.1f %s",
            size / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    /**
     * 清理缓存目录
     */
    fun clearCacheDir(context: Context): Boolean {
        return try {
            val cacheDir = getCacheDir(context)
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}