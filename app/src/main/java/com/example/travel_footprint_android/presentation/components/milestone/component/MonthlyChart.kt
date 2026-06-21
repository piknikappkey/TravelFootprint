package com.example.travel_footprint_android.presentation.components.milestone.component

import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.milestone.MonthlyMileage

/**
 * MonthlyChart - 月度里程折线趋势图
 *
 * 基于 Canvas 自绘的月度里程折线趋势图，含呼吸发光动画、数据标签和虚线网格
 * - 平滑贝塞尔曲线连接各月数据点
 * - 呼吸发光动画增强视觉效果
 * - 显示数据标签和月份标签
 */

// ==================== 图表颜色常量 ====================

/** 折线图主线条颜色（亮蓝色） */
private val ChartLineColor = Color(0xFF60A5FA)
/** 折线图发光辉光颜色（浅蓝色，用于呼吸发光效果） */
private val ChartGlowColor = Color(0xFF93C5FD)
/** 折线图数据点颜色（亮蓝色） */
private val ChartPointColor = Color(0xFF60A5FA)
/** 折线图虚线网格颜色（半透明白色） */
private val ChartGridColor = Color(0x22FFFFFF)

// ==================== 图表组件 ====================

/**
 * 月度里程折线图
 *
 * @param monthlyData 月度里程数据列表
 * @param modifier 修饰符
 */
@Composable
internal fun MonthlyChart(
    monthlyData: List<MonthlyMileage>,
    modifier: Modifier = Modifier
) {
    // 呼吸发光动画：glowAlpha 在 0.3 ~ 0.8 之间无限循环
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

    // 计算最大 Y 值以确定缩放比例
    val maxValue = monthlyData.maxOfOrNull { it.distanceKm } ?: 1.0
    val safeMax = if (maxValue <= 0) 1.0f else maxValue.toFloat()
    // 图表上下内边距（顶部留空放标签，底部留空放月份名）
    val chartTopPadding = 20f
    val chartBottomPadding = 44f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val chartHeight = height - chartTopPadding - chartBottomPadding
        val points = monthlyData.size
        // 少于 2 个点无法绘制曲线，直接返回
        if (points < 2) return@Canvas

        // X 轴步长
        val stepX = width / (points - 1)

        // Y 轴缩放系数（最大值上浮 20% 留空间）
        val yScale = chartHeight / (safeMax * 1.2f)

        // 构造平滑贝塞尔曲线路径
        val path = Path()
        // 计算所有数据点的 Offset（x, y）坐标
        val dataPoints = monthlyData.mapIndexed { index, data ->
            val x = (index * stepX).toFloat()
            val y = chartTopPadding + chartHeight - (data.distanceKm.toFloat() * yScale)
            Offset(x, y)
        }

        // 使用 cubicTo 绘制平滑曲线（贝塞尔曲线）
        path.moveTo(dataPoints.first().x, dataPoints.first().y)
        for (i in 1 until dataPoints.size) {
            val prev = dataPoints[i - 1]
            val curr = dataPoints[i]
            val cpx1 = (prev.x + curr.x) / 2 // 控制点 X 取两点中点
            path.cubicTo(cpx1, prev.y, cpx1, curr.y, curr.x, curr.y)
        }

        // 填充路径：曲线 → 底部 → 起点形成闭合区域
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(dataPoints.last().x, height - chartBottomPadding)
        fillPath.lineTo(dataPoints.first().x, height - chartBottomPadding)
        fillPath.close()

        // 绘制渐变填充区域（从浅蓝到透明）
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

        // 绘制外层发光辉光线条（呼吸动画控制透明度）
        drawPath(
            path = path,
            color = ChartGlowColor.copy(alpha = glowAlpha * 0.4f),
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 绘制主线条（亮蓝色细线）
        drawPath(
            path = path,
            color = ChartLineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 绘制数据点（白色外圈 + 蓝色内点）
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

        // 在数据点上方绘制数值标签（仅大于 0 时显示）
        dataPoints.forEachIndexed { index, point ->
            if (monthlyData[index].distanceKm > 0) {
                val labelText = String.format("%.2f", monthlyData[index].distanceKm)
                val paint = Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    textSize = 22f
                    textAlign = Paint.Align.CENTER
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

        // 绘制 3 条水平虚线网格
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

        // 在底部绘制月份标签
        val labelPaint = Paint().apply {
            color = 0xAAFFFFFF.toInt()
            textSize = 18f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        monthlyData.forEachIndexed { index, data ->
            val x = index * stepX
            drawContext.canvas.nativeCanvas.drawText(
                data.monthLabel,
                x,
                height - 2.dp.toPx(),
                labelPaint
            )
        }
    }
}
