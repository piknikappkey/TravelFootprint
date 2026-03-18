// app/src/main/java/com/example/travel_footprint_android/data/entity/Tag.kt
package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                          // 标签编号
    val name: String,                            // 标签名称
    val color: String,                           // 标签颜色
    val usageCount: Int = 0                      // 使用次数
)