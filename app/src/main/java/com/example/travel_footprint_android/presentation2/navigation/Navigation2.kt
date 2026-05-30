package com.example.travel_footprint_android.presentation2.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation2.viewmodel.nav_controller.CustomNavController
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.Purple40
import com.example.travel_footprint_android.ui.theme.Purple80

@Composable
fun Navigation2(
    modifier: Modifier = Modifier
) {
    /**
     * 自定义导航
     */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = BGLight0)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center // 水平居中
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(.8f),
        ) {
            NavPathObj2.list.forEach {
                NavItem(
                    it,
                    Modifier.weight(1f),
                    { CustomNavController.navigate(it) },
                    CustomNavController.currentDestination.value)
            }
        }
    }
}
@Composable
fun NavItem(
    navPath: NavPath2,
    modifier: Modifier,
    navChange: () -> Unit,
    navPathNow: NavPath2
) {
    val isSelected = navPathNow == navPath

    // 文字透明度动画
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemAlpha"
    )

    // 文字大小动画
    val animatedFontSize by animateFloatAsState(
        targetValue = if (isSelected) .9f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemFontSize"
    )

    // 图标大小动画
    val animateIconSize by animateFloatAsState(
        targetValue = if (isSelected) 1f else .8f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemSize"
    )

    // 背景色动画：选中时渐变为带透明度的紫色，未选中时完全透明
    val backgroundAlpht by animateFloatAsState(
        targetValue = if (isSelected) .3f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemAlpha"
    )


    Column(
        modifier = modifier
            .padding(10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = navChange
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .size(18.dp * animateIconSize)
                .alpha(animatedAlpha),
            painter = painterResource(id = navPath.icon),
            contentDescription = navPath.name + "图标",
            colorFilter = ColorFilter.tint(Purple40.copy(alpha = 0.8f)),
        )

        Spacer(Modifier.padding(2.dp))

        // 用 Box 包裹 Text 以添加背景，同时保持内容居中
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp)) // 圆角
                .background(Purple80.copy(backgroundAlpht)) // 动画背景色
                .padding(horizontal = 10.dp, vertical = 0.dp) // 内边距让背景更自然
        ) {
            Text(
                text = navPath.name,
                modifier = Modifier.alpha(animatedAlpha),
                fontSize = 14.sp  * animatedFontSize,
                color = FontDark4,
            )
        }
    }
}