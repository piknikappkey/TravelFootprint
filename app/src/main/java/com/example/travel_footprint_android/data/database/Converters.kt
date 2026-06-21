// app/src/main/java/com/example/travel_footprint_android/data/database/Converters.kt
package com.example.travel_footprint_android.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    companion object {
        val gson = Gson()
        private val listStringType = TypeToken.getParameterized(List::class.java, String::class.java).type
    }

    // Date 转换
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // List<String> 转换（用于 journeyImagePaths）
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        return gson.fromJson(value, listStringType)
    }
}
