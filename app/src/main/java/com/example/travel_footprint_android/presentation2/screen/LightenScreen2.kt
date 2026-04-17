package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.components.SVGMap
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2

@Composable
fun LightenScreen2() {
    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        SVGMap(modifier = Modifier.weight(1f))
        LightPanel2()
    }
}
