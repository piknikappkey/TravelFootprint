package com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.light_city_edit_set_time

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightCityEditSetTime(
    selectedCities: String,
    lightedTime: LocalDate,
    onSaveClick: (LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val datePickerState = rememberDatePickerState()
        SetDateDialog(
            { showDialog = it },
            datePickerState,
            onSaveClick
        )
    }

    // 触发对话框的按钮和结果显示
    TextButton(onClick = { showDialog = true }) {
        Text("选择日期")
    }
    Text(selectedCities + "点亮时间: $lightedTime")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDateDialog(
    setShowDialog: (Boolean) -> Unit,
    datePickerState: DatePickerState,
    setSelectedDate: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        onDismissRequest = { setShowDialog(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        setSelectedDate(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        )
                    }
                    setShowDialog(false)
                }
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = { setShowDialog(false) }) { Text("取消") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}