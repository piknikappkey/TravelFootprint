/*
 * Line - 虚线分隔线组件
 *
 * 【用途】
 *  - 在页面或卡片中作为视觉分隔线使用，用于区分不同内容区块
 *  - 提供可定制的虚线样式，应用于列表项之间、标题与内容之间等场景
 *
 * 【功能】
 *  1. 虚线绘制：通过 Canvas 的 drawLine + PathEffect.dashPathEffect 绘制水平虚线
 *  2. 自定义样式：支持自定义颜色、虚线实段长度、间隔长度、线条粗细
 *  3. 自动撑宽：默认 fillMaxWidth 填满父容器宽度，高度由线条粗细决定
 *  4. 垂直居中：线条在 Canvas 容器内垂直居中绘制
 *
 * 【关联组件】
 *  - SecondColor1：主题色（浅暖色 #FFE9BC），作为默认线条颜色
 *  - 本组件不依赖其他项目内自定义组件，仅使用 Compose Canvas 原生 API
 *
 * 【简单实现逻辑】
 *  1. 使用 Canvas 创建一个与父容器等宽、高度为 thickness.dp 的绘制区域
 *  2. 在 Canvas 内从左侧边缘到右侧边缘绘制一条水平线，垂直居中于画布
 *  3. 通过 PathEffect.dashPathEffect 设置虚线模式：实段长度 dashLength、间隔长度 gapLength
 *  4. 线条的 strokeWidth 使用 thickness.dp.toPx() 转换为像素，确保物理显示一致性
 */
package com.example.travel_footprint_android.presentation2.components.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.SecondColor1

// 虚线分隔线组件：使用 Canvas 绘制一条可自定义样式的水平虚线
@Composable
fun Line(
    modifier: Modifier = Modifier.fillMaxWidth(), // 外部修饰符，默认填满父容器宽度
    color: Color = SecondColor1,                   // 线条颜色，默认使用主题浅暖色
    dashLength: Float = 18f,                       // 虚线实段长度（像素），默认18px
    gapLength: Float = 6f,                         // 虚线间隔长度（像素），默认6px
    thickness: Float = 1f,                         // 线条粗细（dp），默认1dp
) {
    // Canvas 画布：宽度由 modifier 决定，高度固定为线条粗细
    Canvas(
        modifier = modifier
            .height(thickness.dp)
    ) {
        // 在画布垂直居中位置绘制一条水平虚线：从左边缘到右边缘
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),     // 起点：画布左边缘、垂直居中
            end = Offset(size.width, size.height / 2), // 终点：画布右边缘、垂直居中
            strokeWidth = thickness.dp.toPx(),         // 线条宽度转换为像素
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength, gapLength), // [0]=实段长度, [1]=间隔长度
                phase = 0f                           // 起始相位偏移
            )
        )
    }
}