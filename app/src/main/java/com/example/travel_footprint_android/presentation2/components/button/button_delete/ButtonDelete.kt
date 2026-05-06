package com.example.travel_footprint_android.presentation2.components.button.button_delete

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.ui.theme.DeleteColor
import com.example.travel_footprint_android.ui.theme.FontDark2

@Composable
fun ButtonDelete(
    modifier: Modifier = Modifier,
    title: String = "删除",
    color: Color = DeleteColor,
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
//            .border(2.dp, color.copy(.5f), roundedCornerShape)
            .padding(paddingValues) // 内边距让背景更自然
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            modifier = Modifier.alpha(.8f),
            fontSize = fontSize,
            color = fontColor,
        )
    }
}