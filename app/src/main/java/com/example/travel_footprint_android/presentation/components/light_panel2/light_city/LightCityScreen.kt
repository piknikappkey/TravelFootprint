// LightCityScreen.kt
package com.example.travel_footprint_android.presentation.components.light_panel2.light_city

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation.components.light_panel2.LightPanel2State
import com.example.travel_footprint_android.presentation.screen.nav_screen.LightenCityMode
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
                EmptyStateHint(text = "暂无已点亮城市")
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
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    MoreChip(count = lightCityList.size - 10)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== 省份模式 ====================
            if (lightedProvinces.isEmpty()) {
                EmptyStateHint(text = "暂无已点亮省份")
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
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    MoreChip(count = lightedProvinces.size - 10)
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
fun EmptyStateHint(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}

@Composable
fun MoreChip(count: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF3F4F6))
    ) {
        Text(
            text = "+$count",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
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
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEFF6FF))  // 浅蓝色背景，区分城市
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3B82F6)
            )

            if (isDeleteMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消点亮",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDelete() },
                    tint = Color(0xFFEF4444)
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
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEFF6FF))  // 浅蓝色背景，区分城市
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3B82F6)  // 品牌蓝色文字
            )

            if (isDeleteMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消点亮",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDelete() },
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}