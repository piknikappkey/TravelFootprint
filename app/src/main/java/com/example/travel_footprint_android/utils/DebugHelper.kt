// app/src/main/java/com/example/travel_footprint_android/utils/DebugHelper.kt
package com.example.travel_footprint_android.utils

import android.util.Log
import com.example.travel_footprint_android.data.repository.FootprintRepository
import com.example.travel_footprint_android.data.repository.JourneyRepository
import com.example.travel_footprint_android.domain.service.LocationService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugHelper @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val footprintRepository: FootprintRepository,
    private val locationService: LocationService
) {

    private val tag = "DB_DEBUG"

    /**
     * 测试获取所有旅程的足迹数量
     */
    suspend fun testFootprintCounts() {
        println("\n========== 测试足迹数量 ==========")
        Log.d(tag, "========== 测试足迹数量 ==========")

        // 1. 获取所有旅程
        val journeys = journeyRepository.getAllJourneys().first()

        if (journeys.isEmpty()) {
            println("⚠️ 没有旅程数据，请先创建旅程")
            Log.d(tag, "⚠️ 没有旅程数据，请先创建旅程")
            println("========== 测试结束 ==========\n")
            return
        }

        // 2. 获取所有旅程的足迹数量
        val counts = journeyRepository.getFootprintCounts()

        println("📊 足迹数量统计:")
        Log.d(tag, "📊 足迹数量统计:")

        journeys.forEach { journey ->
            val count = counts[journey.id] ?: 0
            println("   旅程 [${journey.id}] ${journey.title}: $count 个足迹")
            Log.d(tag, "   旅程 [${journey.id}] ${journey.title}: $count 个足迹")
        }

        println("========== 测试结束 ==========\n")
        Log.d(tag, "========== 测试结束 ==========")
    }

    /**
     * 测试单个旅程的足迹数量
     */
    suspend fun testSingleFootprintCount(journeyId: Long) {
        println("\n========== 测试单个足迹数量 ==========")
        Log.d(tag, "========== 测试单个足迹数量 ==========")

        val count = journeyRepository.getFootprintCount(journeyId)
        println("📊 旅程ID=$journeyId 的足迹数量: $count")
        Log.d(tag, "📊 旅程ID=$journeyId 的足迹数量: $count")

        println("========== 测试结束 ==========\n")
        Log.d(tag, "========== 测试结束 ==========")
    }

    /**
     * 查看所有旅程
     */
    suspend fun testGetAllJourneys() {
        println("\n========== 查看所有旅程 ==========")
        Log.d(tag, "========== 查看所有旅程 ==========")

        val journeys = journeyRepository.getAllJourneys().first()

        if (journeys.isEmpty()) {
            println("⚠️ 没有旅程数据")
            Log.d(tag, "⚠️ 没有旅程数据")
        } else {
            println("📊 共有 ${journeys.size} 个旅程:")
            Log.d(tag, "📊 共有 ${journeys.size} 个旅程:")
            journeys.forEach { journey ->
                println("   [${journey.id}] ${journey.title}")
                Log.d(tag, "   [${journey.id}] ${journey.title}")
            }
        }

        println("========== 查看结束 ==========\n")
        Log.d(tag, "========== 查看结束 ==========")
    }

    /**
     * 创建测试旅程
     */
    suspend fun testCreateJourney(title: String): Long {
        println("🔵 创建测试旅程: $title")
        Log.d(tag, "🔵 创建测试旅程: $title")

        val id = journeyRepository.createJourney(
            title = title,
            style = "watercolor",
            description = "测试旅程"
        )

        println("✅ 创建成功: id=$id")
        Log.d(tag, "✅ 创建成功: id=$id")
        return id
    }
    /**
     * 添加测试足迹
     */
    suspend fun addTestFootprint(journeyId: Long, lat: Double, lng: Double, notes: String): Long {
        println("🔵 添加测试足迹: journeyId=$journeyId, 位置=($lat, $lng)")

        val address = locationService.reverseGeocode(lat, lng)
        println("📍 地址: $address")

        val footprintId = footprintRepository.addFootprint(
            journeyId = journeyId,
            lat = lat,
            lng = lng,
            photos = null,
            notes = notes
        )

        println("✅ 足迹添加成功: id=$footprintId")
        return footprintId
    }

    /**
     * 批量添加测试足迹
     */
    suspend fun addMultipleTestFootprints() {
        println("\n========== 批量添加测试足迹 ==========")

        // 获取所有旅程
        val journeys = journeyRepository.getAllJourneys().first()

        if (journeys.isEmpty()) {
            println("⚠️ 没有旅程，请先创建旅程")
            return
        }

        // 为每个旅程添加测试足迹
        journeys.forEach { journey ->
            println("为旅程 [${journey.id}] ${journey.title} 添加足迹")

            // 添加3个测试足迹
            for (i in 1..3) {
                val lat = 39.9 + i * 0.01
                val lng = 116.4 + i * 0.01
                addTestFootprint(
                    journeyId = journey.id,
                    lat = lat,
                    lng = lng,
                    notes = "${journey.title}的足迹$i"
                )
            }
        }

        println("========== 批量添加完成 ==========\n")
    }
}