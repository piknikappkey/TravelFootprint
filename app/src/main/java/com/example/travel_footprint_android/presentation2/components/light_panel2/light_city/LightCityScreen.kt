// LightCityScreen.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode

@Composable
fun LightCityScreen(
    lightPanel2State: LightPanel2State,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightenCityMode: LightenCityMode,
    isDeleteMode: Boolean = false,
    onDeleteProvince: (String) -> Unit = {},
    onDeleteCity: (String) -> Unit = {}
) {
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
        if (lightPanel2State == LightPanel2State.EDIT) return@Column

        if (lightenCityMode == LightenCityMode.CITY) {
            // 城市模式：显示已点亮城市
            if (lightCityList.isEmpty()) {
                Text(
                    text = "暂无已点亮城市",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lightCityList, key = { it.cityAdcode }) { city ->
                        LightedCityChip(
                            name = city.cityName,
                            isDeleteMode = isDeleteMode,
                            onDelete = { onDeleteCity(city.cityAdcode) }
                        )
                    }
                }
            }
        } else {
            // 省份模式：显示已点亮省份
            if (lightedProvinces.isEmpty()) {
                Text(
                    text = "暂无已点亮省份",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lightedProvinces, key = { it.provinceAdcode }) { province ->
                        LightedProvinceChip(
                            name = province.provinceName,
                            isDeleteMode = isDeleteMode,
                            onDelete = { onDeleteProvince(province.provinceAdcode) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LightedProvinceChip(
    name: String,
    isDeleteMode: Boolean,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (isDeleteMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消点亮",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun LightedCityChip(
    name: String,
    isDeleteMode: Boolean,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (isDeleteMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消点亮",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() },
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}