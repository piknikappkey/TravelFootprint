// LightCityScreen.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
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
    // 编辑模式下不显示内容
    if (lightPanel2State == LightPanel2State.EDIT) return

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (lightenCityMode.isCityMode) {
            // ==================== 城市模式 ====================
            if (lightCityList.isEmpty()) {
                Text(
                    text = "暂无已点亮城市",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)
                )
            } else {
                Crossfade(
                    targetState = lightPanel2State,
                    animationSpec = tween(200)
                ) { state ->
                    if (state == LightPanel2State.ALL_DISPLAY) {
                        // 展开模式：使用流式布局，自动换行
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            lightCityList.forEach { city ->
                                LightedCityChip(
                                    name = city.cityName,
                                    isDeleteMode = isDeleteMode,
                                    onDelete = { onDeleteCity(city.cityAdcode) }
                                )
                            }
                        }
                    } else {
                        // 收缩模式：单行横向滚动
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lightCityList.take(10), key = { it.cityAdcode }) { city ->
                                LightedCityChip(
                                    name = city.cityName,
                                    isDeleteMode = isDeleteMode,
                                    onDelete = { onDeleteCity(city.cityAdcode) }
                                )
                            }
                            // 如果超过10个，显示更多提示
                            if (lightCityList.size > 10) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Text(
                                            text = "+${lightCityList.size - 10}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== 省份模式 ====================
            if (lightedProvinces.isEmpty()) {
                Text(
                    text = "暂无已点亮省份",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)
                )
            } else {
                // 使用 AnimatedContent 实现平滑切换
                AnimatedContent(
                    targetState = lightPanel2State,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "provinceContent"
                ) { state ->
                    if (state == LightPanel2State.ALL_DISPLAY) {
                        // 展开模式：使用流式布局，自动换行
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            lightedProvinces.forEach { province ->
                                LightedProvinceChip(
                                    name = province.provinceName,
                                    isDeleteMode = isDeleteMode,
                                    onDelete = { onDeleteProvince(province.provinceAdcode) }
                                )
                            }
                        }
                    } else {
                        // 收缩模式：单行横向滚动
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lightedProvinces.take(10), key = { it.provinceAdcode }) { province ->
                                LightedProvinceChip(
                                    name = province.provinceName,
                                    isDeleteMode = isDeleteMode,
                                    onDelete = { onDeleteProvince(province.provinceAdcode) }
                                )
                            }
                            // 如果超过10个，显示更多提示
                            if (lightedProvinces.size > 10) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Text(
                                            text = "+${lightedProvinces.size - 10}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
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
            //在这里可以自定义点亮城市样式
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