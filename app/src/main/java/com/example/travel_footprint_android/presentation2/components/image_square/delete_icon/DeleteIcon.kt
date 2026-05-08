package com.example.travel_footprint_android.presentation2.components.image_square.delete_icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.SecondColor3
import com.example.travel_footprint_android.ui.theme.SecondColor4

@Composable
fun DeleteIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp,
    clickable: () -> Unit
) {
    Box(
        modifier = modifier
            .offset(x = iconSize / 3, y = -(iconSize / 3))
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, SecondColor3, RoundedCornerShape(16.dp))
            .background(BGLight0.copy(alpha = 0.8f))
            .clickable {
                clickable()
            },
    ) {
        Image(
            modifier = Modifier
                .size(iconSize),
            painter = painterResource(id = R.drawable.ic_delete3),
            contentDescription = "删除图标",
            colorFilter = ColorFilter.tint(SecondColor4),
        )
    }
}
