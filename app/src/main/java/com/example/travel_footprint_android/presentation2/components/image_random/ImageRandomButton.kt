/**
 * ImageRandomButton - 随机涂鸦图片轮播按钮
 *
 * 用途：
 * - 提供一个可点击的按钮，图标内容为随机选取的涂鸦风格图片（ic_scrawl* 系列）
 * - 图片以自动轮播方式切换，配合淡入淡出动画效果，增强视觉趣味性
 * - 用于 JourneyScreen2 的按钮区域，作为装饰性与功能性结合的交互元素
 *
 * 功能：
 * - 随机图片初始化：启动时从 R.drawable 中随机选取一个以 "ic_scrawl" 前缀的 drawable 资源作为初始图标
 * - 自动轮播：通过 LaunchedEffect + while(true) 无限循环，每 autoSwitchIntervalMs 毫秒切换一次图片
 * - 淡入淡出动画：图片切换时执行 alpha 从 1f→0f→1f 的过渡动画（animatable 配合 tween），时长由 fadeInOutMs 控制
 * - 点击交互：按钮可点击，每次点击触发 onClick 回调，同时通过触发 changeImage 状态切换递进一次轮播周期
 *
 * 关联组件：
 * - getRandomScrawlDrawable()（同包 ImageUtils.kt）：
 *   - 通过 Java 反射遍历 R.drawable 类的所有 declaredFields
 *   - 筛选出以 "ic_scrawl" 前缀命名的 drawable 资源字段
 *   - 从中随机选取一个返回其 drawable 资源 ID
 *   - 若未匹配到任何字段，兜底返回 R.drawable.ic_scrawl0
 * - 被 JourneyScreen2 直接使用，作为右下角按钮区域的装饰按钮组件
 *
 * 实现逻辑：
 * 1. 初始时调用 getRandomScrawlDrawable() 获取随机图片 ID 存入 currentImage 状态
 * 2. 创建 Animatable(1f) 对象管理透明度动画
 * 3. 定义 animateSwitchImage() 挂起函数：先淡出→切换图片→淡入
 * 4. LaunchedEffect(changeImage) 启动无限循环：animateSwitchImage() → delay(interval) → 循环
 * 5. Image 组件显示当前图片，绑定 alpha 动画值和 clickable 修饰符
 * 6. 点击时切换 changeImage 布尔值（触发 LaunchedEffect 重新启动一轮循环）并调用 onClick
 */
package com.example.travel_footprint_android.presentation2.components.image_random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ImageRandomButton(
    modifier: Modifier = Modifier, // 应用于 Image 的外层修饰符
    size: Int = 40, // 按钮尺寸（dp）
    autoSwitchIntervalMs: Long = 5000L, // 自动轮播间隔（毫秒），默认 5 秒
    fadeInOutMs: Int = 300, // 淡入淡出动画时长（毫秒）
    onClick: () -> Unit, // 点击按钮时的回调
) {
    // 当前显示的图片资源 ID，初始随机选取
    var currentImage by remember { mutableIntStateOf(getRandomScrawlDrawable()) }
    // 透明度动画控制器（1f = 完全不透明，0f = 完全透明）
    val alpha = remember { Animatable(1f) }
    // 用于触发轮播重新开始的切换标记（点击时翻转）
    var changeImage by remember { mutableStateOf(false) }

    // 图片切换动画：先淡出 → 换图 → 淡入
    suspend fun animateSwitchImage() {
        alpha.animateTo(0f, animationSpec = tween(fadeInOutMs)) // 淡出
        currentImage = getRandomScrawlDrawable() // 随机选取新图片
        alpha.animateTo(1f, animationSpec = tween(fadeInOutMs)) // 淡入
    }

    // 监听 changeImage 状态变化，启动无限轮播循环
    // 每次 changeImage 值变化时取消旧协程、重启新循环
    LaunchedEffect(changeImage) {
        while (true) {
            animateSwitchImage() // 执行一次淡入淡出切换
            delay(autoSwitchIntervalMs) // 等待指定间隔后进入下一轮
        }
    }

    // 图片按钮：显示随机涂鸦图标，绑定透明度和点击事件
    Image(
        painter = painterResource(id = currentImage), // 当前随机图片
        contentDescription = null,
        modifier = modifier
            .size(size.dp) // 设置按钮尺寸
            .alpha(alpha.value) // 绑定透明度动画
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 无点击涟漪效果
                onClick = {
                    changeImage = !changeImage // 翻转状态，触发 LaunchedEffect 重启轮播
                    onClick() // 调用外部点击回调
                }
            ),
        contentScale = ContentScale.Fit, // 图片等比缩放适配容器
    )
}
