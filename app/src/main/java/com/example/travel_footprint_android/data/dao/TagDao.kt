// app/src/main/java/com/example/travel_footprint_android/data/dao/TagDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Tag
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.FootprintTagCrossRef

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY usageCount DESC")
    suspend fun getAllTags(): List<Tag>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchTags(keyword: String): List<Tag>

    @Insert
    suspend fun insertFootprintTagCrossRef(crossRef: FootprintTagCrossRef)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN footprint_tag_cross_ref f ON t.id = f.tagId
        WHERE f.footprintId = :footprintId
    """)
    suspend fun getTagsByFootprint(footprintId: Long): List<Tag>

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagWithFootprints(tagId: Long): TagWithFootprints

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE id = :tagId")
    suspend fun incrementTagUsage(tagId: Long)

    @Query("UPDATE tags SET usageCount = usageCount - 1 WHERE id = :tagId")
    suspend fun decrementTagUsage(tagId: Long)
}

//data class FootprintWithTags(
//    @Embedded val footprint: Footprint,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "id",
//        associateBy = Junction(
//            FootprintTagCrossRef::class,
//            parentColumn = "footprintId",
//            entityColumn = "tagId"
//        )
//    )
//    val tags: List<Tag>
//)
data class TagWithFootprints(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FootprintTagCrossRef::class,
            parentColumn = "tagId",
            entityColumn = "footprintId"
        )
    )
    val footprints: List<Footprint>
)
