// app/src/main/java/com/example/travel_footprint_android/data/entity/FootprintTagCrossRef.kt
package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "footprint_tag_cross_ref",
    primaryKeys = ["footprintId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Footprint::class,
            parentColumns = ["id"],
            childColumns = ["footprintId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FootprintTagCrossRef(
    val footprintId: Long,
    val tagId: Long
)