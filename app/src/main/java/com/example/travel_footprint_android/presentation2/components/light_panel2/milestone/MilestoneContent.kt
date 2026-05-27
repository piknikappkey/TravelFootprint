package com.example.travel_footprint_android.presentation2.components.light_panel2.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2Tab

data class Milestone(
    val id: Int,
    val name: String,
    val description: String,
    val requiredProvinces: Int,
    val icon: String = "trophy"
)

private val milestones = listOf(
    Milestone(1, "初出茅庐", "点亮第1个省份", 1),
    Milestone(2, "初露锋芒", "点亮5个省份", 5),
    Milestone(3, "走南闯北", "点亮10个省份", 10),
    Milestone(4, "行万里路", "点亮15个省份", 15),
    Milestone(5, "踏遍四方", "点亮20个省份", 20),
    Milestone(6, "纵横四海", "点亮25个省份", 25),
    Milestone(7, "大江南北", "点亮30个省份", 30),
    Milestone(8, "足迹天下", "点亮全部34个省份", 34)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MilestoneContent(
    lightedProvinceCount: Int
) {
    val achievedMilestones = remember(lightedProvinceCount) {
        milestones.filter { it.requiredProvinces <= lightedProvinceCount }
    }
    val unachievedMilestones = remember(lightedProvinceCount) {
        milestones.filter { it.requiredProvinces > lightedProvinceCount }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "里程碑成就",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "已点亮 $lightedProvinceCount/${LightPanel2Tab.TOTAL_PROVINCE_COUNT} 个省份",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val totalCount = milestones.size

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已解锁: ${achievedMilestones.size}/$totalCount",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        // 已达成成就
        if (achievedMilestones.isNotEmpty()) {
            Text(
                text = "已解锁成就",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                achievedMilestones.forEach { milestone ->
                    MilestoneCard(
                        milestone = milestone,
                        isAchieved = true,
                        progress = 1f
                    )
                }
            }
        }

        // 未达成成就
        if (unachievedMilestones.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "未解锁成就",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                unachievedMilestones.forEach { milestone ->
                    MilestoneCard(
                        milestone = milestone,
                        isAchieved = false,
                        progress = (lightedProvinceCount.toFloat() / milestone.requiredProvinces).coerceAtMost(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    isAchieved: Boolean,
    progress: Float
) {
    val bgColor = if (isAchieved) Color(0xFFEFF6FF) else Color(0xFFF3F4F6)
    val textColor = if (isAchieved) Color(0xFF1F2937) else Color(0xFFD1D5DB)
    val descColor = if (isAchieved) Color(0xFF6B7280) else Color(0xFFE5E7EB)

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isAchieved) Color(0xFF3B82F6) else Color(0xFFD1D5DB)),
            contentAlignment = Alignment.Center
        ) {
            if (isAchieved) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "成就",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "未解锁",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = milestone.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAchieved) Color(0xFF1F2937) else Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = milestone.description,
            fontSize = 11.sp,
            color = descColor,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isAchieved) Color(0xFFBFDBFE) else Color(0xFFE5E7EB))
        ) {
            if (!isAchieved) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF93C5FD))
                )
            }
        }

        if (!isAchieved) {
            Spacer(Modifier.height(4.dp))
            val percent = (progress * 100).toInt()
            Text(
                text = "进度 $percent%",
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}