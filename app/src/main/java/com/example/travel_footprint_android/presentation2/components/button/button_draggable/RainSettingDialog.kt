package com.example.travel_footprint_android.presentation2.components.button.button_draggable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.image_random.ImageRainViewModel
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.MainColor3
import com.example.travel_footprint_android.ui.theme.SecondColor2
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

    Dialog(
        onDismissRequest = onDismiss
    ) {
        BGBox {
            BGImgBox(
                listOf(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3),
            ) {
                Column(
                    modifier = Modifier
                        .height(dialogHeight)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Headline(
                        modifier = Modifier.fillMaxWidth(),
                        text = "涂鸦雨设置",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))

                    SwitchRow(
                        label = "显示涂鸦雨",
                        checked = settings.rainEnabled,
                        onCheckedChange = { imageRainViewModel.updateRainEnabled(it) }
                    )

                    Spacer(Modifier.height(8.dp))

                    SwitchRow(
                        label = "清空涂鸦雨",
                        checked = isClearing,
                        onCheckedChange = { checked ->
                            if (checked) {
                                imageRainViewModel.clearAll()
                                isClearing = true
                            } else {
                                isClearing = false
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

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

                    Spacer(Modifier.height(8.dp))

                    InputFieldNumber(
                        label = "按压图片放大值(dp)",
                        value = settings.pressScale.toString(),
                        onValueChange = { imageRainViewModel.updatePressScale(it.toFloatOrNull() ?: settings.pressScale) },
                        tipText = "按压放大值",
                    )

                    Spacer(Modifier.height(8.dp))

                    InputFieldNumber(
                        label = "按压图片旋转速度(°/s)",
                        value = settings.rotationSpeed.toString(),
                        onValueChange = {
                            imageRainViewModel.updateRotationSpeed(
                                it.toFloatOrNull() ?: settings.rotationSpeed
                            )
                        },
                        tipText = "旋转速度",
                    )

                    Spacer(Modifier.height(8.dp))

                    SwitchRow(
                        label = "开启涂鸦雨混乱拖拽",
                        checked = settings.isChaos,
                        onCheckedChange = { imageRainViewModel.updateIsChaos(it) }
                    )

                    Spacer(Modifier.height(14.dp))
                    ButtonSave(
                        title = "关闭",
                        color = SecondColor2,
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextMedium(
            text = label,
            fontSize = 15.sp,
        )
        Switch(
            modifier = Modifier.scale(.7f),
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MainColor3,
                checkedTrackColor = MainColor3.copy(alpha = 0.5f),
            )
        )
    }
}

@Composable
private fun InputFieldNumber(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tipText: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextMedium(
            text = label,
            fontSize = 13.sp,
        )
        InputText3(
            value = value,
            onValueChange = onValueChange,
            tipText = tipText,
            padding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TwoInputFields(
    label: String,
    minVal: String,
    maxVal: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    minTip: String,
    maxTip: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextMedium(
            text = label,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputText3(
                value = minVal,
                onValueChange = onMinChange,
                tipText = minTip,
                padding = PaddingValues(horizontal = 0.dp),
                modifier = Modifier.weight(1f),
            )
            TextMedium(text = "~", fontSize = 14.sp)
            InputText3(
                value = maxVal,
                onValueChange = onMaxChange,
                tipText = maxTip,
                padding = PaddingValues(horizontal = 0.dp),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
