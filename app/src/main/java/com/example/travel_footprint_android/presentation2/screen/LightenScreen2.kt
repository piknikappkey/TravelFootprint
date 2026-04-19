package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2
import com.example.travel_footprint_android.presentation2.components.svg_map.SVGMap

@Composable
fun LightenScreen2() {
    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        SVGMap(Modifier.weight(1f))
        LightPanel2()
    }
}
