package com.example.travel_footprint_android.presentation.components.image_random.setting_dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainSettings
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel

@Composable
fun RainSwitchSettings(
    settings: ImageRainSettings,
    imageRainViewModel: ImageRainViewModel,
    isClearing: Boolean,
    onClearCheckedChange: (Boolean) -> Unit,
) {
    SwitchRow(
        label = "显示涂鸦雨",
        checked = settings.rainEnabled,
        onCheckedChange = { imageRainViewModel.updateRainEnabled(it) }
    )

    Spacer(Modifier.height(8.dp))

    SwitchRow(
        label = "清空涂鸦",
        checked = isClearing,
        onCheckedChange = onClearCheckedChange
    )

    Spacer(Modifier.height(8.dp))

    SwitchRow(
        label = "开启涂鸦雨混乱拖拽",
        checked = settings.isChaos,
        onCheckedChange = { imageRainViewModel.updateIsChaos(it) }
    )
}
