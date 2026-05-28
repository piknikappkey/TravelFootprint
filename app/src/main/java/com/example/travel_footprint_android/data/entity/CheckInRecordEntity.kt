package com.example.travel_footprint_android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "check_in_records")
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityAdcode: String,
    val cityName: String,
    val note: String,
    val time: Date,
    val tags: List<String> = emptyList(),
    val photoPaths: List<String> = emptyList()
)