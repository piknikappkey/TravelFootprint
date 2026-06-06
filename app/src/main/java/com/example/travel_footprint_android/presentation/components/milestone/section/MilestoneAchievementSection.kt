package com.example.travel_footprint_android.presentation.components.milestone.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.milestone.Milestone
import com.example.travel_footprint_android.presentation.components.milestone.component.Head
import com.example.travel_footprint_android.presentation.components.milestone.component.UnlockProgress
import com.example.travel_footprint_android.presentation.components.milestone.component.MilestoneFlowRow
import com.example.travel_footprint_android.presentation.components.text.headline.Headline

/**
 * 里程碑成就卡片区域
 *
 * @param lightedProvinceCount 已点亮省份数量
 * @param achievedMilestones 已解锁成就列表
 * @param allUnachievedMilestones 全部未解锁成就列表
 * @param nextUnachievedMilestones 下一项待解锁成就列表
 * @param totalKm 总里程
 * @param journeyCount 旅程数量
 * @param footprintCount 足迹数量
 * @param coverCount 封面数量
 * @param imageCount 图片数量
 * @param showAllUnachieved 是否展开全部未解锁成就
 * @param onToggleShowAllUnachieved 切换展开/收起状态
 * @param headExpanded 是否展开Head统计详情
 * @param onToggleHeadExpanded 切换Head展开/收起状态
 */
@Composable
fun MilestoneAchievementSection(
    lightedProvinceCount: Int,
    achievedMilestones: List<Milestone>,
    allUnachievedMilestones: List<Milestone>,
    nextUnachievedMilestones: List<Milestone>,
    totalKm: Double,
    journeyCount: Int = 0,
    footprintCount: Int = 0,
    coverCount: Int = 0,
    imageCount: Int = 0,
    showAllUnachieved: Boolean,
    onToggleShowAllUnachieved: () -> Unit,
    headExpanded: Boolean = false,
    onToggleHeadExpanded: () -> Unit = {}
) {
    BGImgBox(
        R.drawable.bg_rectangular_1__2__2, R.drawable.bg_rectangular_1__2__3,
        modifier = Modifier
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // 标题
            Head(
                lightedProvinceCount = lightedProvinceCount,
                expanded = headExpanded,
                onToggleExpand = onToggleHeadExpanded,
                totalKm = totalKm,
                journeyCount = journeyCount,
                footprintCount = footprintCount,
                coverCount = coverCount,
                imageCount = imageCount
            )

            Spacer(Modifier.height(8.dp))

            // 解锁进度概览（已解锁/总数）
            UnlockProgress(achievedMilestones.size)

            Spacer(Modifier.height(8.dp))

            // 已解锁成就列表（FlowRow 自动换行）
            if (achievedMilestones.isNotEmpty()) {
                Headline(
                    text = "已解锁成就",
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.padding(bottom = 8.dp),
                    letterSpacing = TextUnit.Unspecified
                )
                MilestoneFlowRow(
                    Milestones = achievedMilestones,
                    lightedProvinceCount = lightedProvinceCount,
                    totalKm = totalKm,
                    journeyCount = journeyCount,
                    footprintCount = footprintCount,
                    coverCount = coverCount,
                    imageCount = imageCount,
                    isAchieved = true
                )
            }

            // 未解锁成就列表（含分隔线）
            if (allUnachievedMilestones.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                UnachievedMilestonesSection(
                    allUnachievedMilestones = allUnachievedMilestones,
                    nextUnachievedMilestones = nextUnachievedMilestones,
                    lightedProvinceCount = lightedProvinceCount,
                    totalKm = totalKm,
                    journeyCount = journeyCount,
                    footprintCount = footprintCount,
                    coverCount = coverCount,
                    imageCount = imageCount,
                    showAll = showAllUnachieved,
                    onToggleShowAll = onToggleShowAllUnachieved
                )
            }
        }
    }
}
