// app/src/main/java/com/example/travel_footprint_android/data/entity/Location.kt
package com.example.travel_footprint_android.data.entity

import android.graphics.PointF
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = Footprint::class,
            parentColumns = ["id"],
            childColumns = ["footprintId"],
            onDelete = ForeignKey.CASCADE  // 删除足迹时同时删除位置
        )
    ],
    indices = [androidx.room.Index(value = ["footprintId"])]
)
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                          // 坐标编号
    val footprintId: Long,                       // 所属足迹编号
    val latitude: Double,                         // 纬度
    val longitude: Double,                        // 经度
    @ColumnInfo(name = "idx")
    val index: Int = 0                            // 顺序索引（用于多个坐标点）
) {
    fun getLatLng(): PointF {
        return PointF(latitude.toFloat(), longitude.toFloat())
    }
}