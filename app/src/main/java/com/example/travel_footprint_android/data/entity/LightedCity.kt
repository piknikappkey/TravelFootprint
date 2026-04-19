// app/src/main/java/com/example/travel_footprint_android/data/entity/LightedCity.kt
package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 点亮城市实体类
 *
 * 对应 china_all_data.json 中的城市数据结构
 */
@Entity(tableName = "lighted_cities")
data class LightedCity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityAdcode: String,        // 城市行政区划代码 (如: 110100)
    val cityName: String,          // 城市名称 (如: 北京市)
    val provinceAdcode: String,    // 所属省份代码 (如: 110000)
    val provinceName: String,      // 所属省份名称 (如: 北京市)
    val lightedTime: Date,         // 点亮时间
    val latitude: Double,          // 城市中心纬度
    val longitude: Double,         // 城市中心经度
    val remark: String = ""        // 备注
) {
    fun getFormattedTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(lightedTime)
    }
}