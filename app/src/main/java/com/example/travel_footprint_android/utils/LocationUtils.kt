// app/src/main/java/com/example/travel_footprint_android/utils/LocationUtils.kt
package com.example.travel_footprint_android.utils

import android.location.Location
import kotlin.math.*

object LocationUtils {

    private const val EARTH_RADIUS = 6371000.0 // 地球半径（米）

    /**
     * 计算两点之间的距离（米）
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * 计算多点路径的总距离（米）
     */
    fun calculateTotalDistance(points: List<Pair<Double, Double>>): Float {
        if (points.size < 2) return 0f

        var total = 0f
        for (i in 0 until points.size - 1) {
            val (lat1, lon1) = points[i]
            val (lat2, lon2) = points[i + 1]
            total += calculateDistance(lat1, lon1, lat2, lon2)
        }
        return total
    }

    /**
     * 计算路径的中心点
     */
    fun calculateCenterPoint(points: List<Pair<Double, Double>>): Pair<Double, Double> {
        if (points.isEmpty()) return 0.0 to 0.0
        if (points.size == 1) return points[0]

        var latSum = 0.0
        var lonSum = 0.0

        points.forEach { (lat, lon) ->
            latSum += lat
            lonSum += lon
        }

        return latSum / points.size to lonSum / points.size
    }

    /**
     * 将度转换为弧度
     */
    private fun degToRad(deg: Double): Double = deg * PI / 180.0

    /**
     * 将弧度转换为度
     */
    private fun radToDeg(rad: Double): Double = rad * 180.0 / PI

    /**
     * 判断是否在中国境内
     */
    fun isInChina(lat: Double, lon: Double): Boolean {
        return lat in 0.0..55.0 && lon in 70.0..140.0
    }

    /**
     * 格式化距离
     */
    fun formatDistance(meters: Float): String {
        return when {
            meters < 1000 -> "${meters.toInt()}米"
            meters < 100000 -> "${"%.1f".format(meters / 1000)}公里"
            else -> "${meters.toInt() / 1000}公里"
        }
    }

    /**
     * 获取方位描述
     */
    fun getBearingDescription(bearing: Float): String {
        return when (bearing) {
            in 0.0..22.5, in 337.5..360.0 -> "北"
            in 22.5..67.5 -> "东北"
            in 67.5..112.5 -> "东"
            in 112.5..157.5 -> "东南"
            in 157.5..202.5 -> "南"
            in 202.5..247.5 -> "西南"
            in 247.5..292.5 -> "西"
            in 292.5..337.5 -> "西北"
            else -> "未知"
        }
    }
}