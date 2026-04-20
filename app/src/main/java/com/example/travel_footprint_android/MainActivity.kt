// app/src/main/java/com/example/travel_footprint_android/MainActivity.kt
package com.example.travel_footprint_android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.example.travel_footprint_android.presentation2.screen.MainScreen2
import com.example.travel_footprint_android.utils.DebugHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var debugHelper: DebugHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求位置权限
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )

        setContent {
            // 传入 debugHelper 即可显示调试按钮，不传则无调试按钮
            MainScreen2(debugHelper = debugHelper)
        }
    }
}