/**
 * 自定义底部导航栏组件
 * 
 * 用途：
 * - 提供应用底部导航功能，包含"点亮"和"旅程"两个导航项
 * - 根据选中状态展示平滑的视觉动画效果
 * 
 * 功能：
 * - 导航项图标和文字展示
 * - 选中/未选中状态的平滑过渡动画（透明度、字体大小、图标大小、背景色）
 * - 点击切换导航页面
 * 
 * 关联组件：
 * - CustomNavController: 管理导航状态，记录当前选中的导航项，处理页面切换（含200ms防抖）
 * - NavPath2: 导航项数据类，包含名称和图标资源ID
 * - NavPathObj2: 导航项常量对象，定义"点亮"和"旅程"两个预设导航项
 * - 主题颜色系统: BGLight0(导航栏背景色)、FontDark4(文字颜色)、Purple40(图标颜色)、Purple80(选中背景色)
 * 
 * 实现逻辑：
 * - Navigation2: 使用 Box + Row 构建水平居中的导航栏容器，遍历 NavPathObj2.list 创建等宽的 NavItem
 * - NavItem: 接收 NavPath2 配置，通过比较当前路径与传入路径判断是否选中
 *           使用 animateFloatAsState 实现4个属性的 200ms 缓动动画
 *           点击时调用 CustomNavController.navigate() 切换导航状态
 */
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.presentation2.viewmodel.nav_controller.NavigationViewModel
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.Purple40
import com.example.travel_footprint_android.ui.theme.Purple80

@Composable
fun Navigation3(
    modifier: Modifier = Modifier,
    navigationViewModel: NavigationViewModel,
    setViewIndex: (Int) -> Unit,
) {
    val currentDest by navigationViewModel.currentDestination.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = BGLight0)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(.8f),
        ) {
            NavPathObj2.list.forEachIndexed { index, it ->
                NavItem(
                    it,
                    Modifier.weight(1f),
                    {
                        navigationViewModel.navigate(it)
                        setViewIndex(index)
                    },
                    currentDest)
            }
        }
    }
}

// 单个导航项组件：根据选中状态展示不同样式，点击触发导航切换
@Composable
private fun NavItem(
    navPath: NavPath2,
    modifier: Modifier,
    navChange: () -> Unit,
    navPathNow: NavPath2
) {
    // 判断当前项是否被选中
    val isSelected = navPathNow == navPath

    // 文字透明度动画：选中时完全不透明(1f)，未选中时半透明(0.6f)，200ms过渡
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemAlpha"
    )

    // 文字大小动画：选中时缩小(.9f)，未选中时正常(1f)，200ms过渡
    val animatedFontSize by animateFloatAsState(
        targetValue = if (isSelected) .9f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemFontSize"
    )

    // 图标大小动画：选中时正常(1f)，未选中时缩小(.8f)，200ms过渡
    val animateIconSize by animateFloatAsState(
        targetValue = if (isSelected) 1f else .8f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemSize"
    )

    // 背景色动画：选中时渐变为30%透明度紫色(.3f)，未选中时完全透明(0f)，200ms过渡
    val backgroundAlpht by animateFloatAsState(
        targetValue = if (isSelected) .3f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "navItemAlpha"
    )

    // 垂直布局：图标在上，文字在下，居中对齐，点击无涟漪效果
    Column(
        modifier = modifier
            .padding(10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 禁用点击涟漪反馈
                onClick = navChange
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 导航图标：尺寸和透明度随选中状态动画变化，统一使用紫色染色
        Image(
            modifier = Modifier
                .size(18.dp * animateIconSize)
                .alpha(animatedAlpha),
            painter = painterResource(id = navPath.icon),
            contentDescription = navPath.name + "图标",
            colorFilter = ColorFilter.tint(Purple40.copy(alpha = 0.8f)),
        )

        // 图标与文字之间的间距
        Spacer(Modifier.padding(2.dp))

        // 用 Box 包裹 Text 以添加圆角背景，同时保持内容居中
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp)) // 圆角背景
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