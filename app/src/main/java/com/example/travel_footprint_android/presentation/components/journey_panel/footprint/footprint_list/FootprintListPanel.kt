package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_list

/*
 * 【文件用途】
 * FootprintListPanel 是"足迹运动记录面板"组件，为一个指定的足迹(Footprint)提供
 * 运动数据(持续时间、移动距离、移动速度、消耗卡路里)的记录、实时计算与展示功能。
 *
 * 【核心功能】
 * 1. 状态机控制：通过枚举 FootprintListPanelState (START / PAUSE / STOP) 管理记录状态
 * 2. 实时计时：在 START 状态下每秒更新持续时间(durationTime)
 * 3. GPS 距离追踪：通过 LocationRecorder 组件获取实时经纬度，使用 Haversine 公式
 *    计算相邻定位点之间的距离，累加得到总位移距离(displacementDistance)
 * 4. 卡路里估算：根据 totalDistance × 60 的线性公式估算消耗卡路里
 * 5. 路线分段管理：通过 locationIndex 将 GPS 坐标点按路线段分组，支持多次启停
 * 6. 数据持久化：每秒钟将最新的运动数据写入 Room 数据库(通过 JourneyViewModel.updateFootprint)
 * 7. 暂停累计：支持多次暂停/继续，暂停期间的时间不计入持续时间
 *
 * 【关联组件】
 * - Footprint (data.entity):      足迹实体，包含 duration/distance/speed/calories 等运动字段
 * - Location (data.entity):       位置实体，包含经纬度与路线索引(index)，通过外键关联 Footprint
 * - JourneyViewModel:             管理足迹与位置数据的增删改查(updateFootprint / addLocation / getLocation)
 * - JourneyMap3ViewModel:         管理地图路线绘制(clearAllRoutes)
 * - LocationRecorder:             高德定位组件，持续回调实时经纬度(latitude, longitude)
 * - BGBox:                        通用背景容器，提供圆角阴影背景
 * - ButtonMain:                   通用按钮组件(开始/暂停/结束)
 * - TextMedium / TextSmall:       通用文字组件，使用自定义字体
 * - FootprintListPanelState:      内部枚举，定义 START(记录中) / PAUSE(已暂停) / STOP(已停止)
 *
 * 【简单实现逻辑】
 * 1. 组件挂载时调用 journeyViewModel.getLocation(footprint) 加载该足迹的历史位置数据
 * 2. LocationRecorder 在 isRecord=true 时持续回调 GPS 坐标，与上一个坐标计算距离
 *    (过滤 >100m 的跳变，视为 GPS 噪声)，累计到 displacementDistance
 * 3. 用户点击"开始"→ panelState=START，LaunchedEffect 启动每秒循环：
 *    - 更新 durationTime = 当前时间 - startTime - 累计暂停时间 + 历史持续时间
 *    - 计算 speed = displacementDistance / (durationTime / 1000)
 *    - 调用 journeyViewModel.updateFootprint() 持久化
 * 4. 用户点击"暂停"→ panelState=PAUSE，记录 pauseStartTime，停止 GPS 和计时
 * 5. 用户点击"结束"→ panelState=STOP，所有变量重置为 footprint 初始值
 * 6. 组件销毁(onDispose)时停止记录、清除地图路线、释放定位资源
 */

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
import com.example.travel_footprint_android.presentation.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_details.LocationRecorder
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
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

