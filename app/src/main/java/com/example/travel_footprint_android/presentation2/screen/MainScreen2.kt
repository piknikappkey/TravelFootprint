package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation2.components.debug.DebugOverlay
import com.example.travel_footprint_android.presentation2.navigation.CustomNavHost2
import com.example.travel_footprint_android.presentation2.navigation.Navigation2
import com.example.travel_footprint_android.utils.DebugHelper

@Composable
fun MainScreen2(
    debugHelper: DebugHelper? = null
) {
    var showSplash by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                CustomNavHost2(modifier = Modifier.weight(1f))
                Navigation2(modifier = Modifier.wrapContentHeight())
            }
            if (debugHelper != null) {
                DebugOverlay(
                    debugHelper = debugHelper,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (showSplash) {
            SplashScreen(onFinished = { showSplash = false })
        }
    }
}
