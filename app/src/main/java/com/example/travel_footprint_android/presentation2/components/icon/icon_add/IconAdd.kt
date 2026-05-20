package com.example.travel_footprint_android.presentation2.components.icon.icon_add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.FontDark6

@Composable
fun IconAdd(
    modifier: Modifier = Modifier.fillMaxSize(),
    clickable: () -> Unit = {},
    iconSize: Float = .4f,
) {
    Box(
        modifier = modifier
            .background(
                color = BGLight0,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(onClick = { clickable() })
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