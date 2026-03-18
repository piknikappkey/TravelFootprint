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

    @Insert
    suspend fun insertFootprint(footprint: Footprint): Long

    @Update
    suspend fun updateFootprint(footprint: Footprint)

    @Delete
    suspend fun deleteFootprint(footprint: Footprint)

    @Query("DELETE FROM footprints WHERE journeyId = :journeyId")
    suspend fun deleteFootprintsByJourney(journeyId: Long)

    @Query("SELECT * FROM footprints WHERE journeyId = :journeyId ORDER BY createTime")
    fun getFootprintsByJourney(journeyId: Long): Flow<List<Footprint>>

    @Query("SELECT * FROM footprints WHERE createTime BETWEEN :start AND :end ORDER BY createTime")
    suspend fun getFootprintsByDateRange(start: Date, end: Date): List<Footprint>

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


