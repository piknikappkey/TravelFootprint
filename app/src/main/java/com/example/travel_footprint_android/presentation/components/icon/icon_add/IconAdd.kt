/**
 * 添加图标按钮组件
 * 
 * 用途：
 * - 提供一个通用的"添加"操作图标按钮，可嵌入到其他组件中使用
 * - 适用于需要触发新增操作（如添加图片、新建项目等）的场景
 * 
 * 功能：
 * - 展示一个居中的"+"图标，带阴影和圆角背景
 * - 支持点击回调，触发外部传入的添加操作
 * - 图标大小、阴影、圆角、背景色均可自定义
 * 
 * 关联组件：
 * - FontDark6: 主题颜色常量，用于图标染色
 * - R.drawable.ic_add: "+"图标资源
 * 
 * 实现逻辑：
 * - Box 作为容器：外层设置阴影(默认为1.dp)、圆角(默认5.dp)、背景色(默认暖白色)
 * - Image 居中显示，通过 fillMaxSize(iconSize) 控制图标相对于容器的大小比例(默认40%)
 * - 点击事件透传至 clickable 回调
 * 
 * @param modifier 外部 Modifier，默认 fillMaxSize()
 * @param clickable 点击回调，默认空实现
 * @param iconSize 图标占容器大小的比例(0~1)，默认 0.4f
 * @param elevation 阴影高度，默认 1.dp
 * @param shape 圆角形状，默认 RoundedCornerShape(5.dp)
 * @param bgColor 背景色，默认 暖白色(0xFFFDF8F5)
 */
package com.example.travel_footprint_android.presentation.components.icon.icon_add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.FontDark6

// 通用添加图标按钮：Box 容器内含居中图标，支持自定义阴影/圆角/背景/大小
@Composable
fun IconAdd(
    modifier: Modifier = Modifier.fillMaxSize(),
    clickable: () -> Unit = {},
    iconSize: Float = .4f, // 图标占容器比例
    elevation: Dp = 1.dp, // 阴影大小
    shape: RoundedCornerShape = RoundedCornerShape(5.dp), // 圆角
    bgColor: Color = Color(0xFFFDF8F5), // 背景色
) {
    // 外层容器：设置阴影、圆角背景，并响应点击
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,                 // 阴影高度
                shape = shape, // 圆角
                clip = true                        // 同时按照该形状裁剪内容
            )
            .background(
                color = bgColor,
            )
            .clickable(onClick = { clickable() })
    ) {
        // 居中图标：按 iconSize 比例缩放，使用 FontDark6 着色
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(iconSize),
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = "添加图标",
            colorFilter = ColorFilter.tint(FontDark6),
        )
    }
}