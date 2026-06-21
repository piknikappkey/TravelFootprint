/*
 * 文件名：ConfirmDeleteDialog.kt
 * 包路径：presentation2.components.journey_panel2.confirm_delete_dialog
 *
 * 【用途】
 * 通用删除确认弹窗组件，在用户执行删除操作前弹出一个模态对话框，
 * 要求用户二次确认，防止误删除。用于旅程编辑页面等需要删除操作的场景。
 *
 * 【功能】
 * 1. 模态弹窗：使用 Compose Dialog 显示，阻挡底层交互，强制用户确认或取消
 * 2. 标题显示：通过 Headline 组件以大号字体居中显示删除操作标题
 * 3. 消息提示：通过 TextMedium 组件居中显示删除操作的具体说明文字
 * 4. 双按钮交互：底部并排提供"取消"（安全操作）和"删除!"（危险操作）两个按钮
 * 5. 背景装饰：采用 BGBox（圆角阴影容器）+ BGImgBox（随机背景图）双层嵌套提供视觉层次
 *
 * 【关联组件】
 * - BGBox       : 圆角阴影背景容器，为弹窗提供统一的浅杏色底和阴影效果
 * - BGImgBox    : 随机背景图片容器，从两个矩形背景图中随机选取一张装饰弹窗
 * - Headline    : 标题文字组件（FFDaMengKaTongTi 字体，W500 字重，20sp）
 * - TextMedium  : 正文文字组件（FFRuanMengChuLianTi 字体，W300 字重，16sp）
 * - ButtonSave  : 保存/确认风格按钮（圆角阴影卡片），此处用于"取消"按钮
 * - ButtonDelete: 删除风格按钮（圆角阴影卡片，DeleteColor 背景），用于"删除!"按钮
 * - SecondColor2: 金色副色调 #FDD583，用作"取消"按钮的背景色
 *
 * 【简单实现逻辑】
 * 1. 接收四个参数：title（标题文本）、message（消息文本）、
 *    onConfirm（确认回调）、onDismiss（取消回调）
 * 2. 使用 Compose Dialog 包裹所有内容，点击弹窗外围触发 onDismiss
 * 3. BGBox 提供最外层的圆角阴影背景容器
 * 4. BGImgBox 在背景之上随机选取一张装饰图片渲染（bg_rectangular_2 系列），
 *    并覆盖半透明遮罩确保内容可读性
 * 5. Column 垂直排列：标题 → 间距 → 消息 → 间距 → 按钮行
 * 6. Row 水平排列两个按钮，间隔 50dp：
 *    - "取消"用 ButtonSave 组件（金底色 SecondColor2），点击触发 onDismiss
 *    - "删除!"用 ButtonDelete 组件（删除色），点击触发 onConfirm
 *
 * 【调用场景】
 * 被 JourneyEdit 等编辑/管理页面调用，在用户点击删除图标时弹出确认弹窗
 */
package com.example.travel_footprint_android.presentation.components.dialog

import androidx.compose.foundation.layout.Arrangement // 子组件排列方式（间距）
import androidx.compose.foundation.layout.Column // 垂直布局容器
import androidx.compose.foundation.layout.Row // 水平布局容器
import androidx.compose.foundation.layout.Spacer // 占位间距组件
import androidx.compose.foundation.layout.height // 设置 Spacer 高度
import androidx.compose.foundation.layout.padding // 内边距修饰符
import androidx.compose.runtime.Composable // Compose 可组合函数注解
import androidx.compose.ui.Alignment // 子组件对齐方式
import androidx.compose.ui.Modifier // Compose 修饰符
import androidx.compose.ui.text.style.TextAlign // 文本对齐方式
import androidx.compose.ui.unit.dp // dp 尺寸单位
import androidx.compose.ui.unit.sp // sp 字体尺寸单位
import com.example.travel_footprint_android.R // 项目资源引用（drawable 图片 ID）
import com.example.travel_footprint_android.presentation.components.button.button_delete.ButtonDelete // 删除风格按钮
import com.example.travel_footprint_android.presentation.components.button.button_save.ButtonSave // 保存风格按钮（此处用作取消按钮）
import com.example.travel_footprint_android.presentation.components.text.headline.Headline // 标题文字组件
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium // 正文文字组件
import com.example.travel_footprint_android.ui.theme.SecondColor2 // 金色副色调 #FDD583

@Composable
fun ConfirmDeleteDialog(
    title: String, // 弹窗标题文本，如"删除旅程"
    message: String, // 弹窗提示消息，如"确定要删除该旅程吗？此操作不可撤销。"
    onConfirm: () -> Unit, // 用户点击"删除!"时触发的回调（执行删除逻辑）
    onDismiss: () -> Unit, // 用户点击"取消"或点击弹窗外围时触发的回调（关闭弹窗）
) {
    // 创建模态 Dialog，点击弹窗外部区域时触发 onDismiss 关闭弹窗
    DialogBox(
        R.drawable.bg_rectangular_2__1__0,
        R.drawable.bg_rectangular_2__1__1,
        onDismissRequest = onDismiss
    ) {
        // Column 垂直排列弹窗内容：标题 → 消息 → 按钮行
        Column(
            // 设置内容区左右 30dp、上下 24dp 的内边距
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally // 所有子组件水平居中
        ) {
            // 弹窗标题：使用 Headline 组件，20sp 字号，居中对齐
            Headline(
                text = title,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )
            // 标题与消息之间的垂直间距 10dp
            Spacer(Modifier.height(10.dp))
            // 弹窗消息正文：使用 TextMedium 组件，居中对齐
            TextMedium(
                text = message,
                textAlign = TextAlign.Center,
            )
            // 消息与按钮行之间的垂直间距 15dp
            Spacer(Modifier.height(15.dp))
            // Row 水平排列"取消"和"删除!"两个按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(50.dp) // 两按钮间距 50dp
            ) {
                // "取消"按钮：使用 ButtonSave 组件（保存风格），
                // 背景色改为金色 SecondColor2，点击关闭弹窗
                ButtonSave(
                    title = "取消",
                    color = SecondColor2,
                    onClick = onDismiss
                )
                // "删除!"按钮：使用 ButtonDelete 组件（删除风格，红色底），
                // 点击执行删除确认回调
                ButtonDelete(
                    title = "删除!",
                    onClick = onConfirm
                )
            }
        }
    }
}
