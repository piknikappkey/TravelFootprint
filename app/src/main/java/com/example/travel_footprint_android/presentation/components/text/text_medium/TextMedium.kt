package com.example.travel_footprint_android.presentation.components.text.text_medium

/*
 * ============================================================================
 * TextMedium.kt — 可复用中等文字组件（软萌初恋体）
 * ============================================================================
 *
 * 【用途】
 *   提供统一风格的正文/中等大小文字渲染，封装了项目自定义字体
 *   （软萌初恋体）和默认文字样式，被全局多个页面和组件引用。
 *
 * 【功能】
 *   1. 自定义字体：默认使用 FFRuanMengChuLianTi（软萌初恋体）字体族
 *   2. 首行缩进：通过 firstLine 参数控制首行缩进字符数（0 = 不缩进），
 *      基于 16.sp 为基准计算缩进宽度
 *   3. 高度可定制：文字颜色、字号、行数限制、对齐方式、Modifier、
 *      完整 TextStyle 均可通过参数覆盖
 *   4. 统一字重：默认 FontWeight.W300（细体），与项目整体字体风格一致
 *
 * 【关联组件】
 *   - Text（androidx.compose.material3）：底层 Material3 文字渲染组件
 *   - FFRuanMengChuLianTi（ui.theme.Type.kt）：软萌初恋体 FontFamily，
 *     引用自 R.font.ruan_meng_chu_lian_ti 字体资源文件
 *   - FontDark4（ui.theme.Color.kt）：默认字体颜色 #444444 深灰色
 *   - TextIndent（compose.ui.text）：首行缩进样式控制
 *
 * 【简单实现逻辑】
 *   1. 接收 text（必填字符串）及一系列可选样式参数
 *   2. 构造默认 TextStyle：
 *      - textIndent = 根据 firstLine 参数计算首行缩进（firstLine * 16.sp）
 *      - fontFamily = FFRuanMengChuLianTi
 *   3. 调用 Material3 Text 组合函数，传入所有参数：
 *      - 颜色默认 FontDark4，字号默认 16.sp，字重默认 W300
 *      - 可设置最大/最少行数和文本对齐方式
 *   4. 外部调用者可通过 style 参数传入完整 TextStyle 覆盖默认样式
 * ============================================================================
 */

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.ui.theme.FFRuanMengChuLianTi
import com.example.travel_footprint_android.ui.theme.FontDark4

// —— 中等文字组件 ——
// 统一风格的正文文本渲染，使用软萌初恋体，支持首行缩进和各项样式自定义
@Composable
fun TextMedium(
    // 要显示的文本内容（必填）
    text: String,
    // 文字颜色，默认 FontDark4（深灰色 #444444）
    color: Color = FontDark4,
    // 首行缩进的字符数（0 = 不缩进），基于 16.sp 计算实际缩进宽度
    firstLine: Int = 0,
    // 字体大小，默认 16.sp
    fontSize: TextUnit = 16.sp,
    // 最大显示行数，超出部分截断，默认无限制
    maxLines: Int = Int.MAX_VALUE,
    // 最小显示行数，默认至少 1 行
    minLines: Int = 1,
    // 文本水平对齐方式，默认左对齐
    textAlign: TextAlign = TextAlign.Start,
    // 外部 Modifier，用于父组件控制布局
    modifier: Modifier = Modifier,
    // 完整文本样式，默认使用软萌初恋体 + 首行缩进
    style: TextStyle = TextStyle(
        textIndent = TextIndent(
            firstLine = (firstLine * 16).sp,
            restLine = 0.sp
        ),
        fontFamily = FFRuanMengChuLianTi,
    ),
) {
    // 委托给 Material3 Text 进行实际的文本渲染
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.W300,
        textAlign = textAlign,
        maxLines = maxLines,
        minLines = minLines,
        style = style
    )
}