// app/src/main/java/com/example/travel_footprint_android/data/dao/JourneyDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.entity.Footprint
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {

    @Insert
    suspend fun insertJourney(journey: Journey): Long

    @Update
    suspend fun updateJourney(journey: Journey)

    @Delete
    suspend fun deleteJourney(journey: Journey)

    @Query("DELETE FROM journeys WHERE id = :journeyId")
    suspend fun deleteJourneyById(journeyId: Long)

    @Query("SELECT * FROM journeys ORDER BY startDate DESC")
    fun getAllJourneys(): Flow<List<Journey>>

    @Query("SELECT * FROM journeys WHERE id = :id")
    fun getJourneyById(id: Long): Flow<Journey>

    // 新增挂起函数版本
    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getJourneyByIdSuspend(id: Long): Journey?

    @Query("SELECT * FROM journeys WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%'")
    suspend fun searchJourneys(keyword: String): List<Journey>

    // 获取旅程及其所有足迹
    @Transaction
    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    fun getJourneyWithFootprints(journeyId: Long): Flow<JourneyWithFootprints>
}

// 返回类型：旅程 + 足迹列表
data class JourneyWithFootprints(
    @Embedded val journey: Journey,
    @Relation(
        parentColumn = "id",
        entityColumn = "journeyId"
    )
    val footprints: List<Footprint>
)