// app/src/main/java/com/example/travel_footprint_android/ui/theme/Type.kt
package com.example.travel_footprint_android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R

// 设置字体样式
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* 其他默认文字样式 */
)

// 大萌卡通体
val FFDaMengKaTongTi = FontFamily(
    Font(R.font.da_meng_ka_tong_ti, FontWeight.Normal),
    Font(R.font.da_meng_ka_tong_ti, FontWeight.Bold)
)

// 软萌初恋体
val FFRuanMengChuLianTi = FontFamily(
    Font(R.font.ruan_meng_chu_lian_ti, FontWeight.Normal),
    Font(R.font.ruan_meng_chu_lian_ti, FontWeight.Bold)
)