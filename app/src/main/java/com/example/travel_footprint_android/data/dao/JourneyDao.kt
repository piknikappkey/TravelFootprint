package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.entity.Footprint
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {

    // ==================== 基础 CRUD ====================

    @Insert
    suspend fun insertJourney(journey: Journey): Long

    @Update
    suspend fun updateJourney(journey: Journey)

    @Delete
    suspend fun deleteJourney(journey: Journey)

    @Query("DELETE FROM journeys WHERE id = :journeyId")
    suspend fun deleteJourneyById(journeyId: Long)

    // ==================== 查询 ====================

    @Query("SELECT * FROM journeys ORDER BY startDate DESC")
    fun getAllJourneys(): Flow<List<Journey>>

    @Query("SELECT * FROM journeys WHERE id = :id")
    fun getJourneyById(id: Long): Flow<Journey>

    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getJourneyByIdSuspend(id: Long): Journey?

    @Query("SELECT * FROM journeys WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%'")
    suspend fun searchJourneys(keyword: String): List<Journey>

    // ==================== 旅程 + 足迹关联查询 ====================

    @Transaction
    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    fun getJourneyWithFootprints(journeyId: Long): Flow<JourneyWithFootprints>

    // ==================== 地址相关查询 ====================

    // 根据地址文本模糊查询
    @Query("SELECT * FROM journeys WHERE address LIKE '%' || :address || '%'")
    suspend fun getJourneysByAddress(address: String): List<Journey>

    // 获取所有有地址的旅程
    @Query("SELECT * FROM journeys WHERE address != ''")
    fun getAllJourneysWithAddress(): Flow<List<Journey>>

    // ==================== 经纬度相关查询 ====================

    // 根据经纬度范围查询（矩形区域）
    @Query("""
        SELECT * FROM journeys 
        WHERE latitude BETWEEN :minLat AND :maxLat 
        AND longitude BETWEEN :minLng AND :maxLng
        ORDER BY startDate DESC
    """)
    suspend fun getJourneysInBounds(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): List<Journey>

    // 获取附近旅程（基于经纬度距离）
    @Query("""
        SELECT * FROM journeys 
        WHERE latitude != 0.0 AND longitude != 0.0
        AND (latitude BETWEEN :minLat AND :maxLat)
        AND (longitude BETWEEN :minLng AND :maxLng)
        ORDER BY 
            ((latitude - :centerLat) * (latitude - :centerLat) + 
             (longitude - :centerLng) * (longitude - :centerLng)) ASC
        LIMIT :limit
    """)
    suspend fun getNearbyJourneys(
        centerLat: Double,
        centerLng: Double,
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        limit: Int = 20
    ): List<Journey>

    // 获取所有有经纬度的旅程（用于地图显示）
    @Query("SELECT * FROM journeys WHERE latitude != 0.0 AND longitude != 0.0")
    fun getAllJourneysWithCoordinates(): Flow<List<Journey>>

    // ==================== 统计查询 ====================

    /**
     * 获取旅程总数
     */
    @Query("SELECT COUNT(*) FROM journeys")
    suspend fun getJourneyCount(): Int

    /**
     * 获取有封面的旅程数量
     */
    @Query("SELECT COUNT(*) FROM journeys WHERE coverImagePath != ''")
    suspend fun getCoveredJourneyCount(): Int

    /**
     * 获取所有旅程（suspend版本，用于应用层计算图片数量）
     */
    @Query("SELECT * FROM journeys")
    suspend fun getAllJourneysSuspend(): List<Journey>
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