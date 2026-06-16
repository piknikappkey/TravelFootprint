package com.example.travel_footprint_android.presentation.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor2

@Composable
fun TipDialog(
    title: String, // 弹窗标题文本
    message: String, // 弹窗提示消息
    onDismiss: () -> Unit, // 用户点击弹窗外围时触发的回调（关闭弹窗）
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
                horizontalArrangement = Arrangement.Center
            ) {
                // "知道了"按钮：使用 ButtonSave 组件（保存风格），
                // 背景色改为金色 SecondColor2，点击关闭弹窗
                ButtonSave(
                    title = "知道了",
                    color = SecondColor2,
                    onClick = onDismiss
                )
            }
        }
    }
}