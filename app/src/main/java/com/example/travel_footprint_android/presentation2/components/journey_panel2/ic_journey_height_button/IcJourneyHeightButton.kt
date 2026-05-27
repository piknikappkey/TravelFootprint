package com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun IcJourneyHeightButton(
    state: Boolean,
    onClick: () -> Unit,
    aniTime: Int = 400
) {
    // 旋转动画
    val angle by animateFloatAsState(
        targetValue = if(state) 180f else 0f,
        animationSpec = tween(durationMillis = aniTime), // 500 毫秒的旋转动画
        label = "clickRotate"
    )

    Image(
        modifier = Modifier
            .size(22.dp)
            .rotate(angle)
            .clickable(onClick = onClick),
        painter = painterResource(id = R.drawable.ic_up2),
        contentDescription = "返回图标",
        colorFilter = ColorFilter.tint(SecondColor3),
    )
}