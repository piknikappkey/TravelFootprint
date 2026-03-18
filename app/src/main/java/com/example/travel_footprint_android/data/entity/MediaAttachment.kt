// app/src/main/java/com/example/travel_footprint_android/data/entity/MediaAttachment.kt
package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.io.File
import java.util.Date

@Entity(
    tableName = "media_attachments",
    foreignKeys = [
        ForeignKey(
            entity = Footprint::class,
            parentColumns = ["id"],
            childColumns = ["footprintId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MediaAttachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                          // 多媒体编号
    val footprintId: Long,                       // 所属足迹编号
    val type: String,                            // 类型（photo/note/audio）
    val localPath: String,                        // 本地路径
    val thumbnailPath: String,                    // 缩略图路径
    val createTime: Date,                         // 创建时间
    val caption: String                           // 说明文字
) {
    fun getFile(): File = File(localPath)
}