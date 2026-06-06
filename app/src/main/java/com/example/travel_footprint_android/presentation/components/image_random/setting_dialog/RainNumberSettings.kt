package com.example.travel_footprint_android.presentation.components.image_random.setting_dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainSettings
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel

@Composable
fun RainNumberSettings(
    settings: ImageRainSettings,
    imageRainViewModel: ImageRainViewModel,
) {
    InputFieldNumber(
        label = "涂鸦雨最大数量",
        value = settings.maxImages.toString(),
        onValueChange = { imageRainViewModel.updateMaxImages(it.toIntOrNull() ?: settings.maxImages) },
        tipText = "最大图片数量",
    )

    Spacer(Modifier.height(8.dp))

    InputFieldNumber(
        label = "涂鸦雨刷新间隔(ms)",
        value = settings.intervalMs.toString(),
        onValueChange = { imageRainViewModel.updateIntervalMs(it.toLongOrNull() ?: settings.intervalMs) },
        tipText = "刷新间隔",
    )

    Spacer(Modifier.height(8.dp))

    InputFieldNumber(
        label = "拖拽图片放大值(dp)",
        value = settings.pressScale.toString(),
        onValueChange = { imageRainViewModel.updatePressScale(it.toFloatOrNull() ?: settings.pressScale) },
        tipText = "按压放大值",
    )

    Spacer(Modifier.height(8.dp))

    InputFieldNumber(
        label = "拖拽图片旋转速度(°/s)",
        value = settings.rotationSpeed.toString(),
        onValueChange = {
            imageRainViewModel.updateRotationSpeed(
                it.toFloatOrNull() ?: settings.rotationSpeed
            )
        },
        tipText = "旋转速度",
    )
}
