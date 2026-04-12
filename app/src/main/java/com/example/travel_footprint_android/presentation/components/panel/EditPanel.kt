package com.example.travel_footprint_android.presentation.components.panel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.repository.GeoDataRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 编辑面板组件
 *
 * 用于编辑点亮的城市和时间
 *
 * @param selectedCities 已选择的城市代码集合
 * @param lightedTime 点亮时间
 * @param onSaveClick 保存回调
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPanel(
    selectedCities: Set<String>,
    lightedTime: LocalDate,
    onSaveClick: (Set<String>, LocalDate) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val geoDataRepository = remember { GeoDataRepository(context) }

    // 内部编辑状态
    var currentSelectedCities by remember { mutableStateOf(selectedCities) }
    var currentLightedTime by remember { mutableStateOf(lightedTime) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 地理数据
    var provinces by remember { mutableStateOf<List<ProvinceInfo>>(emptyList()) }
    var citiesByProvince by remember { mutableStateOf<Map<String, List<CityItemInfo>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // 加载地理数据
    LaunchedEffect(Unit) {
        val (loadedProvinces, loadedCities) = geoDataRepository.loadAllGeoData()
        provinces = loadedProvinces
        citiesByProvince = loadedCities
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 面板头部
        PanelHeader(
            selectedCount = currentSelectedCities.size,
            onBackClick = onBackClick,
            onSaveClick = {
                onSaveClick(currentSelectedCities, currentLightedTime)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 省份-城市选择器
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            CitySelector(
                modifier = Modifier.fillMaxWidth(),
                selectedCities = currentSelectedCities,
                provinces = provinces,
                citiesByProvince = citiesByProvince,
                onCityToggle = { cityAdcode ->
                    currentSelectedCities = if (currentSelectedCities.contains(cityAdcode)) {
                        currentSelectedCities - cityAdcode
                    } else {
                        currentSelectedCities + cityAdcode
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 日期选择器
        DateSelector(
            selectedDate = currentLightedTime,
            onDateClick = { showDatePicker = true }
        )

        // 日期选择对话框
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { date ->
                    date?.let { currentLightedTime = it }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                initialDate = currentLightedTime
            )
        }
    }
}

/**
 * 面板头部组件
 *
 * @param selectedCount 已选择城市数量
 * @param onBackClick 返回按钮点击回调
 * @param onSaveClick 保存按钮点击回调
 */
@Composable
private fun PanelHeader(
    selectedCount: Int,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 返回按钮
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // 标题
        Text(
            text = "点亮城市 ($selectedCount)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center)
        )

        // 保存按钮
        IconButton(
            onClick = onSaveClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "保存",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 日期选择器组件
 *
 * @param selectedDate 当前选择的日期
 * @param onDateClick 日期点击回调
 */
@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日")

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "点亮时间",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.format(formatter),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDateClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改日期",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 日期选择对话框
 *
 * @param onDateSelected 日期选择回调
 * @param onDismiss 对话框关闭回调
 * @param initialDate 初始日期
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // 不允许选择未来日期
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedDate = datePickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                onDateSelected(selectedDate)
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
