package com.example.travel_footprint_android.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "journeys")
data class Journey(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                          // 旅程编号
    var title: String,                          // 标题
    val description: String,                     // 描述
    val startDate: Date,                         // 开始日期
    val endDate: Date,                           // 结束日期
    val coverStyle: String,                      // 封面风格
    var coverImagePath: String,                  // 封面图片路径
    var journeyImagePaths: List<String>          // 旅程图片路径列表
) {
    fun getDuration(): Int {
        return ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getFootprintCount(): Int = 0  // 实际会通过查询获取

    fun getCoverImage(): String = coverImagePath
}