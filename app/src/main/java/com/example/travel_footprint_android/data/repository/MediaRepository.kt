// app/src/main/java/com/example/travel_footprint_android/data/repository/MediaRepository.kt
package com.example.travel_footprint_android.data.repository

import com.example.travel_footprint_android.data.dao.MediaDao
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.domain.service.FileStorageService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val mediaDao: MediaDao,           // 注入 mediaDao
    private val fileStorage: FileStorageService  // 注入 fileStorage
) {

    /**
     * 获取所有照片
     */
    fun getAllPhotos(): Flow<List<MediaAttachment>> {
        return mediaDao.getAllPhotos()
    }

    /**
     * 根据足迹ID列表获取多媒体
     */
    suspend fun getMediaByFootprints(footprintIds: List<Long>): List<MediaAttachment> {
        return mediaDao.getMediaByFootprints(footprintIds)
    }

    /**
     * 保存照片到本地
     */
    suspend fun savePhotoToLocal(uri: String): String {
        // 实现保存逻辑
        return fileStorage.copyUriToLocalStorage(android.net.Uri.parse(uri), "photos")
    }

    /**
     * 生成缩略图
     */
    suspend fun generateThumbnail(imagePath: String): String {
        return fileStorage.generateThumbnail(imagePath)
    }

    /**
     * 删除多媒体文件
     */
    suspend fun deleteMediaFile(mediaId: Long) {
        // 先获取多媒体信息
        val media = mediaDao.getMediaByFootprint(mediaId).firstOrNull()
        media?.let {
            // 删除文件
            fileStorage.deleteLocalFile(it.localPath)
            fileStorage.deleteLocalFile(it.thumbnailPath)
            // 删除数据库记录
            mediaDao.deleteMedia(it)
        }
    }
}