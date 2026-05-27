//package com.example.travel_footprint_android.presentation2.components.button.button_light_screen
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.travel_footprint_android.data.entity.LightedCity
//import com.example.travel_footprint_android.data.entity.LightedProvince
//import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
//import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
//
//@Composable
//private fun BottomButtons(
//    lightPanel2State: LightPanel2State,
//    isDeleteMode: Boolean,
//    lightenCityMode: LightenCityMode,
//    lightCityList: List<LightedCity>,
//    lightedProvinces: List<LightedProvince>,
//    selectedCityCodes: Set<String>,
//    unselectedCityCodes: Set<String>,
//    selectedProvinceCodes: Set<String>,
//    unselectedProvinceCodes: Set<String>,
//    onDeleteModeChange: (Boolean) -> Unit,
//    onStateChange: (LightPanel2State) -> Unit,
//    onSelectionInit: () -> Unit,
//    onSelectionReset: () -> Unit,
//    onSave: () -> Unit
//) {
//    Box {
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            if (lightPanel2State == LightPanel2State.EDIT) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Button(
//                        onClick = {
//                            onSave()
//                            onStateChange(LightPanel2State.ROUGH_DISPLAY)
//                            onSelectionReset()
//                        },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("保存")
//                    }
//                    OutlinedButton(
//                        onClick = {
//                            onStateChange(LightPanel2State.ROUGH_DISPLAY)
//                            onSelectionReset()
//                        },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("取消")
//                    }
//                }
//            } else {
//                if (isDeleteMode) {
//                    Button(
//                        onClick = { onDeleteModeChange(false) },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("完成")
//                    }
//                } else {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Button(
//                            onClick = { onDeleteModeChange(true) },
//                            modifier = Modifier.weight(1f),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = MaterialTheme.colorScheme.error
//                            )
//                        ) {
//                            Text("取消点亮")
//                        }
//                        Button(
//                            onClick = {
//                                onStateChange(LightPanel2State.EDIT)
//                                onSelectionInit()
//                            },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Text("点亮${if (lightenCityMode == LightenCityMode.CITY) "城市" else "省份"}")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}