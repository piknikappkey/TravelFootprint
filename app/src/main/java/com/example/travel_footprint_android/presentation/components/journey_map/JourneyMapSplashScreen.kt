package com.example.travel_footprint_android.presentation.components.journey_map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.bg_box.BGBox
import com.example.travel_footprint_android.ui.theme.SecondColor3
import kotlinx.coroutines.delay

@Composable
fun JourneyMapSplashScreen(
    onFinished: () -> Unit,
    onShowScreen: () -> Unit,
) {
    // 整体透明度动画控制器，初始值为 0（完全透明/不可见）
    // 使用 Animatable 而非 animateFloatAsState 因为需要精确控制动画时序
    val alpha = remember { Animatable(0f) }

    // 控制 Logo 是否放大的状态，初始为 false（原始大小）
    var iconLargen by remember { mutableStateOf(false) }

    // Logo 缩放动画：响应 iconLargen 状态变化自动播放
    // false → 1.0 倍原始大小，true → 1.5 倍放大效果
    val aniIconScale by animateFloatAsState(
        targetValue = if (iconLargen) 1.5f else 1f,
        animationSpec = tween(durationMillis = 400), // 缩放动画持续 400ms
        label = "aniImgRainAlpha"
    )

    // 编排整个闪屏动画的时序（仅在首次组合时执行一次，key 为 Unit）
    LaunchedEffect(Unit) {
        // 阶段1：背景淡入，透明度从 0 动画到 1，持续 200ms
        alpha.animateTo(1f, animationSpec = tween(200))

        // 阶段2：通知父组件开始渲染主界面（此时闪屏仍在显示，主界面可在后台预渲染）
        onShowScreen()

        // 阶段3：等待 600ms，让用户欣赏完整的闪屏画面
        delay(600)

        // 阶段4：触发 Logo 放大动画（iconLargen 变为 true，aniIconScale 自动过渡到 1.5f）
        iconLargen = true

        // 阶段5：等待 100ms，让 Logo 放大动画先播放一小段时间
        delay(100)

        // 阶段6：背景淡出，透明度从 1 动画到 0，持续 300ms
        alpha.animateTo(0f, animationSpec = tween(300))

        // 阶段7：通知父组件闪屏动画已结束，可以安全移除闪屏层
        onFinished()
    }

    // 背景图片容器：加载闪屏背景图并居中放置内容
    BGBox(
        modifier = Modifier
            .fillMaxSize()     // 全屏显示
            .alpha(alpha.value), // 整体透明度由 alpha 动画控制器驱动
        contentAlignment = Alignment.Center // 内容居中对齐
    ) {
        // 中心 Logo 图标：地图图标，应用品牌标识
        Image(
            painter = painterResource(id = R.drawable.ic_map), // 加载地图图标资源
            contentDescription = null, // 无无障碍描述（装饰性图片）
            modifier = Modifier
                .size(140.dp)            // 固定尺寸 140dp × 140dp
                .scale(aniIconScale)     // 应用缩放动画（1.0x → 1.5x）
                .alpha(alpha.value)
                .offset(y = (-150).dp),     // 透明度与背景同步
            colorFilter = ColorFilter.tint(SecondColor3), // 主题色染色
        )
    }
}
