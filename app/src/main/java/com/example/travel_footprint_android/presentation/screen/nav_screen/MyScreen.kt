package com.example.travel_footprint_android.presentation.screen.nav_screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation.components.button.button_image_setting.ButtonImageSetting
import com.example.travel_footprint_android.presentation.components.image_random.ImageRain
import com.example.travel_footprint_android.presentation.components.image_random.setting_dialog.RainSettingDialog
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel

@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    imageRainViewModel: ImageRainViewModel = hiltViewModel(key = "image-rain")
) {
    val rainSettings by imageRainViewModel.settings.collectAsState()
    var showRainDialog by remember { mutableStateOf(false) }

    val aniImgRainAlpha by animateFloatAsState(
        targetValue = if (rainSettings.rainEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "aniImgRainAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        BGBox(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "我的")
        }

        ImageRain(
            modifier = Modifier
                .fillMaxSize()
                .alpha(aniImgRainAlpha),
            imageRainViewModel = imageRainViewModel,
        )

        ButtonImageSetting(
            modifier = Modifier.fillMaxSize(),
            onClick = { showRainDialog = true },
            showRainDialog = showRainDialog
        )
    }

    if (showRainDialog) {
        RainSettingDialog(
            imageRainViewModel = imageRainViewModel,
            onDismiss = { showRainDialog = false },
        )
    }
}
