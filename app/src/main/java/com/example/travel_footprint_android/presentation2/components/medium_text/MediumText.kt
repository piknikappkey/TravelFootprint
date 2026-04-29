package com.example.travel_footprint_android.presentation2.components.medium_text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun MediumText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = FontDark4,               // 文字颜色
        fontSize = 16.sp,                 // 字体大小，推荐使用 .sp
        fontWeight = FontWeight.W300,      // 字重，如加粗
        textAlign = TextAlign.Start,      // 文本对齐方式
        style = TextStyle(
            textIndent = TextIndent(
                firstLine = 32.sp, // 首行缩进2个字符的宽度
                restLine = 0.sp    // 其余行不缩进
            )
        )
    )
}