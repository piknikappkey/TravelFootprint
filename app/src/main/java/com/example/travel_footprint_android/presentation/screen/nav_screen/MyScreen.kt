package com.example.travel_footprint_android.presentation.screen.nav_screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation.components.button.button_image_setting.ButtonImageSetting
import com.example.travel_footprint_android.presentation.components.image_random.ImageRain
import com.example.travel_footprint_android.presentation.components.image_random.setting_dialog.RainSettingDialog
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel
import com.example.travel_footprint_android.presentation.components.journey_map.weather.WeatherViewModel
import com.example.travel_footprint_android.presentation.components.milestone.MilestoneContent
import com.example.travel_footprint_android.presentation.components.setting_view.SettingView
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation.viewmodel.MilestoneViewModel

@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    imageRainViewModel: ImageRainViewModel = hiltViewModel(key = "image-rain"),
    lightenViewModel: LightenViewModel = hiltViewModel(),
    milestoneViewModel: MilestoneViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(),
) {
    val rainSettings by imageRainViewModel.settings.collectAsState()
    var showRainDialog by remember { mutableStateOf(false) }

    val uiState by lightenViewModel.uiState.collectAsState()
    val lightCityList = uiState.lightedCities
    val lightedProvinceCount = uiState.lightedProvinceCount
    val allFootprints by lightenViewModel.allFootprints.collectAsState()

    // 将 LightenViewModel 的数据同步到 MilestoneViewModel
    LaunchedEffect(lightCityList, lightedProvinceCount, allFootprints) {
        milestoneViewModel.updateData(allFootprints, lightedProvinceCount, lightCityList)
    }

    val aniImgRainAlpha by animateFloatAsState(
        targetValue = if (rainSettings.rainEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "aniImgRainAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        BGBox(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Headline(
                    text = "我的",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.height(16.dp))

                MilestoneContent(
                    milestoneViewModel = milestoneViewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingView(
                    weatherViewModel = weatherViewModel,
                    onOpenRainSettings = { showRainDialog = true },
                )
            }
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


