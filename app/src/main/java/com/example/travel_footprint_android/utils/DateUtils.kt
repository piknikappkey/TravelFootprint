// app/src/main/java/com/example/travel_footprint_android/utils/DateUtils.kt
package com.example.travel_footprint_android.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    private val fullFormat = SimpleDateFormat(Constants.DATE_FORMAT_FULL, Locale.getDefault())
    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT_DATE, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(Constants.DATE_FORMAT_TIME, Locale.getDefault())

    /**
     * 格式化完整时间
     */
    fun formatFull(date: Date): String {
        return fullFormat.format(date)
    }

    /**
     * 格式化日期
     */
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    /**
     * 格式化时间
     */
    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }

    /**
     * 获取相对时间描述
     */
    fun getRelativeTimeDescription(date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}分钟前"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}小时前"
            diff < TimeUnit.DAYS.toMillis(2) -> "昨天"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}天前"
            else -> formatDate(date)
        }
    }

    /**
     * 计算两个日期之间的天数
     */
    fun daysBetween(start: Date, end: Date): Int {
        val diff = end.time - start.time
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    /**
     * 获取本周的开始时间
     */
    fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    /**
     * 获取本月的开始时间
     */
    fun getStartOfMonth(): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    /**
     * 字符串转日期
     */
    fun parseDate(dateStr: String, format: String = Constants.DATE_FORMAT_DATE): Date? {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}