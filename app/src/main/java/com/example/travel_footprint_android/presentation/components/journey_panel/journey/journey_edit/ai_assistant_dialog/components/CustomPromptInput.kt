package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FFRuanMengChuLianTi
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.MainColor3
import com.example.travel_footprint_android.ui.theme.SecondColor4

/**
 * 自定义提示词输入组件
 *
 * 包含标签文字和文本输入框，标签颜色根据输入内容动态变化
 *
 * @param customPrompt 当前自定义提示词
 * @param onCustomPromptChange 提示词变化回调
 */
@Composable
fun CustomPromptInput(
    title: String = "自定义提示词",
    customPrompt: String,
    onCustomPromptChange: (String) -> Unit,
) {
    TextMedium(
        text = "${title}：",
        color = if (customPrompt.isNotEmpty()) MainColor3 else FontDark4,
        fontSize = 14.sp,
    )
    InputText3(
        value = customPrompt,
        onValueChange = onCustomPromptChange,
        tipText = "输入${title}（可选）",
        padding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        imageVector = Icons.Default.Edit,
        textStyle = TextStyle(
            color = SecondColor4,
            fontSize = 15.sp,
            fontFamily = FFRuanMengChuLianTi
        )
    )
}