package com.example.travel_footprint_android.presentation2.components.icon.icon_edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.ui.theme.SecondColor4

@Composable
fun IconEdit(
    modifier: Modifier = Modifier.size(20.dp),
    colorFilter: ColorFilter = ColorFilter.tint(SecondColor4.copy(alpha = 0.8f)),
    onClick: () -> Unit,
) {
    Image(
        modifier = modifier
            .clickable(onClick = onClick),
        painter = painterResource(R.drawable.ic_edit),
        contentDescription = "修改图标",
        colorFilter = colorFilter,
    )
}