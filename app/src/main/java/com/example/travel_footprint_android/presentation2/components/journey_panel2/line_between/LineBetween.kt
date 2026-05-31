package com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between

/*
 * ============================================================================
 * LineBetween.kt — 分段式虚线分隔线组件
 * ============================================================================
 *
 * 【用途】
 *   在旅程面板编辑界面中作为视觉分隔元素，在表单字段之间插入一条带
 *   上下间距的横向虚线，使布局层次更清晰。
 *
 * 【功能】
 *   1. 上下间距：通过 paddingUp / paddingDown 参数在虚线上下方分别
 *      插入空白间隔
 *   2. 横向虚线：居中绘制一条虚线，宽度占父容器的 95%（默认），
 *      通过 Line 组件（Canvas 自绘）实现虚线效果
 *   3. 高度可定制：虚线的颜色、实线段长度、间隔长度、线粗细、
 *      上下间距、整体宽度占比均可通过参数配置
 *
 * 【关联组件】
 *   - Line（components.line）：Canvas 自绘虚线组件，使用
 *     PathEffect.dashPathEffect 实现虚线效果
 *   - SecondColor2（ui.theme）：副色调金色（#FDD583），
 *     作为虚线的默认颜色（90% 透明度）
 *
 * 【简单实现逻辑】
 *   1. 先渲染一个高度为 paddingUp 的 Spacer，产生顶部间距
 *   2. 再渲染一个居中对齐的 Row，内部放置 Line 组件：
 *      - Line 通过 .fillMaxWidth(lineLength) 控制虚线宽度比例
 *      - Line 内部使用 Canvas.drawLine() + PathEffect.dashPathEffect
 *        绘制虚线
 *   3. 最后渲染一个高度为 paddingDown 的 Spacer，产生底部间距
 * ============================================================================
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.line.Line
import com.example.travel_footprint_android.ui.theme.SecondColor2

// —— 分段式虚线分隔线组件 ——
// 上下带间距的居中对齐虚线，用于表单布局中的视觉分隔
@Composable
fun LineBetween(
    // 虚线颜色，默认 SecondColor2（金色）的 90% 透明度
    color: Color = SecondColor2.copy(.9f),
    // 每个实线段的绘制长度（px 单位，传递给 PathEffect.dashPathEffect）
    dashLength: Float = 18f,
    // 实线段之间的间隔长度（px 单位）
    gapLength: Float = 9f,
    // 虚线的粗细（dp 单位）
    thickness: Float = 1.2f,
    // 虚线上方的空白间距
    paddingUp: Dp = 6.dp,
    // 虚线下方的空白间距
    paddingDown: Dp = 6.dp,
    // 虚线占父容器宽度的比例（0~1），默认 95%
    lineLength: Float = .95f,
) {
    // 顶部间距
    Spacer(Modifier.padding(paddingUp))
    // 居中对齐行，包裹虚线本身
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Canvas 自绘虚线，宽度按 lineLength 比例缩放
        Line(
            Modifier
                .fillMaxWidth(lineLength),
            color,
            dashLength,
            gapLength,
            thickness
        )
    }
    // 底部间距
    Spacer(Modifier.padding(paddingDown))
}