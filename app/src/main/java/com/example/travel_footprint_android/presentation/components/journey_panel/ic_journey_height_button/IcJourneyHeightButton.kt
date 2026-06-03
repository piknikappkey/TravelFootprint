/**
 * 旅程面板高度切换按钮（带旋转动画的箭头图标）
 * 
 * 用途：
 * - 用于旅程面板（JourneyPanel）的高度切换控制
 * - 点击后通过旋转箭头图标（0°↔180°）直观展示面板展开/收起状态
 * 
 * 功能：
 * - 旋转动画：根据 state 值在 0° 和 180° 之间平滑旋转，默认动画时长 400ms
 * - 点击回调：触发外部传入的 onClick 回调切换面板高度
 * - 统一主题色：图标使用 SecondColor3 主题色染色
 * 
 * 关联组件：
 * - SecondColor3: 主题颜色常量（次要强调色），用于图标统一着色
 * - R.drawable.ic_up2: 向上箭头图标资源
 * 
 * 实现逻辑：
 * - animateFloatAsState + tween 实现平滑旋转动画，targetValue 根据 state 切换
 * - Image 组件叠加 rotate(angle) 修饰符实现旋转效果
 * - 点击事件透传至 onClick 回调
 * 
 * @param state 当前状态（true=展开/向上箭头朝下，false=收起/向上箭头朝上）
 * @param onClick 点击回调，用于切换面板高度状态
 * @param aniTime 旋转动画时长（毫秒），默认 400ms
 */
package com.example.travel_footprint_android.presentation.components.journey_panel.ic_journey_height_button

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

// 面板高度切换按钮：箭头图标带旋转动画，点击切换面板展开/收起状态
@Composable
fun IcJourneyHeightButton(
    state: Boolean,
    onClick: () -> Unit,
    aniTime: Int = 400 // 旋转动画时长（毫秒）
) {
    // 旋转动画：state=true 时旋转 180°（箭头朝下），state=false 时旋转 0°（箭头朝上）
    val angle by animateFloatAsState(
        targetValue = if(state) 180f else 0f,
        animationSpec = tween(durationMillis = aniTime), // 平滑缓动动画
        label = "clickRotate"
    )

    // 箭头图标：应用旋转动画 + 主题色染色 + 点击响应
    Image(
        modifier = Modifier
            .size(22.dp)
            .rotate(angle) // 应用旋转角度
            .clickable(onClick = onClick),
        painter = painterResource(id = R.drawable.ic_up2),
        contentDescription = "切换面板高度",
        colorFilter = ColorFilter.tint(SecondColor3), // 主题色染色
    )
}