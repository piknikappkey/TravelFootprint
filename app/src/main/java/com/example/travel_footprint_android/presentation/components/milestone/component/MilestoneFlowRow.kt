package com.example.travel_footprint_android.presentation.components.milestone.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.milestone.Milestone
import com.example.travel_footprint_android.presentation.components.milestone.UnlockCondition
import com.example.travel_footprint_android.presentation.components.milestone.card.MilestoneCard

/**
 * 成就卡片流式布局容器
 *
 * @param Milestones 成就列表
 * @param lightedProvinceCount 已点亮省份数量
 * @param totalKm 总里程
 * @param journeyCount 旅程数量
 * @param footprintCount 足迹数量
 * @param coverCount 封面数量
 * @param imageCount 图片数量
 * @param isAchieved 是否已解锁
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MilestoneFlowRow(
    Milestones: List<Milestone>,
    lightedProvinceCount: Int,
    totalKm: Double,
    journeyCount: Int = 0,
    footprintCount: Int = 0,
    coverCount: Int = 0,
    imageCount: Int = 0,
    isAchieved: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        FlowRow(
            modifier = Modifier
                .widthIn(min = 200.dp)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Milestones.forEach { milestone ->
                val progress = when (val condition = milestone.condition) {
                    is UnlockCondition.Province -> {
                        (lightedProvinceCount.toFloat() / condition.required).coerceIn(0f, 1f)
                    }
                    is UnlockCondition.Mileage -> {
                        (totalKm / condition.requiredKm).toFloat().coerceIn(0f, 1f)
                    }
                    is UnlockCondition.JourneyCount -> {
                        (journeyCount.toFloat() / condition.required).coerceIn(0f, 1f)
                    }
                    is UnlockCondition.FootprintCount -> {
                        (footprintCount.toFloat() / condition.required).coerceIn(0f, 1f)
                    }
                    is UnlockCondition.CoverCount -> {
                        (coverCount.toFloat() / condition.required).coerceIn(0f, 1f)
                    }
                    is UnlockCondition.ImageCount -> {
                        (imageCount.toFloat() / condition.required).coerceIn(0f, 1f)
                    }
                }
                MilestoneCard(
                    milestone = milestone,
                    isAchieved = isAchieved,
                    progress = progress
                )
            }
        }
    }
}
