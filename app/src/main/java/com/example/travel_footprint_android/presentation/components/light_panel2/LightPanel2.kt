package com.example.travel_footprint_android.presentation.components.light_panel2

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation.components.back_buttom.city_province_backButtom
import com.example.travel_footprint_android.presentation.components.light_panel2.checkin.CheckInContent
import com.example.travel_footprint_android.presentation.components.light_panel2.checkin.CheckInRecord
import com.example.travel_footprint_android.presentation.components.light_panel2.corner.CornerContent
import com.example.travel_footprint_android.presentation.components.light_panel2.light_city.LightCityScreen
import com.example.travel_footprint_android.presentation.components.light_panel2.light_city_edit.LightCityEditScreen
import com.example.travel_footprint_android.presentation.components.light_panel2.milestone.MilestoneContent
import com.example.travel_footprint_android.presentation.components.light_panel2.panel_title.PanelTitle
import com.example.travel_footprint_android.presentation.components.svg_map.ShowMapMode
import com.example.travel_footprint_android.presentation.screen.nav_screen.LightenCityMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun LightPanel2(
    modifier: Modifier = Modifier,
    lightenCityMode: LightenCityMode,
    lightenViewModel: LightenViewModel = hiltViewModel(),
    onExpandedChanged: ((Boolean) -> Unit)? = null,
    showMapMode: ShowMapMode? = null,
    onBackButtonClick: (() -> Unit)? = null,
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

    // 从角落页签跳转到打卡页签时，记录省份 adcode
    var selectedProvinceAdcode by remember {
        mutableStateOf<String?>(null)
    }

    // 打卡记录（已持久化到数据库）
    val dbCheckInRecords by lightenViewModel.checkInRecords.collectAsState()
    val checkInRecords = remember(dbCheckInRecords) {
        dbCheckInRecords.map { entity ->
            CheckInRecord(
                cityAdcode = entity.cityAdcode,
                cityName = entity.cityName,
                note = entity.note,
                time = entity.time,
                tags = entity.tags,
                photoPaths = entity.photoPaths
            )
        }
    }

    // 所有足迹
    val allFootprints by lightenViewModel.allFootprints.collectAsState()

    val configuration = LocalConfiguration.current
    val density = configuration.densityDpi.toFloat() / 160f
    val screenHeightPixels = configuration.screenHeightDp * density

    var currentHeightRatio by remember { mutableFloatStateOf(0.4f) }
    var isDragging by remember { mutableStateOf(false) }

    val aniPanelHeight = if (isDragging) {
        currentHeightRatio
    } else {
        animateFloatAsState(
            targetValue = currentHeightRatio,
            animationSpec = tween(durationMillis = 300),
            label = "lightPanelHeight"
        ).value
    }

    val isExpanded = currentHeightRatio > 0.5f

    LaunchedEffect(isExpanded) {
        onExpandedChanged?.invoke(isExpanded)
    }

    val togglePanelHeight = { _: Boolean ->
        if (!isDragging) {
            currentHeightRatio = if (currentHeightRatio < 0.5f) 0.6f else 0.4f
        }
    }

    val onDragDelta = { deltaY: Float ->
        val ratioDelta = -deltaY / screenHeightPixels
        currentHeightRatio = (currentHeightRatio + ratioDelta).coerceIn(0.2f, 0.8f)
    }

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
        (configuration.screenHeightDp.dp * currentHeightRatio - 60.dp).coerceAtLeast(0.dp)

    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val offsetPx = 60.dp.roundToPx()
                val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)
                layout(placeable.width, layoutHeight) {
                    placeable.placeRelative(0, -offsetPx)
                }
            }
    ) {
        if (showMapMode != null && onBackButtonClick != null) {
            city_province_backButtom(
                showMapMode = showMapMode,
                panelIsExpanded = true,
                onClick = onBackButtonClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-70).dp)
            )
        }


        Column {
            // ================= 拖拽区域 =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)  // 拖拽触控区域
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isDragging = true },
                            onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                            onDragEnd = { isDragging = false }
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // 视觉上的浮动指示器 - 与面板分离
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(2.dp),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        )
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE2E8F0).copy(alpha = 0.9f),
                                    Color(0xFF94A3B8).copy(alpha = 0.9f),
                                    Color(0xFFE2E8F0).copy(alpha = 0.9f)
                                )
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(configuration.screenHeightDp.dp * aniPanelHeight)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                        clip = true
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isDragging = true },
                            onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                            onDragEnd = { isDragging = false }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // ================= Tab 标题 =================

                    PanelTitle(
                        modifier = Modifier,
                        selectedTab = selectedTab, onTabSelected = { tab ->
                            selectedTab = tab
                            selectedProvinceAdcode = null
                            if (!isExpanded) {
                                currentHeightRatio = 0.6f
                            }
                        }
                    )

                    // ================= 内容区域 =================

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {

                        if (true) {

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
                                        lightCityList = lightCityList,
                                        onGoCheckIn = { provinceAdcode ->
                                            selectedProvinceAdcode = provinceAdcode
                                            selectedTab = LightPanel2Tab.CHECK_IN
                                            if (!isExpanded) {
                                                currentHeightRatio = 0.6f
                                            }
                                        }
                                    )
                                }

                                LightPanel2Tab.CHECK_IN -> {

                                    CheckInContent(
                                        lightCityList = lightCityList,
                                        checkInRecords = checkInRecords,
                                        currentProvinceAdcode = selectedProvinceAdcode,
                                        onAddCheckIn = { adcode, cityName, note ->
                                            lightenViewModel.addCheckInRecord(
                                                adcode,
                                                cityName,
                                                note
                                            )
                                        },
                                        onAddCheckInRich = { adcode, cityName, note, tags, photoPaths ->
                                            lightenViewModel.addCheckInRecord(
                                                adcode,
                                                cityName,
                                                note,
                                                tags,
                                                photoPaths
                                            )
                                        },
                                        onProvinceFilterCleared = {
                                            selectedProvinceAdcode = null
                                        })
                                }

                                LightPanel2Tab.MILESTONE ->

                                    MilestoneContent(
                                        lightCityList = lightCityList,
                                        lightedProvinceCount = lightedProvinceCount,
                                        allFootprints = allFootprints
                                    )
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
            .padding(6.dp)
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
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.height( 12.dp))

            Text(
                text = "点亮记录",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(Modifier.height(6.dp))

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