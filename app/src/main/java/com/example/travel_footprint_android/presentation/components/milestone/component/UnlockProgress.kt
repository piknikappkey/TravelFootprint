package com.example.travel_footprint_android.presentation.components.milestone.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.milestone.milestones
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

/**
 * 解锁进度概览（已解锁/总数）
 *
 * @param achievedMilestonesSize 已解锁成就数量
 */
@Composable
fun UnlockProgress(
    achievedMilestonesSize: Int
) {
    val totalCount = milestones.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextMedium(
            text = "已解锁: ${achievedMilestonesSize}/$totalCount",
            fontSize = 13.sp,
            color = Color(0xFF6B7280),
        )
    }
}
