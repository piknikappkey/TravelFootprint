package com.example.travel_footprint_android.presentation.components.milestone

import com.example.travel_footprint_android.data.entity.Footprint

/**
 * MilestoneModels - 里程碑功能的数据模型定义
 *
 * 包含里程碑功能中使用的所有数据类和常量定义
 */

// ==================== 里程统计数据模型 ====================

/** 月度里程数据：月份标签 + 该月累计里程（km） */
data class MonthlyMileage(
    val monthLabel: String,
    val distanceKm: Double
)

/** 里程统计汇总数据：总公里数 + 近 6 个月月度里程明细 */
data class MileageData(
    val totalKm: Double,
    val monthlyData: List<MonthlyMileage>
)

// ==================== 月份分组模型 ====================

/** 按月份分组的足迹集合：月份标签 + 月份 Key(yyyyMM) + 该月所有足迹列表 */
data class MonthGroup(
    val monthLabel: String,
    val monthKey: String,
    val footprints: List<Footprint>
)

// ==================== 里程碑成就模型 ====================

/**
 * 成就解锁条件（密封类）
 *
 * 用于定义不同类型的成就解锁条件，支持扩展新的条件类型
 */
sealed class UnlockCondition {
    /** 基于已点亮省份数量解锁 */
    data class Province(val required: Int) : UnlockCondition()
    
    /** 基于累计里程（km）解锁 */
    data class Mileage(val requiredKm: Double) : UnlockCondition()
    
    /** 基于旅程数量解锁 */
    data class JourneyCount(val required: Int) : UnlockCondition()
    
    /** 基于足迹数量解锁 */
    data class FootprintCount(val required: Int) : UnlockCondition()
    
    /** 基于封面数量解锁 */
    data class CoverCount(val required: Int) : UnlockCondition()
    
    /** 基于图片数量解锁 */
    data class ImageCount(val required: Int) : UnlockCondition()
}

/** 里程碑成就定义：id / 名称 / 描述 / 解锁条件 / 图标名（默认奖杯） */
data class Milestone(
    val id: Int,
    val name: String,
    val description: String,
    val condition: UnlockCondition,
    val icon: String = "trophy"
)

/** 里程碑成就配置列表 */
val milestones = listOf(
    // 省份成就
    Milestone(1, "初出茅庐", "点亮第1个省份", UnlockCondition.Province(1)),
    Milestone(2, "走南闯北", "点亮10个省份", UnlockCondition.Province(10)),
    Milestone(3, "踏遍四方", "点亮15个省份", UnlockCondition.Province(20)),
    Milestone(4, "足迹天下", "点亮全部36个地区", UnlockCondition.Province(36)),
    
    // 里程成就
    Milestone(10, "初行者", "累计行走5公里", UnlockCondition.Mileage(5.0)),
    Milestone(11, "漫步者", "累计行走15公里", UnlockCondition.Mileage(20.0)),
    Milestone(12, "远足者", "累计行走40公里", UnlockCondition.Mileage(50.0)),
    Milestone(14, "征途者", "累计行走100公里", UnlockCondition.Mileage(100.0)),
    
    // 旅程数量成就
    Milestone(20, "初次启程", "创建第一个旅程", UnlockCondition.JourneyCount(1)),
    Milestone(21, "旅行达人", "创建5个旅程", UnlockCondition.JourneyCount(5)),
    Milestone(22, "旅行家", "创建20个旅程", UnlockCondition.JourneyCount(20)),
    
    // 足迹数量成就
    Milestone(30, "第一步", "创建一个足迹", UnlockCondition.FootprintCount(1)),
    Milestone(31, "探索者", "创建10个足迹", UnlockCondition.FootprintCount(10)),
    Milestone(32, "行者无疆", "创建30个足迹", UnlockCondition.FootprintCount(30)),
    
    // 封面数量成就
    Milestone(40, "画上封面", "创建第一张封面", UnlockCondition.CoverCount(1)),
    Milestone(41, "封面达人", "创建10张封面", UnlockCondition.CoverCount(10)),
    Milestone(42, "封面艺术家", "创建30张封面", UnlockCondition.CoverCount(30)),
    
    // 图片数量成就
    Milestone(50, "打卡！", "上传第一张图片", UnlockCondition.ImageCount(1)),
    Milestone(51, "我拍拍拍！", "上传30张图片", UnlockCondition.ImageCount(30)),
    Milestone(52, "摄影大师", "上传100张图片", UnlockCondition.ImageCount(100))
)
