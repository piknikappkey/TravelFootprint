package com.example.travel_footprint_android.presentation.components.milestone

import android.util.Log
import com.example.travel_footprint_android.data.entity.Footprint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * MilestoneUtils - 里程碑功能的工具函数
 *
 * 包含里程计算、数据分组和格式化等工具方法
 */

// ==================== 里程计算工具函数 ====================

/**
 * 根据足迹列表计算里程统计数据
 * - 从 Footprint.distance（米）转换为公里
 * - 按月份聚合近 6 个月的月度里程
 * - 足迹为空时返回全零数据
 */
internal fun calculateMileageFromFootprints(footprints: List<Footprint>): MileageData {
    // 调试日志：打印输入的足迹数量和总距离
    Log.d("MilestoneContent", "calculateMileageFromFootprints called with ${footprints.size} footprints")
    if (footprints.isNotEmpty()) {
        val totalDist = footprints.sumOf { it.distance }
        Log.d("MilestoneContent", "Total distance in DB: $totalDist m, footprints: ${footprints.map { "${it.id}=${it.distance}m" }}")
    }

    // 足迹为空 → 返回近 6 个月全零数据
    if (footprints.isEmpty()) {
        val months = (0 until 6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            MonthlyMileage(
                monthLabel = SimpleDateFormat("M月", Locale.getDefault()).format(cal.time),
                distanceKm = 0.0
            )
        }.reversed() // reversed() 使月份从旧到新排列
        return MileageData(totalKm = 0.0, monthlyData = months)
    }

    // 日期格式化器：yyyyMM 用于分组 Key，M月 用于显示标签
    val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
    val monthLabelFormat = SimpleDateFormat("M月", Locale.getDefault())

    // 按月聚合距离数据：key=yyyyMM，value=该月所有里程列表
    val monthlyDistances = mutableMapOf<String, MutableList<Double>>()
    var totalKm = 0.0

    // 遍历每个足迹，将 distance（米）转为公里后按月份归类
    for (footprint in footprints) {
        val distanceKm = footprint.distance / 1000.0
        totalKm += distanceKm
        val monthKey = dateFormat.format(footprint.startTime)
        monthlyDistances.getOrPut(monthKey) { mutableListOf() }.add(distanceKm)
    }

    // 生成近 6 个月的月度里程（包括无数据的月份补 0）
    val monthlyData = (0 until 6).map { i ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -(5 - i)) // 从 5 个月前到当前月
        val key = dateFormat.format(cal.time)
        val distances = monthlyDistances[key] ?: emptyList()
        MonthlyMileage(
            monthLabel = monthLabelFormat.format(cal.time),
            distanceKm = distances.sum()
        )
    }

    return MileageData(totalKm = totalKm, monthlyData = monthlyData)
}

// ==================== 月份分组工具函数 ====================

/**
 * 将足迹列表按月份（yyyyMM）分组并按时间降序排列
 * - 按 startTime 倒序排列
 * - 每个分组返回 MonthGroup（含月份标签和足迹列表）
 */
internal fun groupFootprintsByMonth(footprints: List<Footprint>): List<MonthGroup> {
    // 空列表直接返回空
    if (footprints.isEmpty()) return emptyList()

    // 日期格式化器：yyyyMM 用于分组 Key，yyyy年M月 用于显示标签
    val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
    val monthLabelFormat = SimpleDateFormat("yyyy年M月", Locale.getDefault())

    // 按 startTime 降序排列后，按月份分组
    val grouped = footprints
        .sortedByDescending { it.startTime }
        .groupBy { dateFormat.format(it.startTime) }

    // 将分组结果转为 MonthGroup 列表，并按月份 Key 降序（最新的月份在前）
    return grouped.entries.map { (key, list) ->
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(key) ?: Date()
        MonthGroup(
            monthLabel = monthLabelFormat.format(cal.time),
            monthKey = key,
            footprints = list
        )
    }.sortedByDescending { it.monthKey }
}

// ==================== 格式化工具函数 ====================

/**
 * 将公里数格式化为可读字符串
 * - >= 10000 km → "x.x万"（如 1.2万）
 * - >= 100 km → 整数（如 1234）
 * - < 100 km → 保留一位小数（如 88.5）
 */
internal fun formatTotalKm(km: Double): String {
    return when {
        km >= 10000 -> String.format("%.1f", km / 10000) + "万"
        km >= 1000 -> String.format("%.0f", km)
        km >= 100 -> String.format("%.0f", km)
        else -> String.format("%.1f", km)
    }
}
