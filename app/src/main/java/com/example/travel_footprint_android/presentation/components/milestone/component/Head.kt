package com.example.travel_footprint_android.presentation.components.milestone.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.light_panel2.LightPanel2Tab
import com.example.travel_footprint_android.presentation.components.milestone.IcExpandButton
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

/**
 * 里程碑成就标题 + 省份点亮进度 + 展开统计详情
 *
 * @param lightedProvinceCount 已点亮省份数量
 * @param expanded 是否展开统计详情
 * @param onToggleExpand 切换展开/收起状态
 * @param totalKm 总里程
 * @param journeyCount 旅程数量
 * @param footprintCount 足迹数量
 * @param coverCount 封面数量
 * @param imageCount 图片数量
 */
@Composable
fun Head(
    lightedProvinceCount: Int,
    expanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    totalKm: Double = 0.0,
    journeyCount: Int = 0,
    footprintCount: Int = 0,
    coverCount: Int = 0,
    imageCount: Int = 0
) {
    Column {
        // 里程碑成就标题 + 省份点亮进度 + 展开按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row {
                    Headline(
                        text = "里程碑成就",
                        fontSize = 16.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(Modifier.width(5.dp))
                    // 展开/收起按钮
                    IcExpandButton(
                        expanded = expanded,
                        onClick = onToggleExpand
                    )
                }
                Spacer(Modifier.height(2.dp))
                TextMedium(
                    text = "已点亮 $lightedProvinceCount/${LightPanel2Tab.TOTAL_PROVINCE_COUNT} 个省份",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    firstLine = 1,
                )
            }
        }

        // 展开的统计详情
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // 统计数据列表
                StatItem(label = "累计行走", value = String.format("%.1f", totalKm), unit = "公里")
                StatItem(label = "创建旅程", value = "$journeyCount", unit = "个")
                StatItem(label = "足迹数量", value = "$footprintCount", unit = "个")
                StatItem(label = "封面数量", value = "$coverCount", unit = "张")
                StatItem(label = "图片数量", value = "$imageCount", unit = "张")
            }
        }
    }
}

/**
 * 统计数据项
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextMedium(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            firstLine = 1,
        )
        Spacer(Modifier.width(8.dp))
        TextMedium(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF3B82F6),
            firstLine = 1,
        )
        Spacer(Modifier.width(4.dp))
        TextMedium(
            text = unit,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            firstLine = 1,
        )
    }
}
