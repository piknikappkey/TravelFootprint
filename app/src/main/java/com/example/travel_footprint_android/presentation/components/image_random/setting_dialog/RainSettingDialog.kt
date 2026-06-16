package com.example.travel_footprint_android.presentation.components.image_random.setting_dialog

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.dialog.DialogBox
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainViewModel
import kotlinx.coroutines.delay

@Composable
fun RainSettingDialog(
    imageRainViewModel: ImageRainViewModel = hiltViewModel(key = "image-rain"),
    onDismiss: () -> Unit,
) {
    val settings by imageRainViewModel.settings.collectAsState()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val dialogHeight = screenHeightDp * 0.6f
    var isClearing by remember { mutableStateOf(false) }

    LaunchedEffect(isClearing) {
        if(isClearing) {
            delay(100)
            isClearing = false
        }
    }

    DialogBox(
        R.drawable.bg_rectangular_1__2__3,
        onDismissRequest = onDismiss
    ) {
        RainSettingDialogContent(
            settings = settings,
            imageRainViewModel = imageRainViewModel,
            isClearing = isClearing,
            onClearCheckedChange = { checked ->
                if (checked) {
                    imageRainViewModel.clearAll()
                    isClearing = true
                } else {
                    isClearing = false
                }
            },
            modifier = Modifier.height(dialogHeight),
        )
    }
}
