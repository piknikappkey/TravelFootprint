// app/src/main/java/com/example/travel_footprint_android/presentation2/components/light_panel2/light_city_edit/light_city_edit_select/LightCityEidtSelect.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_select

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import kotlinx.coroutines.delay

@Composable
fun LightCityEidtSelect(
    selectedCityCodes: Set<String> = emptySet(),
    selectedProvinceCodes: Set<String> = emptySet(),
    onCitySelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    onProvinceSelectionChange: (String, Boolean) -> Unit = { _, _ -> },
    lightenCityMode: LightenCityMode,
    lightenViewModel: LightenViewModel = hiltViewModel()
) {
    val uiState by lightenViewModel.uiState.collectAsState()
    val allProvinces by lightenViewModel.allProvinces.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProvince by remember { mutableStateOf<Province?>(null) }
    val citiesByProvince by lightenViewModel.getCitiesByProvince(selectedProvince?.adcode ?: "")
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val lightedProvinces = uiState.lightedProvinces
    val lightedCity = uiState.lightedCities

    Card(
        modifier = Modifier
            .fillMaxSize()
            .height(480.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
        // 标题栏
        Text(
            text = "省份",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 统计信息
        Text(
            text = "共 ${provincesList.size} 个省份",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 分割线
        Divider(
            color = Color(0xFFE5E7EB),
            thickness = 0.5.dp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
    val backgroundColor = when {
        isSelected -> Color(0xFFEFF6FF)  // 浅蓝色
        isTempSelected -> Color.White// 浅绿色
        else -> Color.White
    }

    val textColor = when {
        isSelected || isTempSelected -> Color(0xFF3B82F6)  // 品牌蓝
        else -> Color(0xFF9CA3AF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProvinceChoose() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = province.name,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isTempSelected) FontWeight.Medium else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

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
        // 标题栏
        Text(
            text = "城市",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 动态提示信息
        when {
            selectedProvince == null -> {
                Text(
                    text = "请先选择省份",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            cities.isEmpty() -> {
                Text(
                    text = "暂无城市数据",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            else -> {
                Text(
                    text = "共 ${cities.size} 个城市",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // 分割线
        Divider(
            color = Color(0xFFE5E7EB),
            thickness = 0.5.dp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 城市列表
        if (selectedProvince == null || cities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = "empty",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (selectedProvince == null) "点击左侧选择省份" else "该省份暂无城市",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cities, key = { city -> city.adcode }) { city ->
                    CityItem(
                        city = city,
                        isLighted = lightedCity.any { it.cityAdcode == city.adcode },
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
    val isSelected = isLighted || isTempSelected

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF8FAFC) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = city.name,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF374151)
                )
                if (isLighted) {
                    Text(
                        text = "已点亮",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF3B82F6),
                    uncheckedColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}