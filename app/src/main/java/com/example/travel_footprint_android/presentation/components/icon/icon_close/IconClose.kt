package com.example.travel_footprint_android.presentation.components.icon.icon_close

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun IconClose(
    iconSize: Int = 16,
    shape: Shape = RoundedCornerShape(50.dp),
    bgColor: Color = SecondColor3,
    paddingValues: PaddingValues = PaddingValues(1.dp),
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                color = bgColor,
                shape = shape
            )
            .padding(paddingValues)
            .clickable(onClick = onClick)
    ) {
        Image(
            modifier = Modifier
                .size(iconSize.dp), // 图标尺寸
            painter = painterResource(id = R.drawable.ic_delete3),
            contentDescription = "删除图标",
            colorFilter = ColorFilter.tint(Color(0xffffffff)),
        )
    }
}