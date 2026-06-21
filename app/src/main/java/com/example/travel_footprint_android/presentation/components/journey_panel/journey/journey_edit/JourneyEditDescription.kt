package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit

/**
 * 旅程编辑描述组件
 *
 * 用途：
 * - 在旅程编辑页面中提供旅程描述文字输入区域
 * - 属于旅程面板(journey_panel2) → 旅程编辑(journey_edit) → 描述(description) 层级中的表单组件
 *
 * 功能：
 * - 展示"旅程描述："文字标签，提示用户输入旅程的详细描述
 * - 提供一个带聚焦视觉反馈和字数限制的文本输入框
 * - 最大输入长度为 1024 个字符
 *
 * 关联组件：
 * - Journey(Room 实体): 包含 description 字段，存储旅程描述文本
 * - InputText3: 可复用文本输入组件，支持聚焦态边框切换、图标前缀、背景纹理、字数截断
 * - TextMedium: 自定义文本组件，使用 FFRuanMengChuLianTi 字体
 *
 * 实现逻辑：
 * - 使用 TextMedium 显示"旅程描述："标签，左侧留 15.dp 边距，无首行缩进
 * - 使用 Spacer 在标签和输入框之间保留 2.dp 间距
 * - 通过 journey.description 获取当前描述文字作为 InputText3 的初始值
 * - onValueChange 回调由外部传入，将输入变化同步到 ViewModel 或数据库
 * - InputText3 配置占位提示为"请填写旅程描述"，最大长度 1024 字
 */

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

// 旅程编辑描述组件：组合标签文字与文本输入框，支持旅程描述的编辑
@Composable
fun JourneyEditDescription(
    journey: Journey, // 当前编辑的旅程实体，从中读取 description 作为输入框的初始值
    onValueChange: (String) -> Unit, // 描述文本变化回调，将输入内容同步到 ViewModel/数据库
) {
    // 显示"旅程描述："标签，15.dp 左侧边距，无首行缩进
    TextMedium(
        text = "旅程描述：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    // 标签与输入框之间的 2.dp 间距
    Spacer(Modifier.padding(2.dp))
    // 文本输入框：绑定 journey.description，最大 1024 字符，占位提示"请填写旅程描述"
    InputText3(
        value = journey.description,
        onValueChange = onValueChange,
        tipText = "请填写旅程描述",
        maxLength = 1024
    )
}