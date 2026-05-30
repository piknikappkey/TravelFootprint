package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list.footprint_list_panel

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_details.LocationRecorder
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.FontDark5
import com.example.travel_footprint_android.ui.theme.MainColor2
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun FootprintListPanel(
    footprint: Footprint,
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"),
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(key = "JourneyMap3"),
) {
    // 旅程数据
    val journeyUiState by journeyViewModel.uiState.collectAsState()

    // 地址数据
    val locationList = journeyUiState.LocationList

    LaunchedEffect(locationList) {
        if(locationList.size == 0) {
            Log.d("FootprintListPanel", "locationList = ${locationList}")
        } else {
            Log.d("FootprintListPanel", "locationList = ${locationList.first()}, size = ${locationList.size}")
        }
    }

    LaunchedEffect(footprint) {
        Log.d("FootprintListPanel", "footprint = ${footprint}")
    }

    // 路线索引（用于标记Location属于哪段路线，最小值为1）
    var locationIndex by remember { mutableStateOf(1) }

    var panelState by remember { mutableStateOf(FootprintListPanelState.STOP) }
    var panelStateOld by remember { mutableStateOf(FootprintListPanelState.STOP) }

    var startTime by remember { mutableStateOf<Long>(0) }

    var durationTime by remember { mutableStateOf<Long>(footprint.duration) }

    var displacementDistance by remember { mutableStateOf(footprint.distance) }

    var speed by remember { mutableStateOf(footprint.speed) }

    var calories by remember { mutableStateOf(footprint.calories) }

    var isRecord by remember { mutableStateOf(false) }

    var lastLatitude by remember { mutableStateOf<Double?>(null) }
    var lastLongitude by remember { mutableStateOf<Double?>(null) }

    var pausedDuration by remember { mutableStateOf<Long>(0) }
    var pauseStartTime by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(panelState) {
        when(panelState) {
            FootprintListPanelState.START -> {
                if (startTime == 0L) {
                    startTime = System.currentTimeMillis()
                }
                pauseStartTime?.let { pauseStart ->
                    pausedDuration += System.currentTimeMillis() - pauseStart
                    pauseStartTime = null
                }
                isRecord = true

                // 通过panelStateOld判断该路线是否已经结束，结束则locationIndex增加，否则继续用旧的locationIndex
                if(locationList.size == 0) { // 此时该足迹还没有任何路线
                    locationIndex = 1
                }else if(panelStateOld == FootprintListPanelState.STOP) {
                    // 该段路线已经结束，locationIndex增加
                    locationIndex = locationList.last().index + 1
                } else { // 该段路线未结束，继续使用locationList中最后一个定位的index
                    locationIndex = locationList.last().index
                }

                panelStateOld = FootprintListPanelState.START

                while (panelState == FootprintListPanelState.START) {
                    val currentTime = System.currentTimeMillis()
                    durationTime = currentTime - startTime - pausedDuration + footprint.duration
                    if (durationTime > 0) {
                        speed = (displacementDistance / (durationTime / 1000.0))
                    }
                    // 保存数据
                    if(footprint.startTime.time == 0L) {
                        journeyViewModel.updateFootprint(
                            footprint.copy(
                                startTime = Date(startTime),
                                duration = durationTime,
                                distance = displacementDistance,
                                speed = speed,
                                calories = calories
                            )
                        )
                    } else {
                        journeyViewModel.updateFootprint(
                            footprint.copy(
                                duration = durationTime,
                                distance = displacementDistance,
                                speed = speed,
                                calories = calories
                            )
                        )
                    }


                    delay(1000)
                }
            }
            FootprintListPanelState.PAUSE -> {
                pauseStartTime = System.currentTimeMillis()
                isRecord = false

                panelStateOld = FootprintListPanelState.PAUSE
            }
            FootprintListPanelState.STOP -> {
                isRecord = false

                panelStateOld = FootprintListPanelState.STOP
            }
        }
    }

    LaunchedEffect(Unit) {
        journeyViewModel.getLocation(footprint)
        Log.d("FootprintListPanel", "journeyViewModel.getLocation(footprint)")
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("FootprintListPanel", "onDispose")
            isRecord = false
            journeyMap3ViewModel.clearAllRoutes()
            journeyViewModel.getLocation(footprint.copy(id = 0))
        }
    }

    LocationRecorder(isRecord = isRecord) { latitude, longitude ->
        lastLatitude?.let { prevLat ->
            lastLongitude?.let { prevLon ->
                val distance = calculateDistance(prevLat, prevLon, latitude, longitude)
                if (distance < 100) {
                    displacementDistance += distance
                    calories = displacementDistance * 60
                    // 存储位置
                    journeyViewModel.addLocation(
                        Location(
                            footprintId = footprint.id,
                            latitude = latitude,
                            longitude = longitude,
                            index = locationIndex,
                        )
                    )
                }
            }
        }
        lastLatitude = latitude
        lastLongitude = longitude
    }

    BGBox(
        modifier = Modifier
            .padding(horizontal = 15.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            // 标题栏
            Row {
                TextMedium(
                    text = "记录足迹",
                    fontSize = 18.sp,
                    color = FontDark4
                )

                Spacer(Modifier.weight(1f))

                if (panelState == FootprintListPanelState.START) {
                    TextMedium(
                        text = "正在记录中...",
                        color = MainColor2
                    )
                } else if (panelState == FootprintListPanelState.PAUSE) {
                    TextMedium(
                        text = "已暂停",
                        color = MainColor2
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DataRow2(
                "开始时间：",
                if (footprint.startTime.time > 0) formatDateTime(footprint.startTime.time) else "未开始",
                "持续时间：",
                formatDuration(durationTime)
            )

            DataRow2(
                "移动距离：",
                formatDistance(displacementDistance),
                "移动速度：",
                String.format(Locale.CHINA, "%.2f m/s", speed)
            )

            DataRow2(
                "消耗卡路里",
                formatCalories(calories),
                null,
                null
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ButtonMain(
                    onClick = {
                        if(panelState == FootprintListPanelState.STOP || panelState == FootprintListPanelState.PAUSE) {
                            panelState = FootprintListPanelState.START
                        } else {
                            panelState = FootprintListPanelState.PAUSE
                        }
                    }
                ) {
                    TextMedium(
                        text = if(panelState == FootprintListPanelState.STOP || panelState == FootprintListPanelState.PAUSE) "开始" else "暂停",
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                if(panelState != FootprintListPanelState.STOP) {
                    ButtonMain(
                        onClick = {
                            panelState = FootprintListPanelState.STOP
                            startTime = 0
                            durationTime = footprint.duration
                            displacementDistance = footprint.distance
                            speed = footprint.speed
                            calories = footprint.calories
                            pausedDuration = footprint.duration
                            pauseStartTime = null
                            lastLatitude = null
                            lastLongitude = null
                        }
                    ) {
                        TextMedium(
                            text = "结束",
                            fontSize = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun DataRow2(
    label: String,
    value: String,
    label2: String?,
    value2: String?
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp)
    ) {
        Row{
            TextSmall(
                text = label,
                fontSize = 15.sp,
                color = FontDark5
            )
            Spacer(Modifier.width(0.dp))
            TextSmall(
                text = value,
                fontSize = 15.sp,
                color = FontDark5
            )
        }
        if(label2 != null && value2 != null) {
            Row {
                Spacer(Modifier.weight(1f))

                TextSmall(
                    text = label2,
                    fontSize = 15.sp,
                    color = FontDark5
                )
                Spacer(Modifier.width(0.dp))
                TextSmall(
                    text = value2,
                    fontSize = 15.sp,
                    color = FontDark5
                )
            }
        }
    }
}

private fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371000.0

    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val deltaLat = Math.toRadians(lat2 - lat1)
    val deltaLon = Math.toRadians(lon2 - lon1)

    val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

private fun formatDateTime(timestamp: Long): String {
    val dateTime = Date(timestamp)
    val now = Date()

    val dateTimeSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    val timeOnlySdf = SimpleDateFormat("HH:mm", Locale.CHINA)

    val dateTimeStr = dateTimeSdf.format(dateTime)
    val timeOnlyStr = timeOnlySdf.format(dateTime)

    val dateStr = dateTimeStr.substringBefore(" ")
    val nowDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(now)

    return if (dateStr == nowDateStr) {
        timeOnlyStr
    } else {
        dateTimeStr
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, secs)
        else -> String.format(Locale.CHINA, "%02d:%02d", minutes, secs)
    }
}

private fun formatDistance(distance: Double): String {
    return if (distance >= 1000) {
        String.format(Locale.CHINA, "%.2f km", distance / 1000)
    } else {
        String.format(Locale.CHINA, "%.2f m", distance)
    }
}

private fun formatCalories(calories: Double): String {
    return if (calories >= 1000) {
        String.format(Locale.CHINA, "%.2f 千卡", calories / 1000)
    } else {
        String.format(Locale.CHINA, "%.2f 卡", calories)
    }
}

enum class FootprintListPanelState {
    START,
    PAUSE,
    STOP,
}
