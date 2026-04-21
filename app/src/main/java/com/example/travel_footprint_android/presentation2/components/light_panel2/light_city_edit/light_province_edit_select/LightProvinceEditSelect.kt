// app/src/main/java/com/example/travel_footprint_android/presentation2/components/light_panel2/light_city_edit/light_province_edit_select/LightProvinceEditSelect.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_province_edit_select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel

@Composable
fun LightProvinceEditSelect(
    selectedProvinceCodes: Set<String> = emptySet(),
    onProvinceSelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    viewModel: LightenViewModel = hiltViewModel()
) {
    val allProvinces by viewModel.allProvinces.collectAsStateWithLifecycle(initialValue = emptyList())
    val lightedProvinceCodes by viewModel.lightedProvinceCodes.collectAsStateWithLifecycle(initialValue = emptySet())

    // 过滤掉已点亮的省份，只显示未点亮的
    val unlightedProvinces = allProvinces.filter { province ->
        !lightedProvinceCodes.contains(province.adcode)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .height(400.dp)
    ) {
        Text(
            "选择省份",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
        Divider()

        if (unlightedProvinces.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("所有省份都已点亮", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Divider()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(unlightedProvinces, key = { it.adcode }) { province ->
                    ProvinceItemForProvince(
                        province = province,
                        isLighted = false,
                        isTempSelected = selectedProvinceCodes.contains(province.adcode),
                        onCheckChange = { isChecked ->
                            onProvinceSelectionChange(province.adcode, isChecked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProvinceItemForProvince(
    province: Province,
    isLighted: Boolean,
    isTempSelected: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = province.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isLighted || isTempSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Checkbox(
            checked = isLighted || isTempSelected,
            onCheckedChange = onCheckChange
        )
    }
}