package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_province_edit_select

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LightProvinceEditSelect() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp, horizontal = 0.dp)
    ) {
        Text(
            text = "省份选择模块",
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}
