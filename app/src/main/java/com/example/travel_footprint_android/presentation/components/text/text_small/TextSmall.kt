/*
 * TextSmall - 小号文本组件
 *
 * 【用途】
 *  - 提供统一风格的小号文字展示，用于显示次要信息如日期、地址、数据标签等
 *  - 在整个应用中取代直接使用 Material3 Text，确保字体、颜色、缩进风格一致性
 *
 * 【功能】
 *  1. 主题字体：默认使用 FFRuanMengChuLianTi（软萌初恋体），全局统一视觉风格
 *  2. 首行缩进：通过 firstLine 参数控制首行缩进量（以字符数为单位），其余行不缩进
 *  3. 颜色默认值：默认使用 FontDark6（#666666 中灰色），视觉上比正文弱化
 *  4. 字体大小：默认 12.sp，比 TextMedium（16.sp）更小，适合辅助信息
 *  5. 行数控制：支持 minLines / maxLines 控制文本占位和截断
 *  6. 对齐方式：支持文本对齐方式自定义
 *
 * 【关联组件】
 *  - FontDark6：主题色（中灰色 #666666），作为默认文字颜色
 *  - FFRuanMengChuLianTi：软萌初恋体字体族，应用全局的圆润可爱字体风格
 *  - 被 FootprintListItem、FootprintListPanel 等列表/面板组件广泛使用
 *
 * 【简单实现逻辑】
 *  1. 封装 Material3 Text 组件，固定设置 fontWeight = FontWeight.W300（细体）
 *  2. 通过 TextStyle 设置 fontFamily 为 FFRuanMengChuLianTi
 *  3. firstLine 参数乘以 fontSize 得到首行缩进的像素值（firstLine * fontSize）
 *     - 例如 firstLine=2, fontSize=12.sp → 首行缩进24.sp
 *  4. 其余参数直接透传给 Text 组件
 */
package com.example.travel_footprint_android.presentation.components.text.text_small

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
import androidx.compose.ui.unit.times
import com.example.travel_footprint_android.ui.theme.FFRuanMengChuLianTi
import com.example.travel_footprint_android.ui.theme.FontDark6

// 小号文本组件：封装 Material3 Text，统一使用主题字体和颜色
@Composable
fun TextSmall(
    text: String,                          // 显示的文本内容
    color: Color = FontDark6,              // 文字颜色，默认中灰色（#666666）
    firstLine: Int = 0,                    // 首行缩进字符数（0=不缩进）
    fontSize: TextUnit = 12.sp,            // 字体大小，默认12sp
    minLines: Int = 1,                     // 最少行数，保证占位高度
    maxLines: Int = Int.MAX_VALUE,         // 最大行数，超出截断
    textAlign: TextAlign = TextAlign.Start, // 文本对齐方式
    modifier: Modifier = Modifier           // 外部修饰符
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.W300,      // 细体字重，与主题字体风格匹配
        textAlign = textAlign,
        minLines = minLines,
        maxLines = maxLines,
        style = TextStyle(
            textIndent = TextIndent(
                firstLine = firstLine * fontSize, // 首行缩进 = 字符数 × 字号
                restLine = 0.sp                   // 其余行不缩进
            ),
            fontFamily = FFRuanMengChuLianTi,     // 软萌初恋体
        )
    )
}