package com.example.travel_footprint_android.presentation.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.milestone.card.MileageSummaryCard
import com.example.travel_footprint_android.presentation.components.milestone.section.AllRecordsSection
import com.example.travel_footprint_android.presentation.components.milestone.section.MilestoneAchievementSection
import com.example.travel_footprint_android.presentation.viewmodel.MilestoneViewModel

/**
 * MilestoneContent - 里程碑与里程统计面板
 *
 * 纯 UI 组件，所有数据和状态由 MilestoneViewModel 统一管理。
 *
 * @param milestoneViewModel 里程碑 ViewModel，提供派生数据和 UI 状态
 */
@Composable
fun MilestoneContent(
    milestoneViewModel: MilestoneViewModel
) {
    // 从 ViewModel 收集状态
    val mileageData by milestoneViewModel.mileageData.collectAsState()
    val monthGroups by milestoneViewModel.monthGroups.collectAsState()
    val showAllRecords by milestoneViewModel.showAllRecords.collectAsState()
    val showAllUnachieved by milestoneViewModel.showAllUnachieved.collectAsState()
    val lightedProvinceCount by milestoneViewModel.lightedProvinceCount.collectAsState()
    val achievedMilestones by milestoneViewModel.achievedMilestones.collectAsState()
    val allUnachievedMilestones by milestoneViewModel.allUnachievedMilestones.collectAsState()
    val nextUnachievedMilestones by milestoneViewModel.nextUnachievedMilestones.collectAsState()
    val totalKm by milestoneViewModel.totalKm.collectAsState()
    
    // 新增状态
    val journeyCount by milestoneViewModel.journeyCount.collectAsState()
    val footprintCount by milestoneViewModel.footprintCount.collectAsState()
    val coverCount by milestoneViewModel.coverCount.collectAsState()
    val imageCount by milestoneViewModel.imageCount.collectAsState()
    val headExpanded by milestoneViewModel.headExpanded.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 6.dp)
    ) {
        MileageSummaryCard(
            mileageData = mileageData,
            showAllRecords = showAllRecords,
            onToggleAllRecords = { milestoneViewModel.toggleAllRecords() }
        )

        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(
            visible = showAllRecords,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            AllRecordsSection(monthGroups = monthGroups)
        }

        Spacer(Modifier.height(20.dp))

        MilestoneAchievementSection(
            lightedProvinceCount = lightedProvinceCount,
            achievedMilestones = achievedMilestones,
            allUnachievedMilestones = allUnachievedMilestones,
            nextUnachievedMilestones = nextUnachievedMilestones,
            totalKm = totalKm,
            journeyCount = journeyCount,
            footprintCount = footprintCount,
            coverCount = coverCount,
            imageCount = imageCount,
            showAllUnachieved = showAllUnachieved,
            onToggleShowAllUnachieved = { milestoneViewModel.toggleShowAllUnachieved() },
            headExpanded = headExpanded,
            onToggleHeadExpanded = { milestoneViewModel.toggleHeadExpanded() }
        )
    }
}
