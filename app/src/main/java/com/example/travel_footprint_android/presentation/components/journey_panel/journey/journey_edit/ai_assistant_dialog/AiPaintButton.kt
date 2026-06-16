package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiActionButtonBase
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.CustomPromptInput
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.StyleChip
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * 涂鸦风格选项
 *
 * 每种风格对应一个显示名称和一段 AI 提示词片段，
 * 多选后将所有选中风格的提示词拼合为最终 prompt。
 *
 * @param label 显示在 UI 上的名称
 * @param promptFragment 对应的 AI 提示词片段
 */
enum class PaintStyle(val label: String, val promptFragment: String) {
    CRAYON("蜡笔", "蜡笔画风格，色彩鲜艳，笔触粗犷有质感"),
    DREAMY("梦幻", "梦幻风格，柔和色调，朦胧光影，充满浪漫气息"),
    DYNAMIC("灵动", "灵动风格，线条流畅，色彩明亮活泼，充满生命力"),
    DOODLE("涂鸦", "涂鸦风格，随性自由，手绘线条，俏皮有趣"),
    WATERCOLOR("水彩", "水彩画风格，色彩透明晕染，笔触柔和细腻"),
    PIXEL("像素", "像素风格，复古游戏画面，色彩分明的像素块")
}

/**
 * 构建组合提示词
 *
 * 将用户选中的风格提示词片段与基础指令拼合为完整的 AI prompt。
 * 未选择任何风格时，使用默认的手绘漫画风格。
 *
 * @param selectedStyles 用户选中的风格列表
 * @return 完整的 AI 提示词
 */
fun buildPaintPrompt(selectedStyles: Set<PaintStyle>): String {
    return if (selectedStyles.isEmpty()) {
        "把上传照片转化成一种可爱混乱的二次元蜡笔涂鸦插画风格。\n" +
        "整体像是彩铅+蜡笔+手账贴纸+MS Paint 涂鸦的结合。\n" +
        "使用：粗糙线条，抖动边缘，不均匀上色，彩铅叠色，蜡笔颗粒感，故意幼稚但很有灵气的画风。人物变成Q版萌系二次元风格，大眼睛，夸张表情，可爱活泼。背景加入大量doodle:\n" +
        "爱心，星星，糖果，笑脸云朵，小花，贴纸，游戏UI元素，乱涂乱画符号。\n" +
        "颜色以：粉色，蓝色，紫色，黄色，薄荷色内主。\n" +
        "整体氛围：可爱，混乱，梦幻，互联网kawail,Kawaii aesthetic 不要精致，不要高级商业插画感，不要真实渲染，不要干净线稿，保留\"手绘失败感\"和乱涂鸦感\""
    } else {
        val styleDescriptions = selectedStyles.joinToString("、") { it.promptFragment }
        "把上传照片转化成一种可爱混乱的手绘风插画风格。\n" +
        "整体像是${styleDescriptions}的结合。\n" +
        "使用：粗糙线条，抖动边缘，不均匀上色，故意幼稚但很有灵气的画风。人物变成Q版萌系二次元风格，大眼睛，夸张表情，可爱活泼。背景加入大量doodle:\n" +
        "爱心，星星，糖果，笑脸云朵，小花，贴纸，游戏UI元素，箭头，乱涂乱画符号等可爱元素符号。\n" +
        "颜色以：粉色，蓝色，紫色，黄色，薄荷色内主。\n" +
        "整体氛围：可爱，混乱，梦幻，互联网kawail,Kawaii aesthetic 不要精致，不要高级商业插画感，不要真实渲染，不要干净线稿，保留\"手绘失败感\"和乱涂鸦感\""
    }
}

/**
 * AI 封面涂鸦按钮组件
 *
 * 包含：
 * 1. 自定义提示词输入区域（可输入自定义要求）
 * 2. 风格多选标签区域（可选多种风格组合）
 * 3. 涂鸦按钮（加载中时显示进度动画并禁用）
 * 4. 提示说明和进度条
 *
 * @param isLoading 是否正在加载
 * @param onPaintWithPrompt 点击回调，传入组合后的提示词
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPaintButton(
    isLoading: Boolean,
    onPaintWithPrompt: (String) -> Unit,
) {
    // 用户选中的风格集合
    var selectedStyles by remember { mutableStateOf(emptySet<PaintStyle>()) }
    // 用户自定义提示词
    var customPrompt by remember { mutableStateOf("") }

    AiActionButtonBase(
        label = "AI 封面涂鸦：",
        buttonIcon = Icons.Default.Brush,
        buttonText = "开始涂鸦",
        loadingText = "涂鸦中...",
        tipText = "选择喜欢的风格（可多选组合），点击「开始涂鸦」生成效果。生成时间较长，请耐心等待 3-5 分钟。",
        footerText = "注：由豆包(doubao-seedream)生成~",
        isLoading = isLoading,
        onButtonClick = {
            val prompt = if (customPrompt.isNotBlank()) {
                "把上传照片按提示词要求转化，使用以下提示词：${customPrompt}"
            } else {
                buildPaintPrompt(selectedStyles)
            }
            onPaintWithPrompt(prompt)
        },
        extraContent = {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)) {
                TextMedium(
                    text = "预设提示词：",
                    color = if (customPrompt.isNotEmpty()) FontDark4 else MainColor3,
                    fontSize = 14.sp,
                )
                // 风格多选标签区域
                FieldSelectionFlow(
                    isLoading = isLoading,
                    selectedStyles = selectedStyles,
                    onSelectionChange = { sel -> selectedStyles = sel }
                )

                // 自定义提示词输入区域
                CustomPromptInput(
                    customPrompt = customPrompt,
                    onCustomPromptChange = { customPrompt = it }
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldSelectionFlow(
    isLoading: Boolean,
    selectedStyles: Set<PaintStyle>,
    onSelectionChange: (Set<PaintStyle>) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .widthIn(min = 100.dp)
                .padding(horizontal = 8.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            PaintStyle.entries.forEach { style ->
                val isSelected = style in selectedStyles
                StyleChip(
                    label = style.label,
                    isSelected = isSelected,
                    onClick = {
                        onSelectionChange(
                            if (isSelected) {
                                selectedStyles - style
                            } else {
                                selectedStyles + style
                            }
                        )
                    },
                    enabled = !isLoading
                )
            }
        }
    }
}
