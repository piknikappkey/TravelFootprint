// app/src/main/java/com/example/travel_footprint_android/presentation2/components/light_panel2/light_city_edit/light_province_edit_select/LightProvinceEditSelect.kt
package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_province_edit_select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Card(
        modifier = Modifier
            .fillMaxSize()
            .height(480.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题栏
            Text(
                text = "选择省份",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 统计信息
            Text(
                text = "共 ${unlightedProvinces.size} 个省份可供选择",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 分割线
            Divider(
                color = Color(0xFFE5E7EB),
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            if (unlightedProvinces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "all lighted",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "所有省份都已点亮\n" +
                                    "快去朋友圈分享一下吧~",
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
}

@Composable
fun ProvinceItemForProvince(
    province: Province,
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
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = province.name,
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