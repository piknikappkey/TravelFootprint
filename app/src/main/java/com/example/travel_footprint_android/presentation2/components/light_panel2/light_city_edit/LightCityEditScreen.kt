// app/src/main/java/com/example/travel_footprint_android/presentation2/components/light_panel2/light_city_edit/LightCityEditScreen.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
    onSelectionChanged: (
        selectedCityCodes: Set<String>,
        unselectedCityCodes: Set<String>,
        selectedProvinceCodes: Set<String>,
        unselectedProvinceCodes: Set<String>
    ) -> Unit = { _, _, _, _ -> }
) {
    var selectedCityCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedProvinceCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var unselectedCityCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var unselectedProvinceCodes by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
    ) {
        if (lightPanel2State != LightPanel2State.EDIT) return

        if (lightenCityMode == LightenCityMode.CITY) {
            LightCityEidtSelect(
                selectedCityCodes = selectedCityCodes,
                selectedProvinceCodes = selectedProvinceCodes,
                onCitySelectionChange = { cityCode, isSelected ->
                    if (isSelected) {
                        selectedCityCodes = selectedCityCodes + cityCode
                        unselectedCityCodes = unselectedCityCodes - cityCode
                    } else {
                        selectedCityCodes = selectedCityCodes - cityCode
                        unselectedCityCodes = unselectedCityCodes + cityCode
                    }
                    onSelectionChanged(
                        selectedCityCodes, unselectedCityCodes,
                        selectedProvinceCodes, unselectedProvinceCodes
                    )
                },
                onProvinceSelectionChange = { provinceCode, isSelected ->
                    if (isSelected) {
                        selectedProvinceCodes = selectedProvinceCodes + provinceCode
                        unselectedProvinceCodes = unselectedProvinceCodes - provinceCode
                    } else {
                        selectedProvinceCodes = selectedProvinceCodes - provinceCode
                        unselectedProvinceCodes = unselectedProvinceCodes + provinceCode
                    }
                    onSelectionChanged(
                        selectedCityCodes, unselectedCityCodes,
                        selectedProvinceCodes, unselectedProvinceCodes
                    )
                }
            )
        } else {
            LightProvinceEditSelect(
                selectedProvinceCodes = selectedProvinceCodes,
                onProvinceSelectionChange = { provinceCode, isSelected ->
                    if (isSelected) {
                        selectedProvinceCodes = selectedProvinceCodes + provinceCode
                        unselectedProvinceCodes = unselectedProvinceCodes - provinceCode
                    } else {
                        selectedProvinceCodes = selectedProvinceCodes - provinceCode
                        unselectedProvinceCodes = unselectedProvinceCodes + provinceCode
                    }
                    onSelectionChanged(
                        selectedCityCodes, unselectedCityCodes,
                        selectedProvinceCodes, unselectedProvinceCodes
                    )
                }
            )
        }
    }
}