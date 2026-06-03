package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_list

/**
 * 足迹列表添加图标按钮
 *
 * 用途：
 * - 在足迹列表的末尾提供一个"添加"按钮，用于触发新增足迹的操作
 * - 属于旅程面板(journey_panel2) → 足迹面板(footprint_panel) → 足迹列表(footprint_list) 层级中的交互组件
 *
 * 功能：
 * - 展示一个带 padding 的添加图标按钮
 * - 点击后触发外部传入的 clickable 回调，用于新增足迹
 * - 图标尺寸固定为 48.dp × 48.dp
 *
 * 关联组件：
 * - IconAdd: 通用的添加图标组件，内部包含一个带阴影和圆角背景的"+"图标
 * - FontDark6(图标颜色): 来自 ui/theme/Color.kt 的灰色常量 Color(0xFF666666)
 *
 * 实现逻辑：
 * - 外层 Box 提供 10.dp 的 padding 间距
 * - 内层调用 IconAdd 组件，固定宽高为 48.dp
 * - clickable 回调通过参数透传给 IconAdd，由外部决定点击后的具体行为
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.icon.icon_add.IconAdd

// 足迹列表添加图标按钮：在外层 Box 中包裹 IconAdd，提供固定尺寸和 padding
@Composable
fun FootprintListAddIcon(
    modifier: Modifier = Modifier, // 外部传入的 Modifier，用于调整位置/大小
    clickable: () -> Unit = {} // 点击添加按钮时的回调，默认空操作
) {
    // 外层容器：为图标提供 10.dp 的内边距间距
    Box(
        modifier = modifier
            .padding(10.dp)
    ) {
        // 内层添加图标：固定宽高为 48.dp，点击回调透传出去
        IconAdd(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp),
            clickable = clickable,
        )
    }
}