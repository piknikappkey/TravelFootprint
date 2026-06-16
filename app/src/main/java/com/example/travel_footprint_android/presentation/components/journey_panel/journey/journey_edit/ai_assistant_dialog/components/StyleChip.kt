package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.BGLight4
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.FontDark8
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * 风格选择标签组件
 *
 * 选中时高亮显示，未选中时为浅色边框样式。
 *
 * @param label 标签文字
 * @param isSelected 是否被选中
 * @param onClick 点击回调
 * @param enabled 是否可用
 */
@Composable
fun StyleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val backgroundColor = when {
        !enabled -> BGLight4.copy(alpha = 0.5f)
        isSelected -> MainColor3.copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    val contentColor = when {
        !enabled -> FontDark6.copy(alpha = 0.5f)
        isSelected -> MainColor3
        else -> FontDark6
    }
    val borderStroke = when {
        !enabled -> BorderStroke(1.dp, FontDark8.copy(alpha = 0.3f))
        isSelected -> BorderStroke(1.dp, MainColor3)
        else -> BorderStroke(1.dp, FontDark8.copy(alpha = 0.5f))
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = borderStroke,
    ) {
        TextMedium(
            text = label,
            fontSize = 15.sp,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
