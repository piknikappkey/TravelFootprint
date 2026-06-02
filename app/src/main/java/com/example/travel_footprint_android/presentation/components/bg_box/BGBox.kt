/*
 * 文件名：BGBox.kt
 * 包路径：presentation2.components.bg_box
 *
 * 【用途】
 * 通用背景容器可组合组件，为内容提供统一的背景样式：带阴影的圆角色块。
 * 本组件是一个轻量级的 Box 包装器，封装了 `shadow` + `background` 组合修饰符，
 * 使项目中各处需要使用统一背景风格的地方可以复用，避免重复书写相同的 Modifier 链。
 *
 * 【功能】
 * 1. 阴影效果：通过 `Modifier.shadow()` 添加阴影，可自定义阴影高度（elevation）和形状（shape）
 * 2. 圆角背景：通过 `Modifier.background()` 添加圆角色块背景，可自定义颜色（bgColor）和圆角形状（shape）
 * 3. 内容插槽：通过 `@Composable () -> Unit` 的 composable 参数接收任意子内容，类似于 slot API
 *
 * 【关联组件】
 * - BGImgBox    : 同包下的增强版背景容器，额外支持从 drawable 列表中随机选取并渲染背景图片
 * - JourneyList3: 旅程列表组件中使用 BGBox 包裹"前往足迹"按钮的背景
 *
 * 【简单实现逻辑】
 * 1. 接收 modifier、elevation、shape、bgColor、composable 五个参数
 * 2. 内部创建一个 Box，将传入的 modifier 与 shadow + background 修饰符链组合
 * 3. shadow 使用 clip=true 确保阴影按圆角形状裁剪
 * 4. background 应用 bgColor 颜色作为纯色背景
 * 5. 在 Box 内部调用 composable() 渲染传入的子内容
 *
 * 【被引用位置】
 * - JourneyList.kt: 右下角"前往足迹->"按钮的背景
 */

package com.example.travel_footprint_android.presentation.components.bg_box

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 通用背景盒子组件：带阴影的圆角色块容器
 *
 * @param modifier   外部传入的修饰符，叠加在 shadow + background 修饰符之前
 * @param elevation  阴影高度（dp），默认 1.dp
 * @param shape      圆角形状，默认 8.dp 圆角，同时作用于阴影裁剪和背景形状
 * @param bgColor    背景颜色，默认浅杏色 #FCF1EB
 * @param composable 子内容插槽，接收一个 @Composable lambda，渲染在 Box 内部
 */
@Composable
fun BGBox(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp, // 阴影大小
    shape: RoundedCornerShape = RoundedCornerShape(8.dp), // 圆角
    bgColor: Color = Color(0xFFFCF1EB),
    contentAlignment: Alignment = Alignment.TopStart,
    composable: @Composable () -> Unit,
) {
    // 使用 Box 作为容器，按顺序叠加：外部 modifier → shadow（阴影 + 按形状裁剪） → background（纯色圆角色块）
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,                 // 阴影高度
                shape = shape, // 圆角
                clip = true                        // 同时按照该形状裁剪内容
            )
            .background(
                color = bgColor,
            ),
        contentAlignment = contentAlignment
    ) {
        // 调用传入的 composable lambda，渲染子内容
        composable()
    }
}
