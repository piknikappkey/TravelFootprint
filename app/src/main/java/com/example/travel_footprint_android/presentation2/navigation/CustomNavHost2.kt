package com.example.travel_footprint_android.presentation2.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import com.example.travel_footprint_android.presentation2.screen.JourneyScreen2
import com.example.travel_footprint_android.presentation2.screen.LightenScreen2
import com.example.travel_footprint_android.presentation2.viewmodel.nav_controller.CustomNavController

@Composable
fun CustomNavHost2(
    modifier: Modifier = Modifier
) {
    val currentDest = CustomNavController.currentDestination.value
    var currentDestOld by remember { mutableStateOf(currentDest) }

    // 白板动画
    val aniAlpha = remember { Animatable(1f) }
    val aniZIndex = remember { Animatable(0f) }

    LaunchedEffect(currentDest) {
        if(currentDest != currentDestOld) {
            aniZIndex.snapTo(10f) // 调出白板用于遮挡页面
            aniAlpha.animateTo(1f, animationSpec = tween(10)) // 调整透明度
            currentDestOld = currentDest // 切换页面
            aniAlpha.animateTo(0f, animationSpec = tween(150)) // 调回透明度
            aniZIndex.snapTo(0f) // 移除白板
        }
    }


    Box(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(aniZIndex.value)
                .alpha(aniAlpha.value)
        ) {}
        when (currentDestOld) {
            NavPathObj2.lighten -> LightenScreen2()
            NavPathObj2.journey -> JourneyScreen2()
        }
    }
}
