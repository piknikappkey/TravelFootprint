// app/src/main/java/com/example/travel_footprint_android/data/entity/City.kt
package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cities",
    foreignKeys = [
        ForeignKey(
            entity = Province::class,
            parentColumns = ["adcode"],
            childColumns = ["provinceAdcode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("provinceAdcode")]
)
data class City(
    @PrimaryKey
    val adcode: String,        // 城市代码 (如: 110100)
    val name: String,          // 城市名称 (如: 北京市)
    val provinceAdcode: String,// 所属省份代码
    val centerLat: Double,     // 中心纬度
    val centerLng: Double,     // 中心经度
    val sortOrder: Int = 0     // 排序顺序
)