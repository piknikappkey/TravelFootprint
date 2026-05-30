// app/src/main/java/com/example/travel_footprint_android/MainActivity.kt
package com.example.travel_footprint_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.travel_footprint_android.presentation2.screen.MainScreen2
import com.example.travel_footprint_android.utils.DebugHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var debugHelper: DebugHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainScreen2(debugHelper = null)
        }
    }
}
