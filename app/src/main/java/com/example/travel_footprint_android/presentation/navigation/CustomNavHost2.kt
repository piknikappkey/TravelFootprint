package com.example.travel_footprint_android.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.presentation.screen.nav_screen.JourneyScreen
import com.example.travel_footprint_android.presentation.screen.nav_screen.MyScreen
import com.example.travel_footprint_android.presentation.screen.viewmodel.NavigationViewModel
import com.example.travel_footprint_android.presentation2.screen.LightenScreen2
import com.example.travel_footprint_android.ui.theme.BGLight0

@Composable
fun CustomNavHost2(
    navigationViewModel: NavigationViewModel,
) {
    val currentDest by navigationViewModel.currentDestination.collectAsStateWithLifecycle()

    AnimatedContent(
        modifier = Modifier.background(BGLight0),
        targetState = currentDest,
        transitionSpec = {
            val direction = getDirection(initialState, targetState)
            slideInHorizontally(
                animationSpec = tween(durationMillis = 500),
                initialOffsetX = { fullWidth -> direction * fullWidth }
            ).togetherWith(
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = 500),
                    targetOffsetX = { fullWidth -> -direction * fullWidth }
                )
            )
        },
        label = "page_transition"
    ) { dest ->
        when (dest) {
            NavPathObj.lighten -> LightenScreen2()
            NavPathObj.journey -> JourneyScreen()
            NavPathObj.my -> MyScreen()
        }
    }
}

private fun getDirection(initial: NavPath, target: NavPath): Int {
    val initialIndex = NavPathObj.list.indexOf(initial)
    val targetIndex = NavPathObj.list.indexOf(target)
    return if (targetIndex > initialIndex) 1 else -1
}
