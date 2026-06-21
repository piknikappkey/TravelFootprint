package com.example.travel_footprint_android.presentation.components.milestone

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.SecondColor3

/**
 * 展开/收起按钮（带旋转动画的箭头图标）
 *
 * 用途：
 * - 用于未解锁成就列表的展开/收起控制
 * - 点击后通过旋转箭头图标（0°↔180°）直观展示列表展开/收起状态
 *
 * @param expanded 当前是否展开状态
 * @param onClick 点击回调，用于切换展开/收起状态
 * @param aniTime 旋转动画时长（毫秒），默认 400ms
 */
@Composable
fun IcExpandButton(
    expanded: Boolean,
    onClick: () -> Unit,
    aniTime: Int = 400
) {
    // 旋转动画：expanded=true 时旋转 0°（箭头朝下），expanded=false 时旋转 180°（箭头朝上）
    val angle by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(durationMillis = aniTime),
        label = "expandRotate"
    )

    // 箭头图标：应用旋转动画 + 主题色染色 + 点击响应
    Image(
        modifier = Modifier
            .size(20.dp)
            .rotate(angle)
            .clickable(onClick = onClick),
        painter = painterResource(id = R.drawable.ic_up2),
        contentDescription = if (expanded) "收起" else "展开",
        colorFilter = ColorFilter.tint(SecondColor3)
    )
}
