package com.example.travel_footprint_android.presentation2.components.ani_shade

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.delay

@Composable
fun AniShade(
    aniStart: Boolean,
    aniOverFunc: () -> Unit,
    aniTime: Long,
    shadeShowTime: Int = 0,
    shadeHideTime: Int = 50,
    shadeStopTime: Long = aniTime,
    composable: @Composable () -> Unit = {},
) {
    // 白板动画
    val aniAlpha = remember { Animatable(1f) }
    val aniZIndex = remember { Animatable(0f) }

    // 动画计时器
    var startTime by remember { mutableStateOf<Long>(0) }

    LaunchedEffect(aniStart) {
        if(aniTime > (System.currentTimeMillis() - startTime)) {
            return@LaunchedEffect
        }
        if(aniStart) {
            Log.d("AniShade", "animation start!")
            startTime = System.currentTimeMillis()
            aniZIndex.snapTo(10f) // 调出白板用于遮挡页面
            aniAlpha.animateTo(1f, animationSpec = tween(shadeShowTime))
            delay(shadeStopTime)
            aniAlpha.animateTo(0f, animationSpec = tween(shadeHideTime)) // 调回透明度
            aniZIndex.snapTo(0f) // 移除白板
            aniOverFunc()
            Log.d("AniShade", "animation over!")
        }
    }

    // 遮罩
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(aniZIndex.value)
            .alpha(aniAlpha.value)
    ) {
        composable()
    }
}