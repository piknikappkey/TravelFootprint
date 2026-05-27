package com.example.travel_footprint_android.presentation.components.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 城市选择器组件
 *
 * 左右分栏布局：
 * - 左侧：省份列表（可滚动）
 * - 右侧：城市列表（可滚动）
 *
 * @param modifier 修饰符
 * @param selectedCities 已选中的城市代码集合
 * @param provinces 省份列表
 * @param citiesByProvince 按省份分组的城市列表
 * @param onCityToggle 城市选中/取消选中回调
 */
@Composable
fun CitySelector(
    modifier: Modifier = Modifier,
    selectedCities: Set<String>,
    provinces: List<ProvinceInfo>,
    citiesByProvince: Map<String, List<CityItemInfo>>,
    onCityToggle: (String) -> Unit
) {
    // 当前选中的省份
    var selectedProvince by remember { mutableStateOf(provinces.firstOrNull()?.adcode ?: "") }

    // 获取当前省份的城市列表
    val cities = citiesByProvince[selectedProvince] ?: emptyList()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // 左侧省份列表
            ProvinceList(
                provinces = provinces,
                selectedProvince = selectedProvince,
                onProvinceSelect = { selectedProvince = it },
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )

            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // 右侧城市列表
            CityList(
                cities = cities,
                selectedCities = selectedCities,
                onCityToggle = onCityToggle,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * 省份列表组件
 *
 * @param provinces 省份列表
 * @param selectedProvince 当前选中的省份代码
 * @param onProvinceSelect 省份选择回调
 * @param modifier 修饰符
 */
@Composable
private fun ProvinceList(
    provinces: List<ProvinceInfo>,
    selectedProvince: String,
    onProvinceSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(provinces,
                key = { province -> province.adcode }
            )
            { province ->
                ProvinceItem(
                    province = province,
                    isSelected = province.adcode == selectedProvince,
                    onClick = { onProvinceSelect(province.adcode) }
                )
            }
        }
    }
}

/**
 * 省份项组件
 *
 * @param province 省份信息
 * @param isSelected 是否选中
 * @param onClick 点击回调
 */
@Composable
private fun ProvinceItem(
    province: ProvinceInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = province.name,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1
        )
    }
}

/**
 * 城市列表组件
 *
 * @param cities 城市列表
 * @param selectedCities 已选中的城市代码集合
 * @param onCityToggle 城市选中/取消选中回调
 * @param modifier 修饰符
 */
@Composable
private fun CityList(
    cities: List<CityItemInfo>,
    selectedCities: Set<String>,
    onCityToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(cities) { city ->
            CityItem(
                city = city,
                isSelected = selectedCities.contains(city.adcode),
                onToggle = { onCityToggle(city.adcode) }
            )
        }
    }
}

/**
 * 城市项组件
 *
 * @param city 城市信息
 * @param isSelected 是否选中
 * @param onToggle 切换回调
 */
@Composable
private fun CityItem(
    city: CityItemInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = city.name,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选中",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}

// ========== 数据模型 ==========

/**
 * 省份信息
 *
 * @param adcode 行政区划代码
 * @param name 省份名称
 */
data class ProvinceInfo(
    val adcode: String,
    val name: String
)

/**
 * 城市项信息
 *
 * @param adcode 行政区划代码
 * @param name 城市名称
 * @param parentAdcode 父级（省份）行政区划代码
 */
data class CityItemInfo(
    val adcode: String,
    val name: String,
    val parentAdcode: String
)