// 主面板组件：展示某个足迹的运动记录面板，支持开始/暂停/结束控制
@Composable
fun FootprintListPanel(
    footprint: Footprint,                               // 目标足迹对象，携带初始运动数据(duration/distance等)
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"),           // 旅程数据ViewModel（共享同一实例）
    journeyMapViewModel: JourneyMapViewModel = hiltViewModel(key = "JourneyMap3"), // 地图ViewModel（用于清除路线）
) {
    // 从 JourneyViewModel 的 UI 状态中获取位置数据列表
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val locationList = journeyUiState.LocationList

    // 调试日志：打印 locationList 的加载情况
    LaunchedEffect(locationList) {
        if(locationList.size == 0) {
            Log.d("FootprintListPanel", "locationList = ${locationList}")
        } else {
            Log.d("FootprintListPanel", "locationList = ${locationList.first()}, size = ${locationList.size}")
        }
    }

    // 调试日志：打印 footprint 数据
    LaunchedEffect(footprint) {
        Log.d("FootprintListPanel", "footprint = ${footprint}")
    }

    // 路线索引（用于标记Location属于哪段路线，最小值为1，每次开始记录时递增）
    var locationIndex by remember { mutableStateOf(1) }

    // 面板状态机：START=记录中 / PAUSE=已暂停 / STOP=已停止
    var panelState by remember { mutableStateOf(FootprintListPanelState.STOP) }
    // 上一状态快照，用于判断路线是否已结束，决定 locationIndex 是否递增
    var panelStateOld by remember { mutableStateOf(FootprintListPanelState.STOP) }

    // 本次记录的开始时间戳（首次START时记录，用于计算持续时间）
    var startTime by remember { mutableStateOf<Long>(0) }

    // 当前持续时间 = 历史持续时间 + 本次已记录时间（毫秒）
    var durationTime by remember { mutableStateOf<Long>(footprint.duration) }

    // 当前累计位移距离（米）
    var displacementDistance by remember { mutableStateOf(footprint.distance) }

    // 当前移动速度（米/秒）
    var speed by remember { mutableStateOf(footprint.speed) }

    // 当前消耗卡路里
    var calories by remember { mutableStateOf(footprint.calories) }

    // 是否正在记录GPS定位（控制 LocationRecorder 的启停）
    var isRecord by remember { mutableStateOf(false) }

    // 上一个有效的定位点（用于与当前点计算距离差值）
    var lastLatitude by remember { mutableStateOf<Double?>(null) }
    var lastLongitude by remember { mutableStateOf<Double?>(null) }

    // 累计暂停时长（毫秒），用于从持续时间中扣除暂停时间
    var pausedDuration by remember { mutableStateOf<Long>(0) }
    // 本次暂停开始的时间戳，用于计算当前暂停段持续了多久
    var pauseStartTime by remember { mutableStateOf<Long?>(null) }

    // 监听 panelState 变化，执行状态对应的逻辑
    LaunchedEffect(panelState) {
        when(panelState) {
            FootprintListPanelState.START -> {
                // 首次进入START时记录开始时间
                if (startTime == 0L) {
                    startTime = System.currentTimeMillis()
                }
                // 如果是从暂停恢复，将暂停时长累加到 pausedDuration
                pauseStartTime?.let { pauseStart ->
                    pausedDuration += System.currentTimeMillis() - pauseStart
                    pauseStartTime = null
                }
                isRecord = true

                // 计算当前路线段的 locationIndex
                if(locationList.size == 0) { // 足迹还没有任何历史路线
                    locationIndex = 1
                }else if(panelStateOld == FootprintListPanelState.STOP) {
                    // 上一个状态是STOP，说明开始了一段新路线，index递增
                    locationIndex = locationList.last().index + 1
                } else {
                    // 上一个状态是PAUSE或START，继续使用最后一段路线的index
                    locationIndex = locationList.last().index
                }

                panelStateOld = FootprintListPanelState.START

                // 每秒循环：更新持续时间、计算速度、持久化到数据库
                while (panelState == FootprintListPanelState.START) {
                    val currentTime = System.currentTimeMillis()
                    // 持续时间 = 当前时间 - 开始时间 - 暂停累计 + 足迹初始持续时间
                    durationTime = currentTime - startTime - pausedDuration + footprint.duration
                    if (durationTime > 0) {
                        speed = (displacementDistance / (durationTime / 1000.0))
                    }
                    // 将实时运动数据写入数据库
                    if(footprint.startTime.time == 0L) {
                        // 如果足迹还没有开始时间，同时保存 startTime
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

                    delay(1000) // 每秒更新一次
                }
            }
            FootprintListPanelState.PAUSE -> {
                // 进入暂停状态：记录暂停开始时间，停止GPS定位
                pauseStartTime = System.currentTimeMillis()
                isRecord = false

                panelStateOld = FootprintListPanelState.PAUSE
            }
            FootprintListPanelState.STOP -> {
                // 结束记录：停止GPS定位
                isRecord = false

                panelStateOld = FootprintListPanelState.STOP
            }
        }
    }

    // 组件挂载时加载该足迹的历史位置数据（用于在地图上绘制路线）
    LaunchedEffect(Unit) {
        journeyViewModel.getLocation(footprint)
        Log.d("FootprintListPanel", "journeyViewModel.getLocation(footprint)")
    }

    // 组件销毁时的清理工作
    DisposableEffect(Unit) {
        onDispose {
            Log.d("FootprintListPanel", "onDispose")
            isRecord = false
            journeyMapViewModel.clearAllRoutes()         // 清除地图上的路线
            journeyViewModel.getLocation(footprint.copy(id = 0)) // 清空位置列表（id=0表示查询空数据）
        }
    }

    // GPS定位记录器：当 isRecord=true 时持续回调经纬度
    LocationRecorder(isRecord = isRecord) { latitude, longitude ->
        // 如果有上一个定位点，计算两点间距离
        lastLatitude?.let { prevLat ->
            lastLongitude?.let { prevLon ->
                val distance = calculateDistance(prevLat, prevLon, latitude, longitude)
                // 过滤 >100m 的跳变（GPS噪声），保留有效移动距离
                if (distance < 100) {
                    displacementDistance += distance
                    calories = displacementDistance * 60    // 线性卡路里估算
                    // 将当前定位点存入数据库
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
        // 更新上一个定位点为当前点
        lastLatitude = latitude
        lastLongitude = longitude
    }

    // 面板UI：用 BGBox 包裹内容，显示运动数据和操作按钮
    BGBox(
        modifier = Modifier
            .padding(horizontal = 15.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            // 标题栏：左侧"记录足迹"标题 + 右侧状态提示
            Row {
                TextMedium(
                    text = "记录足迹",
                    fontSize = 18.sp,
                    color = FontDark4
                )

                Spacer(Modifier.weight(1f))

                // 根据状态显示"正在记录中..."或"已暂停"
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

            // 数据行1：开始时间 + 持续时间
            DataRow2(
                "开始时间：",
                if (footprint.startTime.time > 0) formatDateTime(footprint.startTime.time) else "未开始",
                "持续时间：",
                formatDuration(durationTime)
            )

            // 数据行2：移动距离 + 移动速度
            DataRow2(
                "移动距离：",
                formatDistance(displacementDistance),
                "移动速度：",
                String.format(Locale.CHINA, "%.2f m/s", speed)
            )

            // 数据行3：消耗卡路里（单行显示）
            DataRow2(
                "消耗卡路里",
                formatCalories(calories),
                null,
                null
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 底部按钮行：开始/暂停切换 + 结束按钮
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 开始/暂停按钮：STOP或PAUSE时显示"开始"，START时显示"暂停"
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

                // 结束按钮：仅在非STOP状态时显示，点击后数据重置为足迹初始值
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

// 数据行组件：显示一组或两组标签-值对（用于展示运动统计数据）
@Composable
private fun DataRow2(
    label: String,      // 左侧标签文字
    value: String,      // 左侧值文字
    label2: String?,    // 右侧标签文字（可为null，隐藏右侧行）
    value2: String?     // 右侧值文字（可为null，隐藏右侧行）
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
        // 如果 label2 和 value2 都不为null，显示右侧数据
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

// 使用 Haversine 公式计算两个经纬度点之间的距离（单位：米）
private fun calculateDistance(
    lat1: Double,   // 起点纬度
    lon1: Double,   // 起点经度
    lat2: Double,   // 终点纬度
    lon2: Double    // 终点经度
): Double {
    val earthRadius = 6371000.0 // 地球平均半径（米）

    // 将经纬度转换为弧度
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val deltaLat = Math.toRadians(lat2 - lat1)
    val deltaLon = Math.toRadians(lon2 - lon1)

    // Haversine 公式核心计算
    val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c // 返回米单位的距离
}

// 格式化时间戳为可读时间：当天显示"HH:mm"，非当天显示"yyyy-MM-dd HH:mm"
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
        timeOnlyStr    // 当天只显示时间
    } else {
        dateTimeStr    // 非当天显示完整日期+时间
    }
}

// 格式化持续时间为"HH:mm:ss"或"mm:ss"格式
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

// 格式化距离：>=1000米显示"xx.xx km"，否则显示"xx.xx m"
private fun formatDistance(distance: Double): String {
    return if (distance >= 1000) {
        String.format(Locale.CHINA, "%.2f km", distance / 1000)
    } else {
        String.format(Locale.CHINA, "%.2f m", distance)
    }
}

// 格式化卡路里：>=1000显示"xx.xx 千卡"，否则显示"xx.xx 卡"
private fun formatCalories(calories: Double): String {
    return if (calories >= 1000) {
        String.format(Locale.CHINA, "%.2f 千卡", calories / 1000)
    } else {
        String.format(Locale.CHINA, "%.2f 卡", calories)
    }
}

// 面板状态枚举：控制记录流程的三种状态
enum class FootprintListPanelState {
    START,  // 记录中（计时+GPS定位）
    PAUSE,  // 已暂停（停止计时+GPS定位，保留数据）
    STOP,   // 已停止（结束记录，数据重置）
}
