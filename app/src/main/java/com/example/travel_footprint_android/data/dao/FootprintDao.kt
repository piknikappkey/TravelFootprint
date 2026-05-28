// app/src/main/java/com/example/travel_footprint_android/data/dao/FootprintDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.data.entity.Tag
import com.example.travel_footprint_android.data.entity.FootprintTagCrossRef
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FootprintDao {
    //插入足迹
    @Insert
    suspend fun insertFootprint(footprint: Footprint): Long

    //更新足迹
    @Update
    suspend fun updateFootprint(footprint: Footprint)

    // 方式1：按对象删除
    @Delete
    suspend fun deleteFootprint(footprint: Footprint)

    // 方式2：按ID删除（推荐）
    @Query("DELETE FROM footprints WHERE id = :footprintId")
    suspend fun deleteFootprintById(footprintId: Long)

    //删除旅程足迹
    @Query("DELETE FROM footprints WHERE journeyId = :journeyId")
    suspend fun deleteFootprintsByJourney(journeyId: Long)

    //查询旅程足迹
    @Query("SELECT * FROM footprints WHERE journeyId = :journeyId ORDER BY createTime")
    fun getFootprintsByJourney(journeyId: Long): Flow<List<Footprint>>

    //查询足迹通过日期
    @Query("SELECT * FROM footprints WHERE createTime BETWEEN :start AND :end ORDER BY createTime")
    suspend fun getFootprintsByDateRange(start: Date, end: Date): List<Footprint>

    //通过喜欢查询足迹
    @Query("SELECT * FROM footprints WHERE rating >= :minRating")
    suspend fun getFootprintsByRating(minRating: Int): List<Footprint>

    // 获取足迹及其所有多媒体
    @Transaction
    @Query("SELECT * FROM footprints WHERE id = :footprintId")
    fun getFootprintWithMedia(footprintId: Long): Flow<FootprintWithMedia>

    // 获取足迹及其位置
    @Transaction
    @Query("SELECT * FROM footprints WHERE id = :footprintId")
    fun getFootprintWithLocation(footprintId: Long): Flow<FootprintWithLocation>

    // 获取足迹及其标签
    @Transaction
    @Query("SELECT * FROM footprints WHERE id = :footprintId")
    fun getFootprintWithTags(footprintId: Long): Flow<FootprintWithTags>


    /**
     * 获取单个旅程的足迹数量
     */
    @Query("SELECT COUNT(*) FROM footprints WHERE journeyId = :journeyId")
    suspend fun getFootprintCountByJourney(journeyId: Long): Int

    /**
     * 获取所有旅程的足迹数量（批量查询）
     */
    @Query("""
        SELECT journeyId, COUNT(*) as count 
        FROM footprints 
        GROUP BY journeyId
    """)
    suspend fun getFootprintCountsByJourney(): List<FootprintCount>

    // 用于批量查询的结果类
    data class FootprintCount(
        val journeyId: Long,
        val count: Int
    )

    @Query("SELECT * FROM footprints WHERE id = :footprintId")
    suspend fun getFootprintById(footprintId: Long): Footprint?


}

data class FootprintWithMedia(
    @Embedded val footprint: Footprint,
    @Relation(
        parentColumn = "id",
        entityColumn = "footprintId"
    )
    val media: List<MediaAttachment>
)

data class FootprintWithLocation(
    @Embedded val footprint: Footprint,
    @Relation(
        parentColumn = "id",
        entityColumn = "footprintId"
    )
    val locations: List<Location>
)

// 修复 FootprintWithTags
data class FootprintWithTags(
    @Embedded val footprint: Footprint,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FootprintTagCrossRef::class,
            parentColumn = "footprintId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)


