// app/src/main/java/com/example/travel_footprint_android/data/repository/JourneyRepository.kt
package com.example.travel_footprint_android.data.repository

import com.example.travel_footprint_android.data.dao.JourneyDao
import com.example.travel_footprint_android.data.dao.FootprintDao
import com.example.travel_footprint_android.data.entity.Journey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyRepository @Inject constructor(
    private val journeyDao: JourneyDao,
    private val footprintDao: FootprintDao
) {

    fun getAllJourneys(): Flow<List<Journey>> = journeyDao.getAllJourneys()

    fun getJourneyById(id: Long): Flow<Journey> = journeyDao.getJourneyById(id)

    fun getJourneyWithFootprints(journeyId: Long) = journeyDao.getJourneyWithFootprints(journeyId)

    suspend fun createJourney(
        //
//        var title: String,                          // 标题
//        val description: String,                    // 描述
//        val startDate: Date,                        // 开始日期
//        val endDate: Date,                          // 结束日期
//        val coverStyle: String,                     // 封面风格
//        var coverImagePath: String,                 // 封面图片路径
//        var journeyImagePaths: List<String>,        // 旅程图片路径列表
//        var address: String = "",                   // 旅程地址（如：北京市、苏州市）
//        var longitude: Double=0.0,                      //旅程经度
//        var latitude: Double=0.0,                       //纬度
        //
        title: String,
        description: String = "",
        startDate: Date = Date(),
        endDate: Date = Date(),
        coverStyle: String,
        coverImagePath: String = "",
        journeyImagePaths: List<String> = emptyList(),
        address: String = "",
        longitude: Double = 0.0,
        latitude: Double = 0.0
    ): Long {
        val journey = Journey(
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            coverStyle = coverStyle,
            coverImagePath = coverImagePath,
            journeyImagePaths = journeyImagePaths,
            address = address,
            longitude = longitude,
            latitude = latitude

        )
        return journeyDao.insertJourney(journey)
    }

    suspend fun updateJourneyCover(journeyId: Long, imagePath: String) {
        val journey = journeyDao.getJourneyByIdSuspend(journeyId) ?: return
        val updated = journey.copy(coverImagePath = imagePath)
        journeyDao.updateJourney(updated)
    }

    suspend fun deleteJourneyWithAllData(journeyId: Long) {
        journeyDao.deleteJourneyById(journeyId)
    }

    //删除旅程
    suspend fun deleteJourney(journey: Journey){
        journeyDao.deleteJourney(journey)
    }

    suspend fun searchJourneys(keyword: String): List<Journey> {
        return journeyDao.searchJourneys(keyword)
    }

    suspend fun getFootprintCount(journeyId: Long): Int {
        return withContext(Dispatchers.IO) {
            footprintDao.getFootprintCountByJourney(journeyId)
        }
    }

    suspend fun getFootprintCounts(): Map<Long, Int> {
        return withContext(Dispatchers.IO) {
            val result = footprintDao.getFootprintCountsByJourney()
            android.util.Log.d("JourneyRepo", "getFootprintCounts result: $result")
            result.associate { it.journeyId to it.count }
        }
    }


    suspend fun updateJourney(journey: Journey) {
        journeyDao.updateJourney(journey)
    }

    // ==================== 🆕 地址和经纬度查询方法 ====================

    suspend fun getJourneysByAddress(address: String): List<Journey> {
        return journeyDao.getJourneysByAddress(address)
    }

    fun getAllJourneysWithAddress(): Flow<List<Journey>> {
        return journeyDao.getAllJourneysWithAddress()
    }

    suspend fun getJourneysInBounds(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<Journey> {
        return journeyDao.getJourneysInBounds(minLat, maxLat, minLng, maxLng)
    }

    //附近旅程查询
    suspend fun getNearbyJourneys(
        centerLat: Double,
        centerLng: Double,
        radiusKm: Double = 50.0,
        limit: Int = 20
    ): List<Journey> {
        // 1度 ≈ 111km，计算经纬度范围
        val delta = radiusKm / 111.0
        val minLat = centerLat - delta
        val maxLat = centerLat + delta
        val minLng = centerLng - delta
        val maxLng = centerLng + delta

        return journeyDao.getNearbyJourneys(
            centerLat, centerLng,
            minLat, maxLat, minLng, maxLng,
            limit
        )
    }

    //获取所有有坐标的旅程（用于地图）
    fun getAllJourneysWithCoordinates(): Flow<List<Journey>> {
        return journeyDao.getAllJourneysWithCoordinates()
    }

    // ==================== 里程碑统计方法 ====================

    /**
     * 获取旅程总数
     */
    suspend fun getJourneyCount(): Int = journeyDao.getJourneyCount()

    /**
     * 获取有封面的旅程数量
     */
    suspend fun getCoveredJourneyCount(): Int = journeyDao.getCoveredJourneyCount()

    /**
     * 获取所有旅程的图片总数（应用层计算）
     */
    suspend fun getTotalImageCount(): Int {
        val journeys = journeyDao.getAllJourneysSuspend()
        return journeys.sumOf { journey ->
            journey.journeyImagePaths.count { it.isNotEmpty() }
        }
    }

}