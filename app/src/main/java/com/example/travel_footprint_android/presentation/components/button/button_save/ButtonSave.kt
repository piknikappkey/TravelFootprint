package com.example.travel_footprint_android.presentation.components.button.button_save

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark2
import com.example.travel_footprint_android.ui.theme.SaveColor

@Composable
fun ButtonSave(
    modifier: Modifier = Modifier,
    title: String = "保存",
    color: Color = SaveColor,
    paddingValues: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(25.dp),
    fontColor: Color = FontDark2,
    fontSize: TextUnit = 15.sp,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = roundedCornerShape,
                clip = false
            )
            .background(
                color = color,
                shape = roundedCornerShape
            )
            .clickable(onClick = onClick)
            .padding(paddingValues) // 内边距让背景更自然
    ) {
        TextMedium(
            text = title,
            fontSize = fontSize,
            color = fontColor,
        )
    }
}