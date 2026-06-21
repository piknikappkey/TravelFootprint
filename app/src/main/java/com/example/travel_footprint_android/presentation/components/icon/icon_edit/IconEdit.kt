package com.example.travel_footprint_android.presentation.components.icon.icon_edit

/**
 * IconEdit - 编辑图标组件
 *
 * 【用途】
 *  - 提供一个可点击的编辑（铅笔）图标，用于触发编辑、修改等操作
 *  - 在足迹列表项中被点击展开后显示，用于跳转到足迹编辑页面
 *
 * 【功能】
 *  - 1. 显示编辑图标：加载 R.drawable.ic_edit 资源作为图标
 *  - 2. 点击事件：通过 onClick 回调支持外部传入点击行为
 *  - 3. 自定义样式：支持自定义 Modifier 和图标着色 ColorFilter
 *
 * 【关联组件】
 *  - 无项目内其他自定义组件依赖，仅使用标准 Compose Image + clickable
 *  - 被 FootprintListItem.HeadRow 引用，用于进入足迹编辑页面
 *
 * 【简单实现逻辑】
 *  - 使用 Compose Image 组件加载编辑图标资源
 *  - 通过 clickable 修饰符使图标可点击，触发传入的 onClick 回调
 *  - ColorFilter.tint 对图标进行主题色着色（默认 SecondColor4 80% 透明度）
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.SecondColor4

// 编辑图标组件：可点击的编辑图标，支持自定义尺寸、着色和点击行为
@Composable
fun IconEdit(
    modifier: Modifier = Modifier.size(20.dp),
    colorFilter: ColorFilter = ColorFilter.tint(SecondColor4.copy(alpha = 0.8f)),
    onClick: () -> Unit,
) {
    // 加载编辑图标资源，应用颜色滤镜和点击修饰符
    Image(
        modifier = modifier
            .clickable(onClick = onClick),
        painter = painterResource(R.drawable.ic_edit),
        contentDescription = "修改图标",
        colorFilter = colorFilter,
    )
}