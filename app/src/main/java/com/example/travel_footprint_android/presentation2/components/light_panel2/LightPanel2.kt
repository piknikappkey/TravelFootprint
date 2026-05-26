// LightPanel2.kt - 完整修复版
package com.example.travel_footprint_android.presentation2.components.light_panel2

import android.util.Log
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city.LightCityScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.LightCityEditScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.panel_title.PanelTitle
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

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
        lightPanel2State == LightPanel2State.EDIT -> 88.dp
        isDeleteMode -> 88.dp
        else -> 72.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                clip = false
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f)
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
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

            // 被点亮数据点亮列表区域
            LightCityScreenWithState(
                lightPanel2State = lightPanel2State,
                lightCityList = lightCityList,
                lightedProvinces = lightedProvinces,
                lightenCityMode = lightenCityMode,
                isDeleteMode = isDeleteMode,
                onDeleteProvince = { provinceCode ->
                    lightenViewModel.unlightProvince(provinceCode)
                },
                onDeleteCity = { cityCode ->
                    lightenViewModel.unlightCity(cityCode)
                }
            )

            // 编辑模式选择器
            if (lightPanel2State == LightPanel2State.EDIT) {
                LightCityEditScreen(
                    lightPanel2State = lightPanel2State,
                    lightenCityMode = lightenCityMode,
                    initialSelectedCityCodes = selectedCityCodes,
                    initialSelectedProvinceCodes = selectedProvinceCodes,
                    onSelectionChanged = { selectedCities, unselectedCities, selectedProvinces, unselectedProvinces ->
                        selectedCityCodes = selectedCities
                        unselectedCityCodes = unselectedCities
                        selectedProvinceCodes = selectedProvinces
                        unselectedProvinceCodes = unselectedProvinces
                    }
                )
            }
        }

        // 底部按钮区域
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "保存",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            lightPanel2State = LightPanel2State.ROUGH_DISPLAY
                            selectedCityCodes = emptySet()
                            unselectedCityCodes = emptySet()
                            selectedProvinceCodes = emptySet()
                            unselectedProvinceCodes = emptySet()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                        // 移除 border 参数，使用默认边框
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // 非编辑模式下的按钮
                if (isDeleteMode) {
                    Button(
                        onClick = {
                            isDeleteMode = false
                            lightenViewModel.refreshAllData()
                            Log.d("LightPanel2", "刷新数据完成")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "完成",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isDeleteMode = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6B7280)
                            )
                            // 移除 border 参数，使用默认边框
                        ) {
                            Text(
                                text = "取消点亮",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = {
                                lightPanel2State = LightPanel2State.EDIT
                                selectedCityCodes = lightCityList.map { it.cityAdcode }.toSet()
                                selectedProvinceCodes =
                                    lightedProvinces.map { it.provinceAdcode }.toSet()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B7280)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "点亮${if (lightenCityMode == LightenCityMode.CITY) "城市" else "省份"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ 单独提取，使用 key 独立重组（移到外部）
@Composable
fun LightCityScreenWithState(
    lightPanel2State: LightPanel2State,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightenCityMode: LightenCityMode,
    isDeleteMode: Boolean,
    onDeleteProvince: (String) -> Unit,
    onDeleteCity: (String) -> Unit
) {
    key(lightPanel2State) {
        LightCityScreen(
            lightPanel2State = lightPanel2State,
            lightCityList = lightCityList,
            lightedProvinces = lightedProvinces,
            lightenCityMode = lightenCityMode,
            isDeleteMode = isDeleteMode,
            onDeleteProvince = onDeleteProvince,
            onDeleteCity = onDeleteCity
        )
    }
}