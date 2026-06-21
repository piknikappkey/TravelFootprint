/**
 * DeleteIcon - 删除图标按钮组件
 *
 * 用途：
 * - 在图片或卡片组件的右上角位置提供一个可点击的删除图标按钮
 * - 用于图片编辑场景中移除已选择的图片（如旅程封面图、足迹图片等）
 * - 通常与 ImageSquare2 等图片展示组件配合使用，作为覆盖层图标
 *
 * 功能：
 * - 删除图标展示：显示 R.drawable.ic_delete3 图标，通过 ColorFilter.tint 着色为金色（SecondColor4）
 * - 右上角偏移定位：通过 offset(x = iconSize/3, y = -iconSize/3) 将按钮偏移到右上角，使其"浮动"在父组件右上角
 * - 视觉样式：圆角容器（RoundedCornerShape）+ 金色描边（SecondColor3）+ 半透明白色背景（BGLight0 80% 透明度）
 * - 点击交互：通过 clickable 修饰符绑定删除回调
 *
 * 关联组件：
 * - BGLight0（ui.theme.Color）：纯白色 #FFFFFFFF，作为按钮背景色（80% 透明度）
 * - SecondColor3（ui.theme.Color）：金色 #FFFFBF47，作为按钮描边颜色
 * - SecondColor4（ui.theme.Color）：金色 #FFFFAB00，作为删除图标的着色
 * - R.drawable.ic_delete3：删除图标 drawable 资源
 * - 被 ImageSquare2 等图片组件调用，用于提供删除图片的交互入口
 *
 * 实现逻辑：
 * 1. 外层 Box 通过 offset 修饰符将按钮定位到右上角（x 正偏移、y 负偏移，偏移量为 iconSize/3）
 * 2. Box 应用 clip + border + background 组合样式，创建带圆角和描边的半透明背景容器
 * 3. 内部 Image 显示 ic_delete3 图标，尺寸由 iconSize 参数控制，通过 ColorFilter 着色
 * 4. 点击整个 Box 触发 clickable 回调
 */
package com.example.travel_footprint_android.presentation.components.image_square.image_square_delete_icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.SecondColor3
import com.example.travel_footprint_android.ui.theme.SecondColor4

@Composable
fun ImageSquareDeleteIcon(
    modifier: Modifier = Modifier, // 应用于外层 Box 的修饰符
    iconSize: Dp, // 删除图标尺寸，同时影响偏移量（offset = iconSize/3）
    clickable: () -> Unit, // 点击删除按钮时的回调
) {
    // 外层容器：偏移定位 + 圆角裁剪 + 描边 + 半透明背景 + 可点击
    Box(
        modifier = modifier
            .offset(x = iconSize / 3, y = -(iconSize / 3)) // 偏移到右上角，形成"浮动"效果
            .clip(RoundedCornerShape(12.dp)) // 裁剪圆角背景范围
            .border(1.dp, SecondColor3, RoundedCornerShape(16.dp)) // 金色描边
            .background(BGLight0.copy(alpha = 0.8f)) // 80% 不透明白色背景
            .clickable {
                clickable() // 触发删除回调
            },
    ) {
        // 删除图标：显示 ic_delete3 资源，通过 ColorFilter 着色为金色
        Image(
            modifier = Modifier
                .size(iconSize), // 图标尺寸
            painter = painterResource(id = R.drawable.ic_delete3),
            contentDescription = "删除图标",
            colorFilter = ColorFilter.tint(SecondColor4), // 金色着色
        )
    }
}
