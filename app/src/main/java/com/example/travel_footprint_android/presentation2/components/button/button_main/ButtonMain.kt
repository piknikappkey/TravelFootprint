package com.example.travel_footprint_android.presentation2.components.button.button_main

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
import com.example.travel_footprint_android.ui.theme.BtnBgColorMain0
import com.example.travel_footprint_android.ui.theme.FontDark2

@Composable
fun ButtonMain(
    modifier: Modifier = Modifier,
    title: String,
    bgColor: Color = BtnBgColorMain0,
    paddingValues: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(8.dp),
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
                color = bgColor,
                shape = roundedCornerShape
            )
            .padding(paddingValues)
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