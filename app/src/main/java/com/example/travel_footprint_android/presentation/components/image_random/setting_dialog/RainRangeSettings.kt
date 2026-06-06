package com.example.travel_footprint_android.presentation.components.image_random.setting_dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainSettings
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel

@Composable
fun RainRangeSettings(
    settings: ImageRainSettings,
    imageRainViewModel: ImageRainViewModel,
) {
    TwoInputFields(
        label = "涂鸦雨随机存在时间(ms)",
        minVal = settings.minExistenceTime.toString(),
        maxVal = settings.maxExistenceTime.toString(),
        onMinChange = {
            val v = it.toIntOrNull() ?: settings.minExistenceTime
            if (v >= 0) {
                imageRainViewModel.updateMinExistenceTime(v)
                if (v > settings.maxExistenceTime) {
                    imageRainViewModel.updateMaxExistenceTime(v)
                }
            }
        },
        onMaxChange = {
            val v = it.toIntOrNull() ?: settings.maxExistenceTime
            if (v >= 0) {
                imageRainViewModel.updateMaxExistenceTime(v)
                if (v < settings.minExistenceTime) {
                    imageRainViewModel.updateMinExistenceTime(v)
                }
            }
        },
        minTip = "最小存在时间",
        maxTip = "最大存在时间",
    )

    Spacer(Modifier.height(8.dp))

    TwoInputFields(
        label = "涂鸦雨随机大小(dp)",
        minVal = settings.minSize.toString(),
        maxVal = settings.maxSize.toString(),
        onMinChange = {
            val v = it.toIntOrNull() ?: settings.minSize
            if (v >= 0) {
                imageRainViewModel.updateMinSize(v)
                if (v > settings.maxSize) {
                    imageRainViewModel.updateMaxSize(v)
                }
            }
        },
        onMaxChange = {
            val v = it.toIntOrNull() ?: settings.maxSize
            if (v >= 0) {
                imageRainViewModel.updateMaxSize(v)
                if (v < settings.minSize) {
                    imageRainViewModel.updateMinSize(v)
                }
            }
        },
        minTip = "最小尺寸",
        maxTip = "最大尺寸",
    )

    Spacer(Modifier.height(8.dp))

    TwoInputFields(
        label = "涂鸦雨随机角度(°)",
        minVal = settings.minAngle.toString(),
        maxVal = settings.maxAngle.toString(),
        onMinChange = {
            val v = it.toIntOrNull() ?: settings.minAngle
            if (v >= 0) {
                imageRainViewModel.updateMinAngle(v)
                if (v > settings.maxAngle) {
                    imageRainViewModel.updateMaxAngle(v)
                }
            }
        },
        onMaxChange = {
            val v = it.toIntOrNull() ?: settings.maxAngle
            if (v >= 0) {
                imageRainViewModel.updateMaxAngle(v)
                if (v < settings.minAngle) {
                    imageRainViewModel.updateMinAngle(v)
                }
            }
        },
        minTip = "最小角度",
        maxTip = "最大角度",
    )
}
