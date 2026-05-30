// app/src/main/java/com/example/travel_footprint_android/data/repository/FootprintRepository.kt
package com.example.travel_footprint_android.data.repository

import android.util.Log
import com.example.travel_footprint_android.data.dao.FootprintDao
import com.example.travel_footprint_android.data.dao.FootprintWithLocation
import com.example.travel_footprint_android.data.dao.LocationDao
import com.example.travel_footprint_android.data.dao.MediaDao
import com.example.travel_footprint_android.data.dao.TagDao
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.FootprintTagCrossRef
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.data.entity.Tag
import com.example.travel_footprint_android.domain.service.LocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootprintRepository @Inject constructor(
    private val footprintDao: FootprintDao,
    private val locationDao: LocationDao,
    private val mediaDao: MediaDao,
    private val tagDao: TagDao,
    private val locationService: LocationService
) {

    fun getFootprintsForMap(journeyId: Long): Flow<List<Footprint>> =
        footprintDao.getFootprintsByJourney(journeyId)

    suspend fun addFootprint(
        journeyId: Long,
        lat: Double,
        lng: Double,
        photos: List<String>?,
        notes: String,//旅程描述
        title:String, //旅程标题
        rating:Int //评分
    ): Long {
        // 1. 获取地址
        val address = locationService.reverseGeocode(lat, lng)

        // 2. 插入足迹
        val footprint = Footprint(
            journeyId = journeyId,
            title =title,
            description = notes,
            createTime = Date(),
            address = address,
            rating = rating
        )
        val footprintId = footprintDao.insertFootprint(footprint)

        // 3. 插入位置
        val location = Location(
            footprintId = footprintId,
            latitude = lat,
            longitude = lng,
            index = 0
        )
        locationDao.insertLocation(location)

        // 4. 插入照片
        photos?.forEachIndexed { index, photoPath ->
            val media = MediaAttachment(
                footprintId = footprintId,
                type = "photo",
                localPath = photoPath,
                thumbnailPath = "",  // 稍后生成
                createTime = Date(),
                caption = "照片 ${index + 1}"
            )
            mediaDao.insertMedia(media)
        }

        return footprintId //返回足迹ID
    }

    suspend fun addFootprint(
        footprint: Footprint
    ): Long {
        // 1. 获取地址
        val address = footprint.address

        // 2. 插入足迹
        val footprint = Footprint(
            journeyId = footprint.journeyId,
            title =footprint.title,
            description = footprint.description,
            createTime = Date(),
            address = address,
            rating = footprint.rating,
            startTime = footprint.startTime
        )
        val footprintId = footprintDao.insertFootprint(footprint)

//        // 3. 插入位置
//        val location = Location(
//            footprintId = footprintId,
//            latitude = lat,
//            longitude = lng,
//            index = 0
//        )
//        locationDao.insertLocation(location)

//        // 4. 插入照片
//        photos?.forEachIndexed { index, photoPath ->
//            val media = MediaAttachment(
//                footprintId = footprintId,
//                type = "photo",
//                localPath = photoPath,
//                thumbnailPath = "",  // 稍后生成
//                createTime = Date(),
//                caption = "照片 ${index + 1}"
//            )
//            mediaDao.insertMedia(media)
//        }

        return footprintId //返回足迹ID
    }


    /**
     * 更新足迹
     */
    /**
     * 更新足迹（完整更新）
     */
    suspend fun updateFootprint(
        footprintId: Long,
        lat: Double,
        lng: Double,
        photos: List<String>?,
        notes: String,
        title: String,
        rating: Int
    ) {
        // 1. 先查询原足迹（获取 journeyId 和 createTime）
        val originalFootprint = footprintDao.getFootprintWithLocation(footprintId).first()
            ?: throw IllegalArgumentException("足迹不存在")

        // 2. 获取地址（根据新经纬度）
        val address = locationService.reverseGeocode(lat, lng)

        // 3. 更新足迹基本信息（保留原有的 journeyId 和 createTime）
        val updatedFootprint = Footprint(
            id = footprintId,
            journeyId = originalFootprint.footprint.journeyId,
            title = title,
            description = notes,
            createTime = originalFootprint.footprint.createTime,  // 保留原创建时间
            address = address,
            rating = rating
        )
        footprintDao.updateFootprint(updatedFootprint)

        // 4. 更新位置
        val locations = locationDao.getLocationsByFootprint(footprintId)
        if (locations.isNotEmpty()) {
            val updatedLocation = locations[0].copy(
                latitude = lat,
                longitude = lng
            )
            locationDao.updateLocation(updatedLocation)
        }

        // 5. 更新照片（先删后增）
        val existingMedia = mediaDao.getMediaByFootprint(footprintId)
        existingMedia.forEach { mediaDao.deleteMedia(it) }

        photos?.forEachIndexed { index, photoPath ->
            val media = MediaAttachment(
                footprintId = footprintId,
                type = "photo",
                localPath = photoPath,
                thumbnailPath = "",
                createTime = Date(),
                caption = "照片 ${index + 1}"
            )
            mediaDao.insertMedia(media)
        }
    }

    /**
     * 获取单个足迹（用于编辑）
     */
    suspend fun getFootprintForEdit(footprintId: Long): FootprintWithLocation? {
        return footprintDao.getFootprintWithLocation(footprintId).first()
    }


    suspend fun updateFootprintLocation(id: Long, lat: Double, lng: Double) {
        val locations = locationDao.getLocationsByFootprint(id)
        if (locations.isNotEmpty()) {
            val primary = locations[0]
            val updated = primary.copy(latitude = lat, longitude = lng)
            locationDao.updateLocation(updated)
        }
    }

    suspend fun addTagToFootprint(footprintId: Long, tagName: String) {
        // 1. 查找或创建标签
        var tag = tagDao.searchTags(tagName).firstOrNull()
        if (tag == null) {
            val tagId = tagDao.insertTag(Tag(name = tagName, color = "#FF5722"))
            tag = tagDao.getAllTags().find { it.id == tagId }
        }

        // 2. 建立关联
        tag?.let {
            val crossRef = FootprintTagCrossRef(footprintId, it.id)
            tagDao.insertFootprintTagCrossRef(crossRef)
            tagDao.incrementTagUsage(it.id)
        }
    }

    suspend fun getFootprintsByTag(tagName: String): List<Footprint> {
        val tags = tagDao.searchTags(tagName)
        if (tags.isEmpty()) return emptyList()

        val tag = tags[0]
        val tagWithFootprints = tagDao.getTagWithFootprints(tag.id)
        return tagWithFootprints.footprints
    }

    fun getFootprintDetail(footprintId: Long) =
        footprintDao.getFootprintWithMedia(footprintId)

    suspend fun clearAllFootprints(journeyId: Long) {
        footprintDao.deleteFootprintsByJourney(journeyId)
    }

    /**
     * 获取单个旅程的足迹数量
     */
    suspend fun getFootprintCount(journeyId: Long): Int {
        val count = footprintDao.getFootprintCountByJourney(journeyId)
        Log.d("FootprintRepo", "getFootprintCount($journeyId) = $count")
        return count
    }

    /**
     * 获取所有旅程的足迹数量（批量）
     */
    suspend fun getFootprintCounts(): Map<Long, Int> {
        val result = footprintDao.getFootprintCountsByJourney()
        Log.d("FootprintRepo", "getFootprintCounts() = $result")
        return result.associate { it.journeyId to it.count }
    }


    // 在 FootprintRepository.kt 中添加
    suspend fun updateFootprint(footprint: Footprint) {
        footprintDao.updateFootprint(footprint)
    }

    // FootprintRepository.kt
    suspend fun deleteFootprint(footprintId: Long) {
        // 直接按ID删除
        footprintDao.deleteFootprintById(footprintId)
    }

    suspend fun deleteFootprint(footprint: Footprint) {
        // 按对象删除，内部调用ID版本
        footprintDao.deleteFootprintById(footprint.id)
    }

    suspend fun getTagsByFootprint(footprintId: Long): List<Tag> {
        return tagDao.getTagsByFootprint(footprintId)
    }

    suspend fun getFootprintById(footprintId: Long): Footprint? {
        return footprintDao.getFootprintById(footprintId)
    }

    // ==================== 地址管理（Location/Address） ====================

    suspend fun getAddressesByFootprint(footprintId: Long): Flow<List<Location>> {
        return locationDao.getAddressesByFootprint(footprintId)
    }

    suspend fun addAddress(location: Location) {
        locationDao.addAddress(location)
    }

    suspend fun deleteLocation(location: Location) {
        locationDao.deleteLocation(location)
    }

    suspend fun setAddressByFootprint(id: Long, footprintId: Long, latitude: Double, longitude: Double, index: Int) {
        locationDao.setAddressByFootprint(id, footprintId, latitude, longitude, index)
    }

    suspend fun updateLocationsByFootprint(footprintId: Long, latitude: Double, longitude: Double, index: Int) {
        locationDao.updateLocationsByFootprint(footprintId, latitude, longitude, index)
    }
}