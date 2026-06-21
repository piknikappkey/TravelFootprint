/**
 * BGImgBox - 随机背景图片容器组件
 *
 * 用途：
 * - 为页面或卡片提供带随机背景图片的 Box 容器，用于装饰性背景展示
 * - 在旅程详情页（JourneyDetails）、旅程列表项（JourneyItem3/4/5）、足迹详情等组件中广泛使用
 * - 图片以 cover 模式居中裁剪填充容器，上方覆盖半透明白色遮罩层保持内容可读性
 * - 若未提供图片列表（或列表为空），则退化为普通 Box 容器
 *
 * 功能：
 * - 随机图片选择：从传入的图片资源 ID 列表中随机选取一张作为背景
 * - 异步图片加载：通过 Coil rememberAsyncImagePainter 异步加载，复用全局缓存
 * - Cover 缩放模式：通过 ContentScale.Crop 实现图片按容器比例等比例缩放覆盖
 * - 硬件位图加速：使用 Compose Image composable，自动利用硬件位图优化
 * - 性能日志：测量并输出组件渲染耗时，用于性能调试
 *
 * 关联组件：
 * - 无项目内自定义依赖，仅依赖 Coil 图片加载库（coil-kt:coil-compose:2.5.0）
 * - 作为基础容器组件，被 BGBox/BGColumn/BGRow 等上层布局组件以及各页面组件直接或间接使用
 */
package com.example.travel_footprint_android.presentation.components.bg_box

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlin.random.Random

@Composable
fun BGImgBox(
    vararg imgIds: Int, // 背景图片资源 ID 列表，随机选取其中一张
    modifier: Modifier = Modifier, // 应用于外层 Box 的修饰符
    drawRectColor: Color = Color.White.copy(alpha = .3f), // 图片上方遮罩层颜色，默认 30% 透明白色
    contentAlignment: Alignment = Alignment.TopStart, // 内容在容器内的对齐方式
    alpha: Float = 1f,
    composable: @Composable () -> Unit, // 容器内的子组件内容
) {
    // 图片列表为空时 → 退化为普通 Box，不加载背景图片
    if (imgIds.size == 0) {
        Box(
            modifier = modifier,
        ) { composable() }
        return
    }

    // 随机选取一张图片资源 ID（remember 确保重组时不重新随机）
    val selectedResId = remember{ imgIds[Random.nextInt(imgIds.size)] }

    val context = LocalContext.current
    val request = remember(selectedResId) {
        ImageRequest.Builder(context)
            .data(selectedResId)
            .crossfade(true)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request)

    // 外层容器：底色 → 背景图片 → 半透明遮罩 → 子组件内容
    // Box 尺寸由 composable() 内容和外部 modifier 共同决定，不强制占满
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        // 背景图片层 — ContentScale.Crop 实现 Cover 缩放模式，利用硬件位图加速
        // matchParentSize: 匹配外层 Box 实际尺寸，不影响 Box 自身测量
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = alpha,
            modifier = Modifier.matchParentSize()
        )

        // 半透明遮罩层：降低背景图片的视觉强度，确保上层文字/组件可读
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(drawRectColor)
        )

        // 内容层 — 决定外层 Box 的自然尺寸
        composable()
    }
}
