// app/src/main/java/com/example/travel_footprint_android/domain/usecase/AppService.kt
package com.example.travel_footprint_android.domain.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.data.repository.FootprintRepository
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.data.repository.MediaRepository
import com.example.travel_footprint_android.domain.service.LocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用统一服务接口
 * 前端只需注入这个类，即可调用所有后端功能
 */
@Singleton
class AppService @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val footprintRepository: FootprintRepository,
    private val mediaRepository: MediaRepository,
    private val locationService: LocationService,
    @ApplicationContext private val context: Context  // 添加这一行
) {

    // ==================== 旅程相关 ====================

    fun getAllJourneys(): Flow<List<Journey>> = journeyRepository.getAllJourneys()

    fun getJourneyById(id: Long): Flow<Journey> = journeyRepository.getJourneyById(id)

    suspend fun createJourney(
        title: String,
        style: String = "watercolor",
        description: String = ""
    ): Long {
        return withContext(Dispatchers.IO) {
            journeyRepository.createJourney(title, style, description)
        }
    }

    /**
     * 更新旅程
     */
    suspend fun updateJourney(journey: Journey) {
        withContext(Dispatchers.IO) {
            journeyRepository.updateJourney(journey)
        }
    }

    suspend fun updateJourneyCover(journeyId: Long, imagePath: String) {
        withContext(Dispatchers.IO) {
            journeyRepository.updateJourneyCover(journeyId, imagePath)
        }
    }

    suspend fun deleteJourney(journeyId: Long) {
        withContext(Dispatchers.IO) {
            journeyRepository.deleteJourneyWithAllData(journeyId)
        }
    }

    suspend fun searchJourneys(keyword: String): List<Journey> {
        return withContext(Dispatchers.IO) {
            journeyRepository.searchJourneys(keyword)
        }
    }

    suspend fun getAllFootprintCounts(): Map<Long, Int> {
        return withContext(Dispatchers.IO) {
            journeyRepository.getFootprintCounts()
        }
    }

    suspend fun getFootprintCount(journeyId: Long): Int {
        return withContext(Dispatchers.IO) {
            journeyRepository.getFootprintCount(journeyId)
        }
    }

    // ==================== 足迹相关 ====================

    fun getFootprintsForMap(journeyId: Long): Flow<List<Footprint>> =
        footprintRepository.getFootprintsForMap(journeyId)

    suspend fun addFootprint(
        journeyId: Long,
        lat: Double,
        lng: Double,
        notes: String,
        photos: List<String>? = null
    ): Long {
        return withContext(Dispatchers.IO) {
            footprintRepository.addFootprint(journeyId, lat, lng, photos, notes)
        }
    }

    /**
     * 更新足迹 - 需要在 FootprintRepository 中添加此方法
     */
    suspend fun updateFootprint(footprint: Footprint) {
        withContext(Dispatchers.IO) {
            footprintRepository.updateFootprint(footprint)
        }
    }

    suspend fun updateFootprintLocation(footprintId: Long, lat: Double, lng: Double) {
        withContext(Dispatchers.IO) {
            footprintRepository.updateFootprintLocation(footprintId, lat, lng)
        }
    }

    /**
     * 删除单个足迹 - 需要在 FootprintRepository 中添加此方法
     */
    suspend fun deleteFootprint(footprintId: Long) {
        withContext(Dispatchers.IO) {
            footprintRepository.deleteFootprint(footprintId)
        }
    }

    suspend fun clearAllFootprints(journeyId: Long) {
        withContext(Dispatchers.IO) {
            footprintRepository.clearAllFootprints(journeyId)
        }
    }

    fun getFootprintDetail(footprintId: Long) = footprintRepository.getFootprintDetail(footprintId)

    // ==================== 标签相关 ====================

    suspend fun addTagToFootprint(footprintId: Long, tagName: String) {
        withContext(Dispatchers.IO) {
            footprintRepository.addTagToFootprint(footprintId, tagName)
        }
    }

    suspend fun getTagsByFootprint(footprintId: Long): List<String> {
        return withContext(Dispatchers.IO) {
            footprintRepository.getTagsByFootprint(footprintId).map { it.name }
        }
    }

    suspend fun getFootprintsByTag(tagName: String): List<Footprint> {
        return withContext(Dispatchers.IO) {
            footprintRepository.getFootprintsByTag(tagName)
        }
    }

    // ==================== 位置相关 ====================

    suspend fun getCurrentLocation(): android.location.Location? {
        return withContext(Dispatchers.IO) {
            locationService.getCurrentLocation()
        }
    }

    /**
     * 获取当前位置的经纬度（简化版）
     */
    suspend fun getCurrentLatLng(): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            locationService.getCurrentLocation()?.let {
                Pair(it.latitude, it.longitude)
            }
        }
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            locationService.reverseGeocode(lat, lng)
        }
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        return locationService.calculateDistance(lat1, lng1, lat2, lng2)
    }

    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    // 单独获取
    suspend fun getCurrentProvince(): String {
        return withContext(Dispatchers.IO) {
            locationService.getCurrentProvince()
        }
    }

    suspend fun getCurrentCity(): String {
        return withContext(Dispatchers.IO) {
            locationService.getCurrentCity()
        }
    }

    suspend fun getCurrentDistrict(): String {
        return withContext(Dispatchers.IO) {
            locationService.getCurrentDistrict()
        }
    }

    suspend fun getProvince(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            locationService.getProvince(lat, lng)
        }
    }

    suspend fun getCity(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            locationService.getCity(lat, lng)
        }
    }

    suspend fun getDistrict(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            locationService.getDistrict(lat, lng)
        }
    }

    // 综合获取
    suspend fun getCurrentLocationDetail(): LocationService.LocationDetail? {
        return withContext(Dispatchers.IO) {
            locationService.getCurrentLocationDetail()
        }
    }

    suspend fun getLocationDetail(lat: Double, lng: Double): LocationService.LocationDetail {
        return withContext(Dispatchers.IO) {
            locationService.getLocationDetail(lat, lng)
        }
    }

    // ==================== 媒体相关 ====================

    suspend fun savePhotoToLocal(uri: String): String {
        return withContext(Dispatchers.IO) {
            mediaRepository.savePhotoToLocal(uri)
        }
    }

    suspend fun generateThumbnail(imagePath: String): String {
        return withContext(Dispatchers.IO) {
            mediaRepository.generateThumbnail(imagePath)
        }
    }

    fun getAllPhotos(): Flow<List<MediaAttachment>> = mediaRepository.getAllPhotos()

    /**
     * 获取足迹的所有照片 - 需要在 MediaRepository 中添加此方法
     */
    suspend fun getPhotosByFootprint(footprintId: Long): List<MediaAttachment> {
        return withContext(Dispatchers.IO) {
            mediaRepository.getPhotosByFootprint(footprintId)
        }
    }

    suspend fun deletePhoto(mediaId: Long) {
        withContext(Dispatchers.IO) {
            mediaRepository.deleteMediaFile(mediaId)
        }
    }


    /**
     * 为足迹添加图片
     */
    suspend fun addPhotoToFootprint(
        footprintId: Long,
        imagePath: String,
        caption: String = ""
    ): Long {
        return withContext(Dispatchers.IO) {
            // 生成缩略图
            val thumbnailPath = mediaRepository.generateThumbnail(imagePath)

            // 创建媒体附件
            val media = MediaAttachment(
                footprintId = footprintId,
                type = "photo",
                localPath = imagePath,
                thumbnailPath = thumbnailPath,
                createTime = Date(),
                caption = caption
            )
            mediaRepository.insertMedia(media)
        }
    }


    /**
     * 为足迹添加图片并保存到数据库
     * @param footprintId 足迹ID
     * @param imagePath 图片本地路径
     * @param thumbnailPath 缩略图路径
     * @param caption 图片说明
     * @return 插入的 mediaId，失败返回 0
     */
    suspend fun addPhotoToFootprint(
        footprintId: Long,
        imagePath: String,
        thumbnailPath: String,
        caption: String = ""
    ): Long {
        return withContext(Dispatchers.IO) {
            try {
                val media = MediaAttachment(
                    footprintId = footprintId,
                    type = "photo",
                    localPath = imagePath,
                    thumbnailPath = thumbnailPath,
                    createTime = Date(),
                    caption = caption
                )
                mediaRepository.insertMedia(media)
            } catch (e: Exception) {
                Log.e("AppService", "添加图片失败: ${e.message}")
                0L
            }
        }
    }

    /**
     * 从 Uri 复制图片到应用本地目录
     * @param uri 图片的 Uri（从相册选择或相机拍摄）
     * @return 保存后的本地路径，失败返回空字符串
     */
    suspend fun copyImageToLocal(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val destFile = File(context.filesDir, "images/$fileName")

                // 创建目录
                destFile.parentFile?.mkdirs()

                // 复制文件
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                destFile.absolutePath
            } catch (e: Exception) {
                Log.e("AppService", "复制图片失败: ${e.message}")
                ""
            }
        }
    }

    /**
     * 从本地文件路径复制图片到应用目录
     * @param imagePath 本地图片绝对路径
     * @return 保存后的本地路径
     */
    suspend fun saveImageToApp(imagePath: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(imagePath)
                if (!sourceFile.exists()) {
                    return@withContext ""
                }

                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val destFile = File(context.filesDir, "images/$fileName")
                destFile.parentFile?.mkdirs()

                sourceFile.inputStream().use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                destFile.absolutePath
            } catch (e: Exception) {
                Log.e("AppService", "保存图片失败: ${e.message}")
                ""
            }
        }
    }

}