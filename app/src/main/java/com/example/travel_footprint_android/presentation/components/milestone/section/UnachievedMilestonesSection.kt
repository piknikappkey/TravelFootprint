package com.example.travel_footprint_android.presentation.components.milestone.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.milestone.IcExpandButton
import com.example.travel_footprint_android.presentation.components.milestone.Milestone
import com.example.travel_footprint_android.presentation.components.milestone.component.MilestoneFlowRow
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

/**
 * 未解锁成就区域
 *
 * @param allUnachievedMilestones 全部未解锁成就列表
 * @param nextUnachievedMilestones 下一项待解锁成就列表（每种条件类型门槛最低的一项）
 * @param lightedProvinceCount 已点亮省份数量
 * @param totalKm 总里程
 * @param journeyCount 旅程数量
 * @param footprintCount 足迹数量
 * @param coverCount 封面数量
 * @param imageCount 图片数量
 * @param showAll 是否展开全部未解锁成就
 * @param onToggleShowAll 切换展开/收起状态
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnachievedMilestonesSection(
    allUnachievedMilestones: List<Milestone>,
    nextUnachievedMilestones: List<Milestone>,
    lightedProvinceCount: Int,
    totalKm: Double,
    journeyCount: Int = 0,
    footprintCount: Int = 0,
    coverCount: Int = 0,
    imageCount: Int = 0,
    showAll: Boolean,
    onToggleShowAll: () -> Unit
) {
    // 浅灰色分隔线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0xFFE5E7EB))
    )
    Spacer(Modifier.height(16.dp))

    // "未解锁成就" 标题 + 展开/收起按钮
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextMedium(
            text = "未解锁成就",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        IcExpandButton(
            expanded = showAll,
            onClick = onToggleShowAll
        )
    }

    // 根据展开状态显示不同的列表
    val displayMilestones = if (showAll) allUnachievedMilestones else nextUnachievedMilestones
    Column(
        modifier = Modifier
            .heightIn(max = 200.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MilestoneFlowRow(
            Milestones = displayMilestones,
            lightedProvinceCount = lightedProvinceCount,
            totalKm = totalKm,
            journeyCount = journeyCount,
            footprintCount = footprintCount,
            coverCount = coverCount,
            imageCount = imageCount,
            isAchieved = false
        )
    }
}
