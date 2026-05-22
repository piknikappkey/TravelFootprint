package com.example.travel_footprint_android.presentation2.components.text.text_medium

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.ui.theme.FFHanYiKaiTiJian
import com.example.travel_footprint_android.ui.theme.FontDark3

@Composable
fun TextMedium(
    text: String,
    color: Color = FontDark3,
    firstLine: Int = 0,
    fontSize: TextUnit = 18.sp,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,               // 文字颜色
        fontSize = fontSize,                 // 字体大小，推荐使用 .sp
        fontWeight = FontWeight.W300,      // 字重，如加粗
        textAlign = textAlign,      // 文本对齐方式
        maxLines = maxLines,
        style = TextStyle(
            textIndent = TextIndent(
                firstLine = (firstLine * 16).sp, // 首行缩进2个字符的宽度
                restLine = 0.sp    // 其余行不缩进
            ),
            fontFamily = FFHanYiKaiTiJian,
        )
    )
}