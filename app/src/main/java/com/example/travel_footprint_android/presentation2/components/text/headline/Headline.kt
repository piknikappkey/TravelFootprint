package com.example.travel_footprint_android.presentation2.components.text.headline

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.ui.theme.FFDaMengKaTongTi
import com.example.travel_footprint_android.ui.theme.FontDark4

@Composable
fun Headline(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = FontDark4,
    letterSpacing: TextUnit = 3.sp,
    lineHeight: TextUnit = 36.sp,
    textAlign: TextAlign? = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.W500,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,               // 文字颜色
        fontSize = fontSize,                 // 字体大小，推荐使用 .sp
        fontWeight = fontWeight,      // 字重，如加粗
        textAlign = textAlign,      // 文本对齐方式
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        style = TextStyle(
            fontFamily = FFDaMengKaTongTi
        )
    )
}