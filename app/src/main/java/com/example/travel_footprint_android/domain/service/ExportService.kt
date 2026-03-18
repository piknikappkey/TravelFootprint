// app/src/main/java/com/example/travel_footprint_android/domain/service/ExportService.kt
package com.example.travel_footprint_android.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.data.repository.FootprintRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    private val mapRenderService: MapRenderService,
    private val posterGenerator: PosterGenerator,
    private val localFileManager: LocalFileManager,
    private val journeyRepository: JourneyRepository,
    private val footprintRepository: FootprintRepository
) {

    /**
     * 生成地图图片
     */
    suspend fun generateMapImage(
        journeyId: Long,
        style: HandDrawStyle,
        resolution: String = "medium"
    ): Bitmap? {
        // 获取旅程数据
        val journey = journeyRepository.getJourneyById(journeyId).first() ?: return null

        // 获取足迹数据
        val footprints = footprintRepository.getFootprintsForMap(journeyId).first()

        // 生成地图截图
        val mapBitmap = mapRenderService.captureForExport(false) ?: return null

        // 应用手绘滤镜
        val handDrawnMap = HandDrawEngine().applyHandDrawFilter(mapBitmap, style)

        return handDrawnMap
    }

    /**
     * 导出PNG
     */
    fun exportToPNG(bitmap: Bitmap, quality: Int, fileName: String): File {
        val file = File(localFileManager.getExportDirectory(), fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, out)
        }
        return file
    }

    /**
     * 导出PDF
     */
    suspend fun exportToPDF(journeyId: Long): File {
        // 简化版：生成图片然后保存为PDF
        val bitmap = generateMapImage(journeyId, HandDrawStyle.WATERCOLOR, "high")
        val fileName = "journey_${journeyId}_${System.currentTimeMillis()}.pdf"
        val file = File(localFileManager.getExportDirectory(), fileName)

        // 这里需要添加PDF生成逻辑
        // 可以使用 Android Print Framework 或第三方库

        return file
    }

    /**
     * 创建备份
     */
    suspend fun createBackup(journeyId: Long): File {
        val backupFile = File(localFileManager.getJourneyBackupPath(journeyId))

        ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->

            // 1. 导出旅程数据为JSON
            val journey = journeyRepository.getJourneyById(journeyId).first()
            val footprints = footprintRepository.getFootprintsForMap(journeyId).first()

            val metadata = mapOf(
                "journey" to journey,
                "footprints" to footprints,
                "exportDate" to System.currentTimeMillis()
            )

            // 写入metadata.json
            zipOut.putNextEntry(ZipEntry("metadata.json"))
            val json = com.google.gson.Gson().toJson(metadata)
            zipOut.write(json.toByteArray())
            zipOut.closeEntry()

            // 2. 导出照片
            footprints.forEachIndexed { index, footprint ->
                // 这里需要获取足迹的照片路径
                // val photos = mediaRepository.getMediaByFootprint(footprint.id)
                // photos.forEach { photo ->
                //     val file = File(photo.localPath)
                //     if (file.exists()) {
                //         zipOut.putNextEntry(ZipEntry("photos/${file.name}"))
                //         file.inputStream().use { it.copyTo(zipOut) }
                //         zipOut.closeEntry()
                //     }
                // }
            }
        }

        return backupFile
    }

    /**
     * 从备份恢复
     */
    suspend fun restoreFromBackup(backupFile: File): Boolean {
        return try {
            // 解压备份文件
            // 解析metadata.json
            // 恢复数据到数据库
            // 恢复照片文件
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 分享图片
     */
    fun shareImage(bitmap: Bitmap, title: String, context: Context): Uri? {
        val fileName = "share_${System.currentTimeMillis()}.jpg"
        val file = File(localFileManager.getCacheDirectory(), fileName)

        return if (FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            null
        }
    }
}