// app/src/main/java/com/example/travel_footprint_android/presentation2/components/light_panel2/light_city_edit/light_city_edit_select/LightCityEidtSelect.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_select

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel

@Composable
fun LightCityEidtSelect(
    selectedCityCodes: Set<String> = emptySet(),
    selectedProvinceCodes: Set<String> = emptySet(),
    onCitySelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    onProvinceSelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    viewModel: LightenViewModel = hiltViewModel()
) {
    val allProvinces by viewModel.allProvinces.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProvince by remember { mutableStateOf<Province?>(null) }
    val citiesByProvince by viewModel.getCitiesByProvince(selectedProvince?.adcode ?: "")
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val lightedCityCodes by viewModel.lightedCityCodes.collectAsStateWithLifecycle(initialValue = emptySet())
    val lightedProvinceCodes by viewModel.lightedProvinceCodes.collectAsStateWithLifecycle(initialValue = emptySet())

    Row(
        modifier = Modifier
            .fillMaxSize()
            .height(400.dp)
    ) {
        ProvinceList(
            provinces = allProvinces,
            selectedProvince = selectedProvince,
            lightedProvinceCodes = lightedProvinceCodes,
            selectedProvinceCodes = selectedProvinceCodes,
            onProvinceClick = { province ->
                selectedProvince = province
            },
            onProvinceCheckChange = onProvinceSelectionChange,
            modifier = Modifier.weight(1f)
        )

        Divider(modifier = Modifier.width(1.dp))

        CityList(
            cities = citiesByProvince,
            selectedProvince = selectedProvince,
            lightedCityCodes = lightedCityCodes,
            selectedCityCodes = selectedCityCodes,
            onCityCheckChange = onCitySelectionChange,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun ProvinceList(
    provinces: List<Province>,
    selectedProvince: Province?,
    lightedProvinceCodes: Set<String>,
    selectedProvinceCodes: Set<String>,
    onProvinceClick: (Province) -> Unit,
    onProvinceCheckChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("省份", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
        Divider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(provinces, key = { it.adcode }) { province ->
                ProvinceItem(
                    province = province,
                    isSelected = selectedProvince?.adcode == province.adcode,
                    isLighted = lightedProvinceCodes.contains(province.adcode),
                    isTempSelected = selectedProvinceCodes.contains(province.adcode),
                    onProvinceClick = { onProvinceClick(province) },
                    onCheckChange = { isChecked ->
                        onProvinceCheckChange(province.adcode, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun ProvinceItem(
    province: Province,
    isSelected: Boolean,
    isLighted: Boolean,
    isTempSelected: Boolean,
    onProvinceClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProvinceClick() }
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
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

@Composable
fun CityList(
    cities: List<City>,
    selectedProvince: Province?,
    lightedCityCodes: Set<String>,
    selectedCityCodes: Set<String>,
    onCityCheckChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = if (selectedProvince != null) "${selectedProvince.name} - 城市" else "请选择省份",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
        Divider()

        if (selectedProvince == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("请先选择省份", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (cities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无城市数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(cities, key = { it.adcode }) { city ->
                    CityItem(
                        city = city,
                        isLighted = lightedCityCodes.contains(city.adcode),
                        isTempSelected = selectedCityCodes.contains(city.adcode),
                        onCheckChange = { isChecked ->
                            onCityCheckChange(city.adcode, isChecked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CityItem(
    city: City,
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
            text = city.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLighted || isTempSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Checkbox(
            checked = isLighted || isTempSelected,
            onCheckedChange = onCheckChange
        )
    }
}