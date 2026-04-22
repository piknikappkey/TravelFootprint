// LightPanel2.kt - 完整修复版
package com.example.travel_footprint_android.presentation2.components.light_panel2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city.LightCityScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.LightCityEditScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.panel_title.PanelTitle
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import com.example.travel_footprint_android.ui.theme.BGLight2

@Composable
fun LightPanel2(
    modifier: Modifier = Modifier,
    lightenCityMode: LightenCityMode,
    lightenViewModel: LightenViewModel = hiltViewModel(),
) {
    val uiState by lightenViewModel.uiState.collectAsState()
    val lightCityList = uiState.lightedCities
    val lightedCityCount = uiState.lightedCityCount
    val lightedProvinces = uiState.lightedProvinces
    val lightedProvinceCount = uiState.lightedProvinceCount

    var lightPanel2State by remember { mutableStateOf(LightPanel2State.ROUGH_DISPLAY) }
    var isDeleteMode by remember { mutableStateOf(false) }

    // ========== 临时选中状态（用于编辑模式） ==========
    var selectedCityCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var unselectedCityCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedProvinceCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var unselectedProvinceCodes by remember { mutableStateOf<Set<String>>(emptySet()) }

    // 计算底部内边距
    val bottomPadding = when {
        lightPanel2State == LightPanel2State.EDIT -> 80.dp
        isDeleteMode -> 80.dp
        else -> 60.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                clip = false
            )
            .background(
                color = BGLight2,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
            )
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f)
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = bottomPadding
                )
        ) {
            PanelTitle(
                lightPanel2State,
                lightedCityCount,
                lightedProvinceCount,
                { lightPanel2State = it },
                lightenCityMode
            )

            LightCityScreen(
                lightPanel2State = lightPanel2State,
                lightCityList = lightCityList,
                lightedProvinces = lightedProvinces,
                lightenCityMode = lightenCityMode,
                isDeleteMode = isDeleteMode,
                onDeleteProvince = { provinceCode ->
                    lightenViewModel.unlightProvince(provinceCode)
                    // 删除后立即刷新数据
                    lightenViewModel.refreshAllData()
                },
                onDeleteCity = { cityCode ->
                    lightenViewModel.unlightCity(cityCode)
                    // 删除后立即刷新数据
                    lightenViewModel.refreshAllData()
                }
            )

            // 编辑模式下的选择器
            LightCityEditScreen(
                lightPanel2State = lightPanel2State,
                lightenCityMode = lightenCityMode,
                initialSelectedCityCodes = selectedCityCodes,  // 传入当前已点亮的城市
                initialSelectedProvinceCodes = selectedProvinceCodes,  // 传入当前已点亮的省份
                onSelectionChanged = { selectedCities, unselectedCities, selectedProvinces, unselectedProvinces ->
                    selectedCityCodes = selectedCities
                    unselectedCityCodes = unselectedCities
                    selectedProvinceCodes = selectedProvinces
                    unselectedProvinceCodes = unselectedProvinces
                }
            )
        }

        // 底部按钮区域
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 编辑模式下的保存和取消按钮
            if (lightPanel2State == LightPanel2State.EDIT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            lightenViewModel.applyLightingChanges(
                                selectedCityCodes = selectedCityCodes,
                                unselectedCityCodes = unselectedCityCodes,
                                selectedProvinceCodes = selectedProvinceCodes,
                                unselectedProvinceCodes = unselectedProvinceCodes
                            )
                            lightPanel2State = LightPanel2State.ROUGH_DISPLAY
                            selectedCityCodes = emptySet()
                            unselectedCityCodes = emptySet()
                            selectedProvinceCodes = emptySet()
                            unselectedProvinceCodes = emptySet()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }
                    OutlinedButton(
                        onClick = {
                            lightPanel2State = LightPanel2State.ROUGH_DISPLAY
                            selectedCityCodes = emptySet()
                            unselectedCityCodes = emptySet()
                            selectedProvinceCodes = emptySet()
                            unselectedProvinceCodes = emptySet()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                }
            } else {
                // 非编辑模式下的按钮
                if (isDeleteMode) {
                    Button(
                        onClick = { isDeleteMode = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完成")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { isDeleteMode = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("取消点亮")
                        }
                        Button(
                            onClick = {
                                lightPanel2State = LightPanel2State.EDIT
                                selectedCityCodes = lightCityList.map { it.cityAdcode }.toSet()
                                selectedProvinceCodes = lightedProvinces.map { it.provinceAdcode }.toSet()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("点亮${if (lightenCityMode == LightenCityMode.CITY) "城市" else "省份"}")
                        }
                    }
                }
            }
        }
    }
}