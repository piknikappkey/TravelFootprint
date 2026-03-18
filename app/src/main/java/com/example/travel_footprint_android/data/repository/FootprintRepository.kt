// app/src/main/java/com/example/travel_footprint_android/data/repository/FootprintRepository.kt
package com.example.travel_footprint_android.data.repository

import com.example.travel_footprint_android.data.dao.*
import com.example.travel_footprint_android.data.entity.*
import com.example.travel_footprint_android.domain.service.LocationService
import kotlinx.coroutines.flow.Flow
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
        notes: String
    ): Long {
        // 1. 获取地址
        val address = locationService.reverseGeocode(lat, lng)

        // 2. 插入足迹
        val footprint = Footprint(
            journeyId = journeyId,
            title = notes.take(20),  // 取前20字作为标题
            description = notes,
            createTime = Date(),
            address = address,
            rating = 0
        )
        val footprintId = footprintDao.insertFootprint(footprint)

        // 3. 插入位置
        val location = Location(
            footprintId = footprintId,
            latitude = lat,
            longitude = lng,
            orderIndex = 0
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

        return footprintId
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
}