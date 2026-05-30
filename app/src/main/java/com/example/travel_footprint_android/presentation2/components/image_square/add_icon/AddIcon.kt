package com.example.travel_footprint_android.presentation2.components.image_square.add_icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.FontDark6

@Composable
fun AddIcon(
    modifier: Modifier = Modifier,
    iconSize: Float,
    clickable: () -> Unit
) {
    Box(
        modifier = modifier
            .background(BGLight0)
            .clickable {
                clickable()
            }
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(iconSize),
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = "添加图标",
            colorFilter = ColorFilter.tint(FontDark6),
        )
    }
}