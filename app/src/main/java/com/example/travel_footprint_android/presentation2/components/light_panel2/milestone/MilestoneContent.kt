package com.example.travel_footprint_android.presentation2.components.light_panel2.milestone

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2Tab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0

private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
}

private fun Double.pow(exp: Int): Double {
    var result = 1.0
    repeat(exp) { result *= this }
    return result
}

data class MonthlyMileage(
    val monthLabel: String,
    val distanceKm: Double
)

data class MileageData(
    val totalKm: Double,
    val monthlyData: List<MonthlyMileage>
)

private fun calculateMileage(cities: List<LightedCity>): MileageData {
    if (cities.size < 2) {
        val total = 0.0
        val months = (0 until 6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            MonthlyMileage(
                monthLabel = SimpleDateFormat("M月", Locale.getDefault()).format(cal.time),
                distanceKm = 0.0
            )
        }.reversed()
        return MileageData(totalKm = total, monthlyData = months)
    }

    val sorted = cities.sortedBy { it.lightedTime }
    val calendar = Calendar.getInstance()
    val sixMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -5) }
    sixMonthsAgo.set(Calendar.DAY_OF_MONTH, 1)
    sixMonthsAgo.set(Calendar.HOUR_OF_DAY, 0)
    sixMonthsAgo.set(Calendar.MINUTE, 0)
    sixMonthsAgo.set(Calendar.SECOND, 0)
    sixMonthsAgo.set(Calendar.MILLISECOND, 0)
    val sixMonthsAgoTime = sixMonthsAgo.time

    val dateFormat = SimpleDateFormat("yyyyMM", Locale.getDefault())

    val monthlyDistances = mutableMapOf<String, Double>()
    var totalKm = 0.0

    for (i in 1 until sorted.size) {
        val prev = sorted[i - 1]
        val curr = sorted[i]
        val dist = haversineDistance(prev.latitude, prev.longitude, curr.latitude, curr.longitude)
        totalKm += dist
        val monthKey = dateFormat.format(curr.lightedTime)
        monthlyDistances[monthKey] = (monthlyDistances[monthKey] ?: 0.0) + dist
    }

    val monthlyData = (0 until 6).map { i ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, - (5 - i))
        val key = dateFormat.format(cal.time)
        MonthlyMileage(
            monthLabel = SimpleDateFormat("M月", Locale.getDefault()).format(cal.time),
            distanceKm = monthlyDistances[key] ?: 0.0
        )
    }

    return MileageData(totalKm = totalKm.roundToInt().toDouble(), monthlyData = monthlyData)
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
    lightCityList: List<LightedCity>,
    lightedProvinceCount: Int
) {
    val mileageData = remember(lightCityList) {
        calculateMileage(lightCityList)
    }

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
        MileageSummaryCard(mileageData = mileageData)

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
private fun MileageSummaryCard(mileageData: MileageData) {
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
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "全部记录",
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
        else -> String.format("%.0f", km)
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
    val chartBottomPadding = 28f

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

        val peakIndex = monthlyData.indices.maxByOrNull { monthlyData[it].distanceKm }
        if (peakIndex != null && monthlyData[peakIndex].distanceKm > 0) {
            val peakPoint = dataPoints[peakIndex]
            val labelText = String.format("%.0f", monthlyData[peakIndex].distanceKm)
            val paint = android.graphics.Paint().apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                peakPoint.x,
                peakPoint.y - 12.dp.toPx(),
                paint
            )
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
                height - 4.dp.toPx(),
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
