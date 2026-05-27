package com.example.travel_footprint_android.presentation2.components.light_panel2

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.components.light_panel2.checkin.CheckInContent
import com.example.travel_footprint_android.presentation2.components.light_panel2.checkin.CheckInRecord
import com.example.travel_footprint_android.presentation2.components.light_panel2.corner.CornerContent
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city.LightCityScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.light_city_edit.LightCityEditScreen
import com.example.travel_footprint_android.presentation2.components.light_panel2.milestone.MilestoneContent
import com.example.travel_footprint_android.presentation2.components.light_panel2.panel_title.PanelTitle
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LightPanel2(
    modifier: Modifier = Modifier,
    lightenCityMode: LightenCityMode,
    lightenViewModel: LightenViewModel = hiltViewModel(),
    onExpandedChanged: ((Boolean) -> Unit)? = null  // 展开状态变化回调，通知外层切换地图组件

) {

    val uiState by lightenViewModel.uiState.collectAsState()

    val lightCityList = uiState.lightedCities
    val lightedCityCount = uiState.lightedCityCount
    val lightedProvinces = uiState.lightedProvinces
    val lightedProvinceCount = uiState.lightedProvinceCount

    var lightPanel2State by remember {
        mutableStateOf(LightPanel2State.ROUGH_DISPLAY)
    }

    var isDeleteMode by remember {
        mutableStateOf(false)
    }

    // Tab
    var selectedTab by remember {
        mutableStateOf(LightPanel2Tab.LIGHT_UP)
    }

    // 打卡记录
    var checkInRecords by remember {
        mutableStateOf<List<CheckInRecord>>(emptyList())
    }

    // 屏幕高度
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp

    // 面板高度
    val collapsedHeight = 40.dp
    val expandedHeight = screenHeightDp * 0.5f

    // 是否展开
    var isExpanded by remember {
        mutableStateOf(false)
    }

    // 当前面板高度（核心状态）
    var panelHeight by remember {
        mutableStateOf(collapsedHeight)
    }

    val density = LocalDensity.current

    // 编辑模式下的选择状态
    var selectedCityCodes by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var unselectedCityCodes by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var selectedProvinceCodes by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var unselectedProvinceCodes by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    // 内容最大高度
    val scrollableMaxHeight =
        (panelHeight - 60.dp).coerceAtLeast(0.dp)

    // 创建一个带回调的状态更新函数
    fun updateExpandedState(expanded: Boolean) {
        isExpanded = expanded
        onExpandedChanged?.invoke(isExpanded)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(panelHeight)
                .shadow(
                    elevation = 8.dp, shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp
                    ), clip = false
                )
                .background(
                    color = Color.White, shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp
                    )
                )
        ) {

            // ================= 拖拽区域 =================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Color.White, RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp
                        )
                    )
                    .pointerInput(Unit) {

                        detectVerticalDragGestures(

                            onVerticalDrag = { _, dragAmount ->

                                // 向上拖拽 dragAmount 为负数
                                // 所以这里必须取反
                                val dragDp = with(density) {
                                    (-dragAmount).toDp()
                                }

                                panelHeight = (panelHeight + dragDp).coerceIn(
                                    collapsedHeight, expandedHeight
                                )
                            },

                            onDragEnd = {

                                val middleHeight =
                                    collapsedHeight + (expandedHeight - collapsedHeight) / 2

                                isExpanded = panelHeight > middleHeight

                                updateExpandedState(isExpanded) //拖拽结束回调

                                panelHeight = if (isExpanded) {
                                    expandedHeight
                                } else {
                                    collapsedHeight
                                }
                            }

                        )
                    }, contentAlignment = Alignment.Center
            ) {

                // 拖拽指示条
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            Color(0xFFE0E0E0), CircleShape
                        )
                )
            }

            // ================= Tab 标题 =================

            PanelTitle(
                selectedTab = selectedTab, onTabSelected = { tab ->

                    selectedTab = tab

                    // 点击 Tab 自动展开
                    if (!isExpanded) {
                        updateExpandedState(true)

                        isExpanded = true

                        panelHeight = expandedHeight
                    }
                })

            // ================= 内容区域 =================

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                if (panelHeight > collapsedHeight + 10.dp) {

                    when (selectedTab) {

                        LightPanel2Tab.LIGHT_UP -> {

                            LightUpContentOnly(
                                lightPanel2State = lightPanel2State,
                                lightCityList = lightCityList,
                                lightedCityCount = lightedCityCount,
                                lightedProvinces = lightedProvinces,
                                lightedProvinceCount = lightedProvinceCount,
                                lightenCityMode = lightenCityMode,
                                isDeleteMode = isDeleteMode,
                                scrollableMaxHeight = scrollableMaxHeight,

                                onStateChange = {
                                    lightPanel2State = it
                                },

                                onDeleteModeChange = {
                                    isDeleteMode = it
                                },

                                onLightenViewModel = lightenViewModel,

                                onSelectionChanged = { sCities, uCities, sProvinces, uProvinces ->

                                    selectedCityCodes = sCities
                                    unselectedCityCodes = uCities
                                    selectedProvinceCodes = sProvinces
                                    unselectedProvinceCodes = uProvinces
                                })
                        }

                        LightPanel2Tab.CORNER -> {

                            CornerContent(
                                lightedProvinceCount = lightedProvinceCount,
                                lightCityList = lightCityList
                            )
                        }

                        LightPanel2Tab.CHECK_IN -> {

                            CheckInContent(
                                lightCityList = lightCityList, checkInRecords = checkInRecords,

                                onAddCheckIn = { adcode, cityName, note ->

                                    checkInRecords = checkInRecords + CheckInRecord(
                                        cityAdcode = adcode,
                                        cityName = cityName,
                                        note = note,
                                        time = Date()
                                    )
                                })
                        }

                        LightPanel2Tab.MILESTONE -> {

                            MilestoneContent(
                                lightedProvinceCount = lightedProvinceCount
                            )
                        }
                    }
                }
            }

            // ================= 底部按钮 =================

            if (isExpanded && selectedTab == LightPanel2Tab.LIGHT_UP) {

                BottomActionButtons(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    lightPanel2State = lightPanel2State,

                    isDeleteMode = isDeleteMode,

                    lightCityList = lightCityList,

                    lightedProvinces = lightedProvinces,

                    lightenCityMode = lightenCityMode,

                    selectedCityCodes = selectedCityCodes,

                    unselectedCityCodes = unselectedCityCodes,

                    selectedProvinceCodes = selectedProvinceCodes,

                    unselectedProvinceCodes = unselectedProvinceCodes,

                    onStateChange = {
                        lightPanel2State = it
                    },

                    onDeleteModeChange = {
                        isDeleteMode = it
                    },

                    onLightenViewModel = lightenViewModel,

                    onSelectionReset = {

                        selectedCityCodes = emptySet()
                        unselectedCityCodes = emptySet()
                        selectedProvinceCodes = emptySet()
                        unselectedProvinceCodes = emptySet()
                    })
            }
        }
    }
}

