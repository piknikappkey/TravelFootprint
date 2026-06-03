/*
 * 文件名：Headline.kt
 * 包路径：presentation2.components.text.headline
 *
 * 【用途】
 * 应用统一的标题文字组件。封装 Material3 Text 组件，提供预设的标题级排版样式，
 * 确保应用中所有标题/大标题文字保持一致的字体、间距和视觉风格。
 * 用于弹窗标题、页面标题、分区标题等需要突出显示的场景。
 *
 * 【功能】
 * 1. 标题渲染：基于 Material3 Text 组件展示标题文本
 * 2. 字体定制：固定使用 FFDaMengKaTongTi（大萌卡通体），营造可爱/亲和的视觉调性
 * 3. 样式预设：提供合理的默认值（20sp 字号、W500 字重、3sp 字间距、36sp 行高），
 *    调用方可按需覆盖
 * 4. 颜色灵活：默认深灰色 FontDark4（#444444），可通过 color 参数自定义
 * 5. 对齐方式：默认左对齐，支持居中对齐（Center）或右对齐（End）等
 *
 * 【关联组件】
 * - ConfirmDeleteDialog : 使用 Headline 显示删除确认弹窗的标题
 * - AddIcon             : 使用 Headline 显示"选择图片"标题
 * - InputText3/5        : 表单组件中使用 Headline 作为输入框上方的字段标签
 * - JourneyEdit         : 使用 Headline 显示"修改旅程"/"新建旅程"等页面大标题
 * - JourneyList3        : 使用 Headline 作为列表区域标题
 * - TextMedium          : 配套的正文文字组件（FFRuanMengChuLianTi 字体，更轻的字重）
 * - Type.kt             : 主题字体定义文件，FFDaMengKaTongTi 在此声明
 * - Color.kt            : 主题颜色定义文件，FontDark4 = #444444 在此声明
 *
 * 【简单实现逻辑】
 * 1. 接收 8 个参数：text（必填，显示的文本内容）、
 *    modifier（修饰符）、fontSize（字号，默认 20sp）、
 *    color（颜色，默认 FontDark4 深灰）、
 *    letterSpacing（字间距，默认 3sp 较宽间距）、
 *    lineHeight（行高，默认 36sp 宽松行距）、
 *    textAlign（对齐方式，默认 Start 左对齐）、
 *    fontWeight（字重，默认 W500 中等粗细）
 * 2. 内部创建 Material3 Text 组件，将参数依次传入
 * 3. 通过 style 参数设置 TextStyle，固定使用 FFDaMengKaTongTi 字体
 * 4. 所有参数均有合理默认值，调用方只需传入 text 即可获得标准标题样式，
 *    也可按需覆盖任何参数
 *
 * 【设计意图】
 * 应用包含多套自定义字体，Headline 专门服务于"标题"层级，
 * 使用大萌卡通体（圆润可爱）强调视觉主次关系，
 * 与 TextMedium（软萌初恋体，正文层级）形成字体层次对比
 */
package com.example.travel_footprint_android.presentation.components.text.headline

import androidx.compose.material3.Text // Material3 文字组件，提供主题化文本渲染
import androidx.compose.runtime.Composable // Compose 可组合函数注解
import androidx.compose.ui.Modifier // Compose 修饰符，用于布局/样式调整
import androidx.compose.ui.graphics.Color // 颜色类型，支持 ARGB 颜色值
import androidx.compose.ui.text.TextStyle // 文字样式组合（字体、行高等）
import androidx.compose.ui.text.font.FontWeight // 字体粗细（W100-W900）
import androidx.compose.ui.text.style.TextAlign // 文本对齐方式（Start/Center/End）
import androidx.compose.ui.unit.TextUnit // 文字尺寸单位（sp/em）
import androidx.compose.ui.unit.sp // sp 尺寸扩展（缩放无关像素）
import com.example.travel_footprint_android.ui.theme.FFDaMengKaTongTi // 大萌卡通体（可爱风格卡通字体）
import com.example.travel_footprint_android.ui.theme.FontDark4 // 深灰色 #444444，标题默认文字颜色

@Composable
fun Headline(
    text: String, // 必填：显示的标题文本内容
    modifier: Modifier = Modifier, // 外部修饰符，用于设置边距/宽高/点击等
    fontSize: TextUnit = 20.sp, // 字号，默认 20sp（大号标题级别）
    color: Color = FontDark4, // 文字颜色，默认深灰色 #444444
    letterSpacing: TextUnit = 3.sp, // 字间距，默认 3sp 较宽间距，增强标题可读性
    lineHeight: TextUnit = 36.sp, // 行高，默认 36sp（约为字号 1.8 倍，宽松舒适）
    textAlign: TextAlign? = TextAlign.Start, // 对齐方式，默认左对齐
    fontWeight: FontWeight = FontWeight.W500, // 字重，默认 W500 中等粗细
) {
    // 渲染 Material3 Text 组件，传入所有样式参数
    Text(
        text = text, // 标题文本内容
        modifier = modifier, // 外部修饰符
        color = color, // 文字颜色
        fontSize = fontSize, // 字体大小
        fontWeight = fontWeight, // 字体粗细
        textAlign = textAlign, // 文本对齐方式
        letterSpacing = letterSpacing, // 字间距
        lineHeight = lineHeight, // 行高
        // 通过 TextStyle 设置字体族为大萌卡通体（项目专属可爱风格字体）
        style = TextStyle(
            fontFamily = FFDaMengKaTongTi
        )
    )
}