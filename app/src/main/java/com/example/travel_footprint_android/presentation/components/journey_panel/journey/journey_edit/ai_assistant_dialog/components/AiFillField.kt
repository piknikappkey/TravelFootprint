package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components

/**
 * AI 智能填写字段枚举
 *
 * 用于标识用户选择让 AI 填充的字段类型
 */
enum class AiFillField(val label: String) {
    TITLE("标题"),
    DESCRIPTION("描述"),
    ADDRESS("地址")
}
