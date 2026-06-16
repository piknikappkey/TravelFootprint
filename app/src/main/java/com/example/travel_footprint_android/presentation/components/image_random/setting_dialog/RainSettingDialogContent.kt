package com.example.travel_footprint_android.presentation.components.image_random.setting_dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainSettings
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel
import com.example.travel_footprint_android.presentation.components.text.headline.Headline

@Composable
fun RainSettingDialogContent(
    settings: ImageRainSettings,
    imageRainViewModel: ImageRainViewModel,
    isClearing: Boolean,
    onClearCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(10.dp, 12.dp, 10.dp, 6.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Headline(
            modifier = Modifier.fillMaxWidth(),
            text = "涂鸦雨设置",
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        RainSwitchSettings(
            settings = settings,
            imageRainViewModel = imageRainViewModel,
            isClearing = isClearing,
            onClearCheckedChange = onClearCheckedChange,
        )

        Spacer(Modifier.height(8.dp))

        RainNumberSettings(
            settings = settings,
            imageRainViewModel = imageRainViewModel,
        )

        Spacer(Modifier.height(8.dp))

        RainRangeSettings(
            settings = settings,
            imageRainViewModel = imageRainViewModel,
        )

        Spacer(Modifier.height(8.dp))
    }
}
