// app/src/main/java/com/example/travel_footprint_android/presentation2/components/light_panel2/light_city_edit/LightCityEditScreen.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_select.LightCityEidtSelect
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_province_edit_select.LightProvinceEditSelect
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode


@Composable
fun LightCityEditScreen(
    lightPanel2State: LightPanel2State,
    lightenCityMode: LightenCityMode,
    viewModel: LightenViewModel = hiltViewModel(),
    initialSelectedCityCodes: Set<String> = emptySet(),
    initialSelectedProvinceCodes: Set<String> = emptySet(),
    onSelectionChanged: (
        selectedCityCodes: Set<String>,
        unselectedCityCodes: Set<String>,
        selectedProvinceCodes: Set<String>,
        unselectedProvinceCodes: Set<String>
    ) -> Unit = { _, _, _, _ -> }
) {
    // 只在进入编辑模式时初始化
    var selectedCityCodes by remember(lightPanel2State) {
        mutableStateOf(initialSelectedCityCodes)
    }
    var selectedProvinceCodes by remember(lightPanel2State) {
        mutableStateOf(initialSelectedProvinceCodes)
    }

    // 自动计算未选中的（被取消点亮的）
    val unselectedCityCodes by remember(selectedCityCodes, initialSelectedCityCodes) {
        derivedStateOf { initialSelectedCityCodes subtract selectedCityCodes }
    }
    val unselectedProvinceCodes by remember(selectedProvinceCodes, initialSelectedProvinceCodes) {
        derivedStateOf { initialSelectedProvinceCodes subtract selectedProvinceCodes }
    }

    // 当选中状态变化时，通知父组件
    LaunchedEffect(selectedCityCodes, unselectedCityCodes, selectedProvinceCodes, unselectedProvinceCodes) {
        onSelectionChanged(
            selectedCityCodes, unselectedCityCodes,
            selectedProvinceCodes, unselectedProvinceCodes
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        if (lightPanel2State != LightPanel2State.EDIT) return@Column

        if (lightenCityMode == LightenCityMode.CITY) {
            LightCityEidtSelect(
                selectedCityCodes = selectedCityCodes,
                selectedProvinceCodes = selectedProvinceCodes,
                onCitySelectionChange = { cityCode, isSelected ->
                    selectedCityCodes = if (isSelected) {
                        selectedCityCodes + cityCode
                    } else {
                        selectedCityCodes - cityCode
                    }
                },
                onProvinceSelectionChange = { provinceCode, isSelected ->
                    selectedProvinceCodes = if (isSelected) {
                        selectedProvinceCodes + provinceCode
                    } else {
                        selectedProvinceCodes - provinceCode
                    }
                }
            )
        } else {
            LightProvinceEditSelect(
                selectedProvinceCodes = selectedProvinceCodes,
                onProvinceSelectionChange = { provinceCode, isSelected ->
                    selectedProvinceCodes = if (isSelected) {
                        selectedProvinceCodes + provinceCode
                    } else {
                        selectedProvinceCodes - provinceCode
                    }
                }
            )
        }
    }
}