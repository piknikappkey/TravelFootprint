package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd

@Composable
fun FootprintListAddIcon(
    modifier: Modifier = Modifier,
    clickable: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .padding(10.dp)
    ) {
        BGBox {
            IconAdd(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp),
                clickable = clickable,
            )
        }
    }
}