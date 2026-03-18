// app/src/main/java/com/example/travel_footprint_android/data/repository/JourneyRepository.kt
package com.example.travel_footprint_android.data.repository

import com.example.travel_footprint_android.data.dao.JourneyDao
import com.example.travel_footprint_android.data.dao.FootprintDao
import com.example.travel_footprint_android.data.entity.Journey
import kotlinx.coroutines.flow.Flow
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
        title: String,
        style: String,
        description: String = "",
        coverImagePath: String = ""
    ): Long {
        val journey = Journey(
            title = title,
            description = description,
            startDate = Date(),
            endDate = Date(),
            coverStyle = style,
            coverImagePath = coverImagePath,
            journeyImagePaths = emptyList()
        )
        return journeyDao.insertJourney(journey)
    }

    suspend fun updateJourneyCover(journeyId: Long, imagePath: String) {
        val journey = journeyDao.getJourneyByIdSuspend(journeyId) ?: return // 如果找不到旅程就返回
            val updated = journey.copy(coverImagePath = imagePath)
            journeyDao.updateJourney(updated)

    }

    suspend fun deleteJourneyWithAllData(journeyId: Long) {
        // 外键设置为 CASCADE，会自动删除关联的足迹和位置
        journeyDao.deleteJourneyById(journeyId)
    }

    suspend fun searchJourneys(keyword: String): List<Journey> {
        return journeyDao.searchJourneys(keyword)
    }
}