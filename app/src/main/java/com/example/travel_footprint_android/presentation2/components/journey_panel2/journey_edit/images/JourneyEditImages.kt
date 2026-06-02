package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.images

/**
 * JourneyEditImages - 旅程编辑页面的图片管理组件
 *
 * ====== 用途 ======
 * 本文件是"旅程编辑"子页面中专用于管理旅程回忆图片的 UI 组件。
 * 在编辑旅程信息时，用户需要查看已添加的回忆照片、删除不需要的照片，
 * 此组件为该编辑场景提供图片区域的完整布局。
 *
 * ====== 主要功能 ======
 * 1. 显示标题标签 —— 在图片网格上方显示"旅程回忆："文字标题，提示用户该区域功能。
 * 2. 嵌入图片网格 —— 委托给 Reminiscence 组件展示旅程图片的流式网格，
 *    并强制开启删除模式（showDelIcon = true），方便编辑时移除照片。
 *
 * ====== 关联组件 ======
 * - TextMedium（/text/text_medium/TextMedium.kt）：
 *   封装的自定义字体文本组件，用于显示"旅程回忆："标签。
 * - Reminiscence（/journey_details/reminiscence/Reminiscence.kt）：
 *   旅程回忆图片网格组件，内部使用 FlowRow 流式布局 + ImageSquare2 组件，
 *   支持已有图片展示、添加新图片、删除图片。
 * - Journey（/data/entity/Journey.kt）：
 *   Room 实体类，其中的 journeyImagePaths: List<String> 存储图片文件路径。
 *
 * ====== 简单实现逻辑 ======
 * 1. 顶层先用 TextMedium 渲染标题文本"旅程回忆："，添加左右 15dp 内边距。
 * 2. 插入一个 2dp 高度的 Spacer 作为标题与图片网格之间的间距。
 * 3. 直接将 journey 和 updateJourney 透传给 Reminiscence 组件，
 *    并设置 showDelIcon = true 开启删除按钮，适配编辑场景。
 */

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_detail.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

// 旅程编辑页面的图片区域 Composable，组合"标题 + 图片网格"布局
// @param journey 当前正在编辑的旅程实体，携带 journeyImagePaths 图片列表供 Reminiscence 渲染
// @param updateJourney 更新旅程数据的回调，图片删除/添加时由 Reminiscence 内部触发并向上传递
@Composable
fun JourneyEditImages(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
) {
    // 渲染"旅程回忆："标题，使用自定义字体 TextMedium，左右留 15dp 边距
    TextMedium(
        text = "旅程回忆：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    // 标题与图片网格之间的垂直间距占位
    Spacer(Modifier.padding(2.dp))
    // 嵌入 Reminiscence 图片网格组件，强制开启删除模式以适配编辑场景
    Reminiscence(
        journey = journey,
        updateJourney = updateJourney,
        showDelIcon = true,
    )
}