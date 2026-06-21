// app/src/main/java/com/example/travel_footprint_android/data/entity/Province.kt
package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provinces")
data class Province(
    @PrimaryKey
    val adcode: String,        // 省份代码 (如: 110000)
    val name: String,          // 省份名称 (如: 北京市)
    val centerLat: Double,     // 中心纬度
    val centerLng: Double,     // 中心经度
    val sortOrder: Int = 0     // 排序顺序
)