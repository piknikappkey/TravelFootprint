package com.example.travel_footprint_android.presentation.components.light_panel2.milestone

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.presentation.components.light_panel2.LightPanel2Tab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyMileage(
    val monthLabel: String,
    val distanceKm: Double
)

data class MileageData(
    val totalKm: Double,
    val monthlyData: List<MonthlyMileage>
)

private fun calculateMileageFromFootprints(footprints: List<Footprint>): MileageData {
    Log.d("MilestoneContent", "calculateMileageFromFootprints called with ${footprints.size} footprints")
    if (footprints.isNotEmpty()) {
        val totalDist = footprints.sumOf { it.distance }
        Log.d("MilestoneContent", "Total distance in DB: $totalDist m, footprints: ${footprints.map { "${it.id}=${it.distance}m" }}")
    }

    if (footprints.isEmpty()) {
        val months = (0 until 6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            MonthlyMileage(
                monthLabel = SimpleDateFormat("M月", Locale.getDefault()).format(cal.time),
                distanceKm = 0.0
            )
        }.reversed()
        return MileageData(totalKm = 0.0, monthlyData = months)
    }

    val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
    val monthLabelFormat = SimpleDateFormat("M月", Locale.getDefault())

    val monthlyDistances = mutableMapOf<String, MutableList<Double>>()
    var totalKm = 0.0

    for (footprint in footprints) {
        val distanceKm = footprint.distance / 1000.0
        totalKm += distanceKm
        val monthKey = dateFormat.format(footprint.startTime)
        monthlyDistances.getOrPut(monthKey) { mutableListOf() }.add(distanceKm)
    }

    val monthlyData = (0 until 6).map { i ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -(5 - i))
        val key = dateFormat.format(cal.time)
        val distances = monthlyDistances[key] ?: emptyList()
        MonthlyMileage(
            monthLabel = monthLabelFormat.format(cal.time),
            distanceKm = distances.sum()
        )
    }

    return MileageData(totalKm = totalKm, monthlyData = monthlyData)
}

data class MonthGroup(
    val monthLabel: String,
    val monthKey: String,
    val footprints: List<Footprint>
)

private fun groupFootprintsByMonth(footprints: List<Footprint>): List<MonthGroup> {
    if (footprints.isEmpty()) return emptyList()

    val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())
    val monthLabelFormat = SimpleDateFormat("yyyy年M月", Locale.getDefault())

    val grouped = footprints
        .sortedByDescending { it.startTime }
        .groupBy { dateFormat.format(it.startTime) }

    return grouped.entries.map { (key, list) ->
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(key) ?: Date()
        MonthGroup(
            monthLabel = monthLabelFormat.format(cal.time),
            monthKey = key,
            footprints = list
        )
    }.sortedByDescending { it.monthKey }
}

private val CardGradientStart = Color(0xFF1A1A2E)
private val CardGradientEnd = Color(0xFF16213E)

private val ChartLineColor = Color(0xFF60A5FA)
private val ChartGlowColor = Color(0xFF93C5FD)
private val ChartPointColor = Color(0xFF60A5FA)
private val ChartGridColor = Color(0x22FFFFFF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MilestoneContent(
    lightCityList: List<com.example.travel_footprint_android.data.entity.LightedCity>,
    lightedProvinceCount: Int,
    allFootprints: List<Footprint>
) {
    val mileageData = remember(allFootprints) {
        calculateMileageFromFootprints(allFootprints)
    }

    val monthGroups = remember(allFootprints) {
        groupFootprintsByMonth(allFootprints)
    }

    var showAllRecords by remember { mutableStateOf(false) }

    val achievedMilestones = remember(lightedProvinceCount) {
        milestones.filter { it.requiredProvinces <= lightedProvinceCount }
    }
    val unachievedMilestones = remember(lightedProvinceCount) {
        milestones.filter { it.requiredProvinces > lightedProvinceCount }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MileageSummaryCard(
            mileageData = mileageData,
            showAllRecords = showAllRecords,
            onToggleAllRecords = { showAllRecords = !showAllRecords }
        )

        AnimatedVisibility(
            visible = showAllRecords,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            AllRecordsSection(monthGroups = monthGroups)
        }

        Spacer(Modifier.height(20.dp))

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
                        progress = (lightedProvinceCount.toFloat() / milestone.requiredProvinces.toFloat()).coerceAtMost(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AllRecordsSection(monthGroups: List<MonthGroup>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "全部记录",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Spacer(Modifier.height(12.dp))

        if (monthGroups.isEmpty()) {
            Text(
                text = "暂无足迹记录",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            monthGroups.forEach { group ->
                MonthGroupSection(group)
            }
        }
    }
}

@Composable
private fun MonthGroupSection(group: MonthGroup) {
    val groupTotalKm = group.footprints.sumOf { it.distance / 1000.0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.monthLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${String.format("%.1f", groupTotalKm)} km",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }

        group.footprints.forEach { footprint ->
            FootprintRecordItem(footprint)
        }
    }
}

@Composable
private fun FootprintRecordItem(footprint: Footprint) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
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
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF60A5FA))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = footprint.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(footprint.startTime),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f", distanceKm),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
                Text(
                    text = "km",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun MileageSummaryCard(
    mileageData: MileageData,
    showAllRecords: Boolean,
    onToggleAllRecords: () -> Unit
) {
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
            Column(
                modifier = Modifier.weight(0.38f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "累计里程",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0x99FFFFFF)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatTotalKm(mileageData.totalKm),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "公里",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0x99FFFFFF),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onToggleAllRecords,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (showAllRecords) "收起记录" else "全部记录",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(0.62f)
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

private fun formatTotalKm(km: Double): String {
    return when {
        km >= 10000 -> String.format("%.1f", km / 10000) + "万"
        km >= 1000 -> String.format("%.0f", km)
        km >= 100 -> String.format("%.0f", km)
        else -> String.format("%.1f", km)
    }
}

@Composable
private fun MonthlyChart(
    monthlyData: List<MonthlyMileage>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chartGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val maxValue = monthlyData.maxOfOrNull { it.distanceKm } ?: 1.0
    val safeMax = if (maxValue <= 0) 1.0f else maxValue.toFloat()
    val chartTopPadding = 20f
    val chartBottomPadding = 44f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val chartHeight = height - chartTopPadding - chartBottomPadding
        val points = monthlyData.size
        if (points < 2) return@Canvas

        val stepX = width / (points - 1)

        val yScale = chartHeight / (safeMax * 1.2f)

        val path = Path()
        val dataPoints = monthlyData.mapIndexed { index, data ->
            val x = (index * stepX).toFloat()
            val y = chartTopPadding + chartHeight - (data.distanceKm.toFloat() * yScale)
            Offset(x, y)
        }

        path.moveTo(dataPoints.first().x, dataPoints.first().y)
        for (i in 1 until dataPoints.size) {
            val prev = dataPoints[i - 1]
            val curr = dataPoints[i]
            val cpx1 = (prev.x + curr.x) / 2
            path.cubicTo(cpx1, prev.y, cpx1, curr.y, curr.x, curr.y)
        }

        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(dataPoints.last().x, height - chartBottomPadding)
        fillPath.lineTo(dataPoints.first().x, height - chartBottomPadding)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    ChartLineColor.copy(alpha = 0.15f),
                    ChartLineColor.copy(alpha = 0.0f)
                ),
                startY = chartTopPadding,
                endY = height - chartBottomPadding
            )
        )

        drawPath(
            path = path,
            color = ChartGlowColor.copy(alpha = glowAlpha * 0.4f),
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawPath(
            path = path,
            color = ChartLineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        dataPoints.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = ChartPointColor,
                radius = 3.dp.toPx(),
                center = point
            )
        }

        dataPoints.forEachIndexed { index, point ->
            if (monthlyData[index].distanceKm > 0) {
                val labelText = String.format("%.2f", monthlyData[index].distanceKm)
                val paint = android.graphics.Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    point.x,
                    point.y - 12.dp.toPx(),
                    paint
                )
            }
        }

        val gridLines = 3
        for (i in 0..gridLines) {
            val y = chartTopPadding + chartHeight * (1f - i.toFloat() / gridLines)
            drawLine(
                color = ChartGridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 0.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
            )
        }

        val labelPaint = android.graphics.Paint().apply {
            color = 0xAAFFFFFF.toInt()
            textSize = 18f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        monthlyData.forEachIndexed { index, data ->
            val x = index * stepX
            drawContext.canvas.nativeCanvas.drawText(
                data.monthLabel,
                x,
                height - 10.dp.toPx(),
                labelPaint
            )
        }
    }
}

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

data class Milestone(
    val id: Int,
    val name: String,
    val description: String,
    val requiredProvinces: Int,
    val icon: String = "trophy"
)

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    isAchieved: Boolean,
    progress: Float
) {
    val bgColor = if (isAchieved) Color(0xFFEFF6FF) else Color(0xFFF3F4F6)

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
            color = if (isAchieved) Color(0xFF6B7280) else Color(0xFFE5E7EB),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isAchieved) 1f else progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isAchieved) Color(0xFF3B82F6)
                        else Color(0xFF93C5FD)
                    )
            )
        }
    }
}
