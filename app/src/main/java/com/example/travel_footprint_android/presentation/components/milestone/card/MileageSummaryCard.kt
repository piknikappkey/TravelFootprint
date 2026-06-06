package com.example.travel_footprint_android.presentation.components.milestone.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation.components.milestone.MileageData
import com.example.travel_footprint_android.presentation.components.milestone.component.MonthlyChart
import com.example.travel_footprint_android.presentation.components.milestone.formatTotalKm
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

/**
 * MileageSummaryCard - 里程统计卡片
 *
 * 展示累计总里程和月度里程趋势图
 * - 左侧：累计总里程（大号数字）+ "全部记录"按钮
 * - 右侧：近 6 个月月度里程折线趋势图
 */

// ==================== 卡片颜色常量 ====================

/** 里程统计卡片渐变的起始色（深蓝黑色） */
private val CardGradientStart = Color(0xFF1A1A2E)
/** 里程统计卡片渐变的结束色（深蓝色） */
private val CardGradientEnd = Color(0xFF16213E)

// ==================== 卡片组件 ====================

/**
 * 累计里程统计卡片
 *
 * @param mileageData 里程统计数据
 * @param showAllRecords 是否显示全部记录
 * @param onToggleAllRecords 切换全部记录显示状态的回调
 */
@Composable
internal fun MileageSummaryCard(
    mileageData: MileageData,
    showAllRecords: Boolean,
    onToggleAllRecords: () -> Unit
) {
    // 深色渐变背景卡片
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(CardGradientStart, CardGradientEnd)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧区域（38%）：总里程 + 按钮
            Column(
                modifier = Modifier.weight(0.4f),
                horizontalAlignment = Alignment.Start
            ) {
                TextMedium(
                    text = "累计里程",
                    fontSize = 12.sp,
                    color = Color(0x99FFFFFF)
                )
                Spacer(Modifier.height(6.dp))
                // 总里程大号数字（自动换算万单位）
                TextMedium(
                    text = formatTotalKm(mileageData.totalKm),
                    fontSize = 36.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                TextMedium(
                    text = "公里",
                    fontSize = 12.sp,
                    color = Color(0x99FFFFFF),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(12.dp))
                // "全部记录" / "收起记录" 切换按钮
                ButtonMain(
                    onClick = onToggleAllRecords,
                    bgColor = Color(0xFF3B82F6),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    TextMedium(
                        text = if (showAllRecords) "收起记录" else "全部记录",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // 右侧区域（62%）：月度里程折线图
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .height(130.dp)
            ) {
                MonthlyChart(
                    monthlyData = mileageData.monthlyData,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
