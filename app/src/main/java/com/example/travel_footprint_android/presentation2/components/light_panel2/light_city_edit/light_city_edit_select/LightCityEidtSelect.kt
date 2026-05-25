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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun LightCityEidtSelect(
    selectedCityCodes: Set<String> = emptySet(),
    selectedProvinceCodes: Set<String> = emptySet(),
    onCitySelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    onProvinceSelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    lightenCityMode: LightenCityMode,
    lightenViewModel: LightenViewModel   = hiltViewModel()
) {
    //获取UI状态
    val uiState by lightenViewModel.uiState.collectAsState()

    //获取省份列表
    val allProvinces by lightenViewModel.allProvinces.collectAsStateWithLifecycle(initialValue = emptyList())

    //选中的省份
    var selectedProvince by remember { mutableStateOf<Province?>(null) }

    //获取选中省份下的所有城市
    val citiesByProvince by lightenViewModel.getCitiesByProvince(selectedProvince?.adcode ?: "")
        .collectAsStateWithLifecycle(initialValue = emptyList())
    //
    //获取ui点亮省份数据
    val lightedProvinces = uiState.lightedProvinces
    //获取ui点亮城市数据
    val lightedCity = uiState.lightedCities


    Row(
        modifier = Modifier
            .fillMaxSize()
            .height(400.dp)
    ) {
        ProvinceList(
            provincesList = allProvinces,
            selectedProvince = selectedProvince,
            selectedProvinceCodes = selectedProvinceCodes,
            onProvinceChoose = { province ->
                selectedProvince = province
            },
            modifier = Modifier.weight(1f)
        )

        //---------------------------------------------------

        CityList(
            cities = citiesByProvince,
            selectedProvince = selectedProvince,
            lightedCity = lightedCity,
            selectedCityCodes = selectedCityCodes,
            onCityCheckChange = onCitySelectionChange,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun ProvinceList(
    provincesList: List<Province>,
    selectedProvince: Province?,
    selectedProvinceCodes: Set<String>,
    onProvinceChoose: (Province) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("省份", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(provincesList, key = { it.adcode }) { province ->
                ProvinceItem(
                    province = province,
                    isSelected = selectedProvince?.adcode == province.adcode,
                    isTempSelected = selectedProvinceCodes.contains(province.adcode),
                    onProvinceChoose = { onProvinceChoose(province) },
                )
            }
        }
    }
}

@Composable
fun ProvinceItem(
    province: Province,
    isSelected: Boolean,
    isTempSelected: Boolean,
    onProvinceChoose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProvinceChoose() }
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = province.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isTempSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

    }
}

//城市列表

@Composable
fun CityList(
    cities: List<City>,
    selectedProvince: Province?,
    lightedCity: List<LightedCity>,
    selectedCityCodes: Set<String>,
    onCityCheckChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text ="请选择城市",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(12.dp)
        )
//-------------------------
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
                        //判断当前城市是否被点亮
                        isLighted = selectedCityCodes.any { cityCode ->
                            lightedCity.any { it.cityAdcode == cityCode }
                        },
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