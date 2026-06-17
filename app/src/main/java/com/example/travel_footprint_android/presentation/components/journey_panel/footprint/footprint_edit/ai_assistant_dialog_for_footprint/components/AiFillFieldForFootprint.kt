package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.ai_assistant_dialog_for_footprint.components

/**
 * 足迹 AI 智能填写字段枚举
 *
 * 用于标识用户选择让 AI 填充的足迹字段类型
 */
enum class AiFillFieldForFootprint(val label: String) {
    TITLE("标题"),
    DESCRIPTION("描述"),
    ADDRESS("地址"),
    RATING("评分")
}
