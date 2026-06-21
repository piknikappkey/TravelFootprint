package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.ai_assistant_dialog_for_footprint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.ai_assistant_dialog_for_footprint.components.AiFillFieldForFootprint
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * 足迹 AI 智能填写按钮组件
 *
 * 显示一个带有 AI 图标的按钮，点击后触发 AI 生成流程
 * 加载中时显示进度动画并禁用按钮
 *
 * @param isLoading 是否正在加载
 * @param selectedFields 当前选中的字段集合
 * @param onSelectionChange 字段选择变化回调
 * @param onClick 点击回调，参数为自定义提示词内容
 */
@Composable
fun AiGenerateButtonForFootprint(
    isLoading: Boolean,
    selectedFields: Set<AiFillFieldForFootprint>,
    onSelectionChange: (Set<AiFillFieldForFootprint>) -> Unit,
    onClick: (customPrompt: String) -> Unit,
) {
    var customPrompt by remember { mutableStateOf("") }

    AiActionButtonBase(
        label = "AI 智能填写：",
        buttonIcon = Icons.Default.AutoAwesome,
        buttonText = "自动生成",
        loadingText = "生成中...",
        tipText = "AI 生成功能会根据您的选择自动填充足迹内容，生成时间较长，请耐心等待约 1-2 分钟。",
        footerText = "注：内容由豆包(doubao-seed-2-0-pro)生成~",
        isLoading = isLoading,
        estimatedLoadTimeMs = 120_000L,
        autoShowTipOnClick = true,
        onButtonClick = { onClick(customPrompt) },
        extraContent = {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)) {
                TextMedium(
                    text = "需要填写的内容：",
                    color = if (customPrompt.isNotEmpty()) FontDark4 else MainColor3,
                    fontSize = 14.sp,
                )
                FieldSelectionRow(
                    isLoading = isLoading,
                    selectedFields = selectedFields,
                    onSelectionChange = onSelectionChange
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

/**
 * 字段选择行组件
 *
 * 使用 StyleChip 显示四个选项：标题、描述、地址、评分
 *
 * @param isLoading 是否正在加载（加载时禁用选择）
 * @param selectedFields 当前选中的字段集合
 * @param onSelectionChange 字段选择变化回调
 */
@Composable
private fun FieldSelectionRow(
    isLoading: Boolean,
    selectedFields: Set<AiFillFieldForFootprint>,
    onSelectionChange: (Set<AiFillFieldForFootprint>) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.weight(1f))
        AiFillFieldForFootprint.entries.forEach { field ->
            StyleChip(
                label = field.label,
                isSelected = field in selectedFields,
                onClick = {
                    val newSet = if (field in selectedFields) {
                        selectedFields - field
                    } else {
                        selectedFields + field
                    }
                    onSelectionChange(newSet)
                },
                enabled = !isLoading
            )
        }
        Spacer(Modifier.weight(1f))
    }
}