// ========== 内容区域（不含底部按钮） ==========
@Composable
private fun LightUpContentOnly(
    lightPanel2State: LightPanel2State,
    lightCityList: List<LightedCity>,
    lightedCityCount: Int,
    lightedProvinces: List<LightedProvince>,
    lightedProvinceCount: Int,
    lightenCityMode: LightenCityMode,
    isDeleteMode: Boolean,
    scrollableMaxHeight: androidx.compose.ui.unit.Dp,
    onStateChange: (LightPanel2State) -> Unit,
    onDeleteModeChange: (Boolean) -> Unit,
    onLightenViewModel: LightenViewModel,
    onSelectionChanged: (Set<String>, Set<String>, Set<String>, Set<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = scrollableMaxHeight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        LightCityScreenWithState(
            lightPanel2State = lightPanel2State,
            lightCityList = lightCityList,
            lightedProvinces = lightedProvinces,
            lightenCityMode = lightenCityMode,
            isDeleteMode = isDeleteMode,
            onDeleteProvince = { provinceCode ->
                onLightenViewModel.unlightProvince(provinceCode)
            },
            onDeleteCity = { cityCode ->
                onLightenViewModel.unlightCity(cityCode)
            })

        if (lightPanel2State == LightPanel2State.EDIT) {
            LightCityEditScreen(
                lightPanel2State = lightPanel2State,
                lightenCityMode = lightenCityMode,
                initialSelectedCityCodes = emptySet(),
                initialSelectedProvinceCodes = emptySet(),
                onSelectionChanged = onSelectionChanged
            )
        }

        if (lightPanel2State != LightPanel2State.EDIT) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "点亮记录",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(12.dp))

            val provinceTimeline = remember(lightCityList) {
                lightCityList.groupBy { it.provinceAdcode }.map { (_, cities) ->
                    val latest = cities.maxByOrNull { it.lightedTime }
                    ProvinceTimelineItem(
                        provinceName = cities.first().provinceName,
                        provinceAdcode = cities.first().provinceAdcode,
                        cityCount = cities.size,
                        latestLightTime = latest?.lightedTime ?: Date(0)
                    )
                }.sortedByDescending { it.latestLightTime }
            }

            provinceTimeline.forEach { item ->
                ProvinceTimelineRow(item = item)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ========== 底部按钮组件 ==========
@Composable
private fun BottomActionButtons(
    modifier: Modifier = Modifier,
    lightPanel2State: LightPanel2State,
    isDeleteMode: Boolean,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightenCityMode: LightenCityMode,
    selectedCityCodes: Set<String>,
    unselectedCityCodes: Set<String>,
    selectedProvinceCodes: Set<String>,
    unselectedProvinceCodes: Set<String>,
    onStateChange: (LightPanel2State) -> Unit,
    onDeleteModeChange: (Boolean) -> Unit,
    onLightenViewModel: LightenViewModel,
    onSelectionReset: () -> Unit
) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lightPanel2State == LightPanel2State.EDIT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onLightenViewModel.applyLightingChanges(
                            selectedCityCodes = selectedCityCodes,
                            unselectedCityCodes = unselectedCityCodes,
                            selectedProvinceCodes = selectedProvinceCodes,
                            unselectedProvinceCodes = unselectedProvinceCodes
                        )
                        onStateChange(LightPanel2State.ROUGH_DISPLAY)
                        onSelectionReset()
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B7280)
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "保存",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = {
                        onStateChange(LightPanel2State.ROUGH_DISPLAY)
                        onSelectionReset()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text(
                        text = "取消", fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            if (isDeleteMode) {
                Button(
                    onClick = {
                        onDeleteModeChange(false)
                        onLightenViewModel.refreshAllData()
                        Log.d("LightPanel2", "刷新数据完成")
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B7280)
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "完成",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDeleteModeChange(true) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text(
                            text = "取消点亮", fontSize = 14.sp, fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = {
                            // 进入编辑模式时，选中所有已点亮的内容
                            onStateChange(LightPanel2State.EDIT)
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B7280)
                        ), shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "点亮${if (lightenCityMode == LightenCityMode.CITY) "城市" else "省份"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ========== 数据类和辅助函数 ==========

data class ProvinceTimelineItem(
    val provinceName: String,
    val provinceAdcode: String,
    val cityCount: Int,
    val latestLightTime: Date
)

@Composable
private fun ProvinceTimelineRow(item: ProvinceTimelineItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.provinceName.take(1),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.provinceName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "【测试示例】${item.provinceName}拥有丰富的自然景观和人文历史，是旅行爱好者不可错过的目的地。这里有壮丽的山川、古老的寺庙和美味的特色美食。",
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${item.cityCount}个城市",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3B82F6)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = SimpleDateFormat("MM/dd", Locale.getDefault()).format(item.latestLightTime),
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun LightCityScreenWithState(
    lightPanel2State: LightPanel2State,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightenCityMode: LightenCityMode,
    isDeleteMode: Boolean,
    onDeleteProvince: (String) -> Unit,
    onDeleteCity: (String) -> Unit
) {
    key(lightPanel2State) {
        LightCityScreen(
            lightPanel2State = lightPanel2State,
            lightCityList = lightCityList,
            lightedProvinces = lightedProvinces,
            lightenCityMode = lightenCityMode,
            isDeleteMode = isDeleteMode,
            onDeleteProvince = onDeleteProvince,
            onDeleteCity = onDeleteCity
        )
    }
}