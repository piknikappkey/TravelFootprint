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
        title: String,
        style: String,
        description: String = "",
        startDate: Date = Date(),
        endDate: Date = Date(),
        coverImagePath: String = "",
        journeyImagePaths: List<String> = emptyList()
    ): Long {
        val journey = Journey(
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            coverStyle = style,
            coverImagePath = coverImagePath,
            journeyImagePaths = journeyImagePaths
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
}