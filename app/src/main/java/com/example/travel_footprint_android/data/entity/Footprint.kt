package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.text.SimpleDateFormat
import java.util.Date

@Entity(
    tableName = "footprints",
    foreignKeys = [
        ForeignKey(
            entity = Journey::class,
            parentColumns = ["id"],
            childColumns = ["journeyId"],
            onDelete = ForeignKey.CASCADE  // 删除旅程时同时删除足迹
        )
    ]
)
data class Footprint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                          // 足迹编号
    val journeyId: Long,                         // 所属旅程编号
    val title: String,                           // 标题
    val description: String,                      // 描述
    val createTime: Date,                         // 创建时间
    val address: String,                          // 地址
    val rating: Int,                              // 个人评分（1-5）
    val startTime: Date = Date(),                 // 活动开始时间
    val duration: Long = 0L,                      // 持续时间（毫秒）
    val distance: Double = 0.0,                   // 移动距离（米）
    val speed: Double = 0.0,                      // 移动速度（米/秒）
    val calories: Double = 0.0                    // 消耗卡路里（千卡）
) {
    fun getFormattedTime(): String {
        // 格式化时间，可以用 SimpleDateFormat
        return SimpleDateFormat("yyyy-MM-dd HH:mm").format(createTime)
    }
}