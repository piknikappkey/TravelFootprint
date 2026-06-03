package com.example.travel_footprint_android.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.screen.nav_screen.JourneyScreen
import com.example.travel_footprint_android.presentation.screen.nav_screen.MyScreen
import com.example.travel_footprint_android.presentation2.screen.LightenScreen2

@Composable
fun CustomNavHost3(
    viewIndex: Int,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    val aniViewIndex by animateFloatAsState(
        targetValue = viewIndex.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "navItemAlpha"
    )

    NavPathObj.list.forEachIndexed { index, it ->
        Box(
            modifier = Modifier.fillMaxSize().offset(x = (screenWidthDp.dp * (index - aniViewIndex)))
        ) {
            when (it) {
                NavPathObj.lighten -> LightenScreen2()
                NavPathObj.journey -> JourneyScreen()
                NavPathObj.my -> MyScreen()
            }
        }
    }
}
