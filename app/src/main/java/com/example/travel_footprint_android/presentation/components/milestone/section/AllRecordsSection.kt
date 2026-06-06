package com.example.travel_footprint_android.presentation.components.milestone.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation.components.milestone.MonthGroup
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * AllRecordsSection - 全部足迹记录列表
 *
 * 展示按月份分组的全部足迹记录
 * - AllRecordsSection：标题 + 无数据提示 / 月份分组列表
 * - MonthGroupSection：单个月份的分组区块（月份标题 + 记录列表）
 * - FootprintRecordItem：单条足迹记录卡片
 */

/**
 * "全部记录"展开区域
 *
 * @param monthGroups 按月份分组的足迹列表
 */
@Composable
internal fun AllRecordsSection(monthGroups: List<MonthGroup>) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0x99FFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // 区域标题
            Headline(
                text = "全部记录",
                fontSize = 16.sp,
                color = Color(0xFF1F2937)
            )
            // 无数据提示 vs 月份分组列表
            if (monthGroups.isEmpty()) {
                TextMedium(
                    text = "暂无足迹记录",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                Spacer(Modifier.height(12.dp))
                monthGroups.forEach { group ->
                    MonthGroupSection(group)
                }
            }
        }
    }
}

/**
 * 单个月份的足迹分组
 *
 * @param group 月份分组数据
 */
@Composable
private fun MonthGroupSection(group: MonthGroup) {
    // 计算该月总里程（米→公里）
    val groupTotalKm = group.footprints.sumOf { it.distance / 1000.0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // 月份标题行：月份名 + 弹性分隔线 + 当月总里程
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextMedium(
                text = group.monthLabel,
                fontSize = 15.sp,
                color = Color(0xFF374151)
            )
            Spacer(Modifier.width(8.dp))
            // 灰色弹性分隔线
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.width(8.dp))
            TextMedium(
                text = "${String.format("%.1f", groupTotalKm)} km",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
            )
        }

        // 遍历该月每个足迹记录
        LazyColumn(
            modifier = Modifier.heightIn(max = 600.dp)
        ) {
            items(group.footprints, key = { it.id }) { it ->
                FootprintRecordItem(it)
            }
        }
    }
}

/**
 * 单条足迹记录卡片
 *
 * @param footprint 足迹数据
 */
@Composable
private fun FootprintRecordItem(footprint: Footprint) {
    // 日期格式化（MM/dd HH:mm）
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    // 距离从米转为公里
    val distanceKm = footprint.distance / 1000.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .clickable { }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 蓝色圆点装饰
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF60A5FA))
            )
            Spacer(Modifier.width(12.dp))
            // 左侧：标题（单行省略）+ 开始时间
            Column(modifier = Modifier.weight(1f)) {
                TextMedium(
                    text = footprint.title,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                TextMedium(
                    text = dateFormat.format(footprint.startTime),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            // 右侧：里程数 + 单位
            Column(horizontalAlignment = Alignment.End) {
                TextMedium(
                    text = String.format("%.2f", distanceKm),
                    fontSize = 16.sp,
                    color = Color(0xFF3B82F6)
                )
                TextMedium(
                    text = "km",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
