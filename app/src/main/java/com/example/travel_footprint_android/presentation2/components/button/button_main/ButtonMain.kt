package com.example.travel_footprint_android.presentation2.components.button.button_main

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
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.BtnBgColorMain0

@Composable
fun ButtonMain(
    modifier: Modifier = Modifier,
    bgColor: Color = BtnBgColorMain0,
    paddingValues: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(8.dp),
    onClick: () -> Unit,
    content: @Composable () -> Unit,
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
        content()
    }
}