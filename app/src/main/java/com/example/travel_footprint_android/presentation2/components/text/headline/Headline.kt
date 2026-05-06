package com.example.travel_footprint_android.presentation2.components.text.headline

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun Headline(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = FontDark4,               // 文字颜色
        fontSize = 20.sp,                 // 字体大小，推荐使用 .sp
        fontWeight = FontWeight.W500,      // 字重，如加粗
        textAlign = TextAlign.Start,      // 文本对齐方式
        letterSpacing = 3.sp,
        lineHeight = 36.sp,
    )
}