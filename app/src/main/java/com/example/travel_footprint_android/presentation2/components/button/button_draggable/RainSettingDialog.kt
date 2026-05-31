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
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.MainColor3
import com.example.travel_footprint_android.ui.theme.SecondColor2
import kotlinx.coroutines.delay

@Composable
fun RainSettingDialog(
    rainEnabled: Boolean,
    onRainEnabledChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    isChaos: Boolean,
    onIsChaosChange: (Boolean) -> Unit,
    maxImages: Int,
    onMaxImagesChange: (Int) -> Unit,
    intervalMs: Long,
    onIntervalMsChange: (Long) -> Unit,
    minExistenceTime: Int,
    onMinExistenceTimeChange: (Int) -> Unit,
    maxExistenceTime: Int,
    onMaxExistenceTimeChange: (Int) -> Unit,
    minSize: Int,
    onMinSizeChange: (Int) -> Unit,
    maxSize: Int,
    onMaxSizeChange: (Int) -> Unit,
    minAngle: Int,
    onMinAngleChange: (Int) -> Unit,
    maxAngle: Int,
    onMaxAngleChange: (Int) -> Unit,
    pressScale: Float,
    onPressScaleChange: (Float) -> Unit,
    rotationSpeed: Float,
    onRotationSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
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
                        checked = rainEnabled,
                        onCheckedChange = onRainEnabledChange
                    )

                    Spacer(Modifier.height(8.dp))

                    SwitchRow(
                        label = "清空涂鸦雨",
                        checked = isClearing,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onClearAll()
                                isClearing = true
                            } else {
                                isClearing = false
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    InputFieldNumber(
                        label = "涂鸦雨最大数量",
                        value = maxImages.toString(),
                        onValueChange = { onMaxImagesChange(it.toIntOrNull() ?: maxImages) },
                        tipText = "最大图片数量",
                    )

                    Spacer(Modifier.height(8.dp))

                    InputFieldNumber(
                        label = "涂鸦雨刷新间隔(ms)",
                        value = intervalMs.toString(),
                        onValueChange = { onIntervalMsChange(it.toLongOrNull() ?: intervalMs) },
                        tipText = "刷新间隔",
                    )

                    Spacer(Modifier.height(8.dp))

                    TwoInputFields(
                        label = "涂鸦雨随机存在时间(ms)",
                        minVal = minExistenceTime.toString(),
                        maxVal = maxExistenceTime.toString(),
                        onMinChange = {
                            val v = it.toIntOrNull() ?: minExistenceTime
                            if (v >= 0) {
                                onMinExistenceTimeChange(v)
                                if (v > maxExistenceTime) {
                                    onMaxExistenceTimeChange(v)
                                }
                            }
                        },
                        onMaxChange = {
                            val v = it.toIntOrNull() ?: maxExistenceTime
                            if (v >= 0) {
                                onMaxExistenceTimeChange(v)
                                if (v < minExistenceTime) {
                                    onMinExistenceTimeChange(v)
                                }
                            }
                        },
                        minTip = "最小存在时间",
                        maxTip = "最大存在时间",
                    )

                    Spacer(Modifier.height(8.dp))

                    TwoInputFields(
                        label = "涂鸦雨随机大小(dp)",
                        minVal = minSize.toString(),
                        maxVal = maxSize.toString(),
                        onMinChange = {
                            val v = it.toIntOrNull() ?: minSize
                            if (v >= 0) {
                                onMinSizeChange(v)
                                if (v > maxSize) {
                                    onMaxSizeChange(v)
                                }
                            }
                        },
                        onMaxChange = {
                            val v = it.toIntOrNull() ?: maxSize
                            if (v >= 0) {
                                onMaxSizeChange(v)
                                if (v < minSize) {
                                    onMinSizeChange(v)
                                }
                            }
                        },
                        minTip = "最小尺寸",
                        maxTip = "最大尺寸",
                    )

                    Spacer(Modifier.height(8.dp))

                    TwoInputFields(
                        label = "涂鸦雨随机角度(°)",
                        minVal = minAngle.toString(),
                        maxVal = maxAngle.toString(),
                        onMinChange = {
                            val v = it.toIntOrNull() ?: minAngle
                            if (v >= 0) {
                                onMinAngleChange(v)
                                if (v > maxAngle) {
                                    onMaxAngleChange(v)
                                }
                            }
                        },
                        onMaxChange = {
                            val v = it.toIntOrNull() ?: maxAngle
                            if (v >= 0) {
                                onMaxAngleChange(v)
                                if (v < minAngle) {
                                    onMinAngleChange(v)
                                }
                            }
                        },
                        minTip = "最小角度",
                        maxTip = "最大角度",
                    )

                    Spacer(Modifier.height(8.dp))

                    InputFieldNumber(
                        label = "按压图片放大值(dp)",
                        value = pressScale.toString(),
                        onValueChange = { onPressScaleChange(it.toFloatOrNull() ?: pressScale) },
                        tipText = "按压放大值",
                    )

                    Spacer(Modifier.height(8.dp))

                    InputFieldNumber(
                        label = "按压图片旋转速度(°/s)",
                        value = rotationSpeed.toString(),
                        onValueChange = {
                            onRotationSpeedChange(
                                it.toFloatOrNull() ?: rotationSpeed
                            )
                        },
                        tipText = "旋转速度",
                    )

                    Spacer(Modifier.height(8.dp))

                    SwitchRow(
                        label = "开启涂鸦雨混乱拖拽",
                        checked = isChaos,
                        onCheckedChange = onIsChaosChange
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
