/**
 * 图片方块中的添加图标组件
 * 
 * 用途：
 * - 在图片选择/展示区域（如 ImageSquare）中作为占位符，提示用户可以添加新图片
 * - 通常用于图片网格的最后一个空位，点击后触发图片选择或添加操作
 * 
 * 功能：
 * - 展示一个居中"+"图标，使用 BGLight0 浅色背景
 * - 支持点击回调，触发外部传入的添加操作
 * - 图标大小可通过参数自定义（占容器比例）
 * 
 * 关联组件：
 * - BGLight0: 主题背景色常量（浅色系）
 * - FontDark6: 主题文字/图标颜色常量（深色系），用于图标染色
 * - R.drawable.ic_add: "+"图标资源
 * 
 * 实现逻辑：
 * - Box 作为容器：设置 BGLight0 背景色，响应点击事件
 * - Image 居中显示：通过 fillMaxSize(iconSize) 控制图标占容器的比例，使用 FontDark6 统一着色
 * 
 * @param modifier 外部 Modifier
 * @param iconSize 图标占容器大小的比例(0~1)
 * @param clickable 点击回调
 */
package com.example.travel_footprint_android.presentation2.components.image_square.add_icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.FontDark6

// 图片方块添加图标：浅色背景 + 居中加号，用于图片网格空位占位
@Composable
fun AddIcon(
    modifier: Modifier = Modifier,
    iconSize: Float, // 图标占容器比例
    clickable: () -> Unit
) {
    // 外层容器：设置浅色背景，响应点击
    Box(
        modifier = modifier
            .background(BGLight0)
            .clickable {
                clickable()
            }
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