package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_gettime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LightCityEditGettime() {
    var lightCityTime = 0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 0.dp)
    ) {
        Text(
            text = "时间选择模块",
            modifier = Modifier
                .align(Alignment.Center)
        )
        Text(
            text = "时间选择模块",
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}
