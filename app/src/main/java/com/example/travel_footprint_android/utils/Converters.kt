// app/src/main/java/com/example/travel_footprint_android/utils/Converters.kt
package com.example.travel_footprint_android.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    private val gson = Gson()

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
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    // List<Double> 转换（用于坐标列表）
    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double>? {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<Double>>() {}.type
        return gson.fromJson(value, type)
    }

    // Map 转换
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        if (value.isNullOrBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }
}