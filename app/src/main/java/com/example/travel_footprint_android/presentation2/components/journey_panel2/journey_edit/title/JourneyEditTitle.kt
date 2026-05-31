package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.title

/*
 * ============================================================================
 * JourneyEditTitle.kt — 旅程标题编辑组件
 * ============================================================================
 *
 * 【用途】
 *   在旅程编辑界面中提供标题输入区域，让用户为旅程设置一个简洁的标题
 *   （最长 20 个字符）。
 *
 * 【功能】
 *   1. 标签展示：使用 TextMedium 显示"旅程标题："提示文字
 *   2. 标题输入：使用 InputText3 可复用输入框组件绑定了 Journey.title，
 *      限制最大输入 20 个字符
 *   3. 数据流：用户输入内容通过 onValueChange 回调同步回上层，
 *      最终写入 Journey.title 属性
 *
 * 【关联组件】
 *   - Journey（data.entity）：Room 实体类，journeys 表映射，包含 title、
 *     description、startDate、endDate、经纬度等旅程信息字段
 *   - TextMedium（text.text_medium）：自定义文字组件，使用软萌初恋体
 *     （FFRuanMengChuLianTi），支持首行缩进和颜色自定义
 *   - InputText3（input.input_text）：自定义输入框组件，带图标前缀、
 *     聚焦边框反馈、字数截断、多行支持
 *
 * 【简单实现逻辑】
 *   1. 接收 Journey 实体对象（持有当前标题值）和标题变更回调
 *   2. 先渲染 TextMedium 标签"旅程标题："（水平 15dp 内边距）
 *   3. 再渲染 2dp 高度的 Spacer 作为标签与输入框之间的间距
 *   4. 最后渲染 InputText3 输入框：
 *      - value 绑定 journey.title，初始显示已有标题
 *      - onValueChange 将用户输入直接上抛
 *      - tipText 为空时显示占位文字"请填写旅程标题"
 *      - maxLength = 20 限制标题最长 20 字
 * ============================================================================
 */

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

// —— 旅程标题编辑组件 ——
// 展示标题标签 + 标题输入框，标题最大长度 20 字
@Composable
fun JourneyEditTitle(
    // 当前正在编辑的旅程实体，用于读取和绑定已有的标题值
    journey: Journey,
    // 标题文本变化时的回调，上层通过该回调更新 Journey.title
    onValueChange: (String) -> Unit,
) {
    // ""旅程标题：""标签——使用 TextMedium 展示提示文字
    TextMedium(
        text = "旅程标题：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    // 标签与输入框之间的 2dp 垂直间距
    Spacer(Modifier.padding(2.dp))
    // 标题输入框——绑定 journey.title，限制 20 字，空时显示占位提示
    InputText3(
        value = journey.title,
        onValueChange = onValueChange,
        tipText = "请填写旅程标题",
        maxLength = 20,
    )
}