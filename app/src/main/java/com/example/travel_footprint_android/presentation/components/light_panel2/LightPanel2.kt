package com.example.travel_footprint_android.presentation.components.light_panel2

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation.components.back_buttom.city_province_backButtom
import com.example.travel_footprint_android.presentation.components.light_panel2.checkin.CheckInContent
import com.example.travel_footprint_android.presentation.components.light_panel2.checkin.CheckInRecord
import com.example.travel_footprint_android.presentation.components.light_panel2.corner.CornerContent
import com.example.travel_footprint_android.presentation.components.light_panel2.light_city.LightCityScreen
import com.example.travel_footprint_android.presentation.components.light_panel2.light_city_edit.LightCityEditScreen
import com.example.travel_footprint_android.presentation.components.light_panel2.panel_title.PanelTitle
import com.example.travel_footprint_android.presentation.components.svg_map.ShowMapMode
import com.example.travel_footprint_android.presentation.screen.nav_screen.LightenCityMode
import kotlinx.serialization.json.JsonNull.content
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

    // ========== 拆分为独立状态流，避免跨 Tab 重组污染 ==========
    val lightCityList by lightenViewModel.lightedCitiesList.collectAsState()
    val lightedProvinces by lightenViewModel.lightedProvincesList.collectAsState()
    val lightedProvinceCount by lightenViewModel.lightedProvinceCountFlow.collectAsState()
    val dbCheckInRecords by lightenViewModel.checkInRecords.collectAsState()
    val allFootprints by lightenViewModel.allFootprints.collectAsState()

    // ✅ 添加 selectionState 状态定义
    var selectionState by remember {
        mutableStateOf(SelectionState())
    }

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

    var lightPanel2State by remember {
        mutableStateOf(LightPanel2State.ROUGH_DISPLAY)
    }

    var isDeleteMode by remember {
        mutableStateOf(false)
    }

    // Tab
    var selectedTab by remember {
        mutableStateOf<LightPanel2Tab?>(null)
    }

    // 从角落页签跳转到打卡页签时，记录省份 adcode
    var selectedProvinceAdcode by remember {
        mutableStateOf<String?>(null)
    }

    val configuration = LocalConfiguration.current

    // ========== 稳定回调（不捕获拖拽状态） ==========
    val onSelectionChanged = remember {
        { sCities: Set<String>, uCities: Set<String>, sProvinces: Set<String>, uProvinces: Set<String> ->
            selectionState = SelectionState(
                selectedCityCodes = sCities,
                unselectedCityCodes = uCities,
                selectedProvinceCodes = sProvinces,
                unselectedProvinceCodes = uProvinces
            )
        }
    }

    val onAddCheckIn = remember {
        { adcode: String, cityName: String, note: String ->
            lightenViewModel.addCheckInRecord(adcode, cityName, note)
        }
    }

    val onAddCheckInRich = remember {
        { adcode: String, cityName: String, note: String, tags: List<String>, photoPaths: List<String> ->
            lightenViewModel.addCheckInRecord(adcode, cityName, note, tags, photoPaths)
        }
    }

    val onProvinceFilterCleared = remember {
        { selectedProvinceAdcode = null }
    }

    val onStateChange = remember {
        { state: LightPanel2State -> lightPanel2State = state }
    }

    val onDeleteModeChange = remember {
        { mode: Boolean -> isDeleteMode = mode }
    }

    val onSelectionReset = remember {
        { selectionState = SelectionState() }
    }

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
                    .padding(top = 240.dp, end = 12.dp)
            )
        }

        DragPanelContainer(
            screenHeightDp = configuration.screenHeightDp,
            onExpandedChanged = onExpandedChanged,
        ) { isExpanded, requestExpand ->
            PanelTitle(
                modifier = Modifier.fillMaxWidth(),
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )

            LightPanelBody(
                selectedTab = selectedTab,
                lightPanel2State = lightPanel2State,
                isDeleteMode = isDeleteMode,
                isExpanded = isExpanded,
                lightCityList = lightCityList,
                lightedProvinces = lightedProvinces,
                lightedProvinceCount = lightedProvinceCount,
                lightenCityMode = lightenCityMode,
                checkInRecords = checkInRecords,
                selectedProvinceAdcode = selectedProvinceAdcode,
                allFootprints = allFootprints,
                selectionState = selectionState,
                onSelectionChanged = onSelectionChanged,
                onAddCheckIn = onAddCheckIn,
                onAddCheckInRich = onAddCheckInRich,
                onProvinceFilterCleared = onProvinceFilterCleared,
                onGoCheckIn = { provinceCode ->
                    selectedProvinceAdcode = provinceCode
                    selectedTab = LightPanel2Tab.CHECK_IN
                    requestExpand()
                },
                onStateChange = onStateChange,
                onDeleteModeChange = onDeleteModeChange,
                onSelectionReset = onSelectionReset
            )
        }
    }
}


// ========== 拖拽容器（隔离重组作用域） ==========
// 使用 NestedScrollConnection 处理 overscroll 时的面板缩放，
// 让子级 LazyColumn 的滚动不受干扰。
@Composable
private fun DragPanelContainer(
    screenHeightDp: Int,
    onExpandedChanged: ((Boolean) -> Unit)?,
    content: @Composable ColumnScope.(isExpanded: Boolean, requestExpand: () -> Unit) -> Unit
) {
    var currentHeightRatio by remember { mutableFloatStateOf(0.4f) }
    var isDragging by remember { mutableStateOf(false) }

    val isExpanded by remember { derivedStateOf { currentHeightRatio > 0.5f } }

    LaunchedEffect(isExpanded) {
        onExpandedChanged?.invoke(isExpanded)
    }

    val configuration = LocalConfiguration.current
    val screenHeightPixels = remember(screenHeightDp, configuration) {
        val density = configuration.densityDpi.toFloat() / 160f
        screenHeightDp * density
    }

    val aniPanelHeight = if (isDragging) {
        currentHeightRatio
    } else {
        animateFloatAsState(
            targetValue = currentHeightRatio,
            animationSpec = tween(durationMillis = 300),
            label = "lightPanelHeight"
        ).value
    }

    val requestExpand = remember {
        { if (currentHeightRatio < 0.5f) currentHeightRatio = 0.6f }
    }

    // NestedScrollConnection：当子级 Scrollable（LazyColumn）滚动到边界后，
    // 剩余的拖拽距离用于面板缩放
    val nestedScrollConnection = remember(screenHeightPixels) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 不干预子级滚动前的消费
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 子级消费后还有剩余（overscroll），用来缩放面板
                if (available.y != 0f) {
                    val ratioDelta = -available.y / screenHeightPixels
                    currentHeightRatio = (currentHeightRatio + ratioDelta).coerceIn(0.2f, 0.8f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }

    // 固定 Box 高度 = 最大可能高度（ratio 上限 0.8），通过 offset 模拟不同可见高度
    // 底部始终锚定：offset 只向下推（正值），不向上拉
    //   aniPanelHeight=0.8f → offset=0       → 可见高度 = 80%（全量）
    //   aniPanelHeight=0.4f → offset=max*0.5  → 可见高度 = 40%
    //   aniPanelHeight=0.2f → offset=max*0.75 → 可见高度 = 20%
    val maxHeightDp = screenHeightDp.dp * 0.7f
    val offsetDp = maxHeightDp * (0.8f - aniPanelHeight) / 0.8f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeightDp)
            .offset(y = offsetDp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                clip = true
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            // 顶部拖拽手柄（用于直接拖拽缩放面板，不影响 LazyColumn 滚动）
            DragHandle(
                screenHeightPixels = screenHeightPixels,
                onDragStart = { isDragging = true },
                onDrag = { ratioDelta ->
                    currentHeightRatio = (currentHeightRatio + ratioDelta).coerceIn(0.2f, 0.8f)
                },
                onDragEnd = { isDragging = false }
            )

            // 面板内容（ColumnScope 上下文，content 可调用 LightPanelBody 等）
            content(isExpanded, requestExpand)
        }
    }
}

// ========== 顶部拖拽手柄 ==========
@Composable
private fun DragHandle(
    screenHeightPixels: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onVerticalDrag = { _, dragAmount ->
                        val ratioDelta = -dragAmount / screenHeightPixels
                        onDrag(ratioDelta)
                    },
                    onDragEnd = { onDragEnd() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 视觉手柄条
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFD1D5DB))
        )
    }
}

// ========== 内容区域（不含底部按钮） ==========
@Composable
private fun LightUpContentOnly(
    lightPanel2State: LightPanel2State,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightenCityMode: LightenCityMode,
    isDeleteMode: Boolean,
    onSelectionChanged: (Set<String>, Set<String>, Set<String>, Set<String>) -> Unit
) {
    val lightenViewModel: LightenViewModel = hiltViewModel()
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
    Log.d("是枫树","$provinceTimeline")

    var showDetailProvince by remember { mutableStateOf<ProvinceTimelineItem?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        item { LightCityScreenWithState(
            lightPanel2State = lightPanel2State,
            lightCityList = lightCityList,
            lightedProvinces = lightedProvinces,
            lightenCityMode = lightenCityMode,
            isDeleteMode = isDeleteMode,
            onDeleteProvince = { provinceCode ->
                lightenViewModel.unlightProvince(provinceCode)
            },
            onDeleteCity = { cityCode ->
                lightenViewModel.unlightCity(cityCode)
            })

        }

        item {
            if (lightPanel2State == LightPanel2State.EDIT) {
                LightCityEditScreen(
                    lightPanel2State = lightPanel2State,
                    lightenCityMode = lightenCityMode,
                    initialSelectedCityCodes = emptySet(),
                    initialSelectedProvinceCodes = emptySet(),
                    onSelectionChanged = onSelectionChanged
                )
            }
        }

        item {
            if (lightPanel2State != LightPanel2State.EDIT) {
                Spacer(Modifier.height(6.dp))
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
                Spacer(Modifier.height(6.dp))
            }
        }

        items(provinceTimeline,key={it.provinceAdcode}){ item ->
            ProvinceTimelineRow(
                item = item,
                onClick = { showDetailProvince = item }
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    // 点亮记录详情弹窗
    showDetailProvince?.let { province ->
        ProvinceDetailDialog(
            item = province,
            onDismiss = { showDetailProvince = null }
        )
    }
}

// ========== 底部按钮组件 ==========
@Composable
private fun BottomActionButtons(
    modifier: Modifier = Modifier,
    lightPanel2State: LightPanel2State,
    isDeleteMode: Boolean,
    lightenCityMode: LightenCityMode,
    selectedCityCodes: Set<String>,
    unselectedCityCodes: Set<String>,
    selectedProvinceCodes: Set<String>,
    unselectedProvinceCodes: Set<String>,
    onStateChange: (LightPanel2State) -> Unit,
    onDeleteModeChange: (Boolean) -> Unit,
    onSelectionReset: () -> Unit
) {
    val lightenViewModel: LightenViewModel = hiltViewModel()
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
                        lightenViewModel.applyLightingChanges(
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
                        lightenViewModel.refreshAllData()
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
private fun ProvinceTimelineRow(item: ProvinceTimelineItem, onClick: () -> Unit = {}) {
    val context = LocalContext.current
    val imageRes = remember(item.provinceName) {
        context.resources.getIdentifier(
            provinceImageName(item.provinceName), "drawable", context.packageName
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageRes != 0) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = item.provinceName,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
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

// ========== 面板主体内容（提取为独立 Composable 以优化重组性能） ==========
@Composable
private fun ColumnScope.LightPanelBody(
    selectedTab: LightPanel2Tab?,
    lightPanel2State: LightPanel2State,
    isDeleteMode: Boolean,
    isExpanded: Boolean,
    lightCityList: List<LightedCity>,
    lightedProvinces: List<LightedProvince>,
    lightedProvinceCount: Int,
    lightenCityMode: LightenCityMode,
    checkInRecords: List<CheckInRecord>,
    selectedProvinceAdcode: String?,
    allFootprints: List<Footprint>,
    selectionState: SelectionState,
    onSelectionChanged: (Set<String>, Set<String>, Set<String>, Set<String>) -> Unit,
    onAddCheckIn: (String, String, String) -> Unit,
    onAddCheckInRich: (String, String, String, List<String>, List<String>) -> Unit,
    onProvinceFilterCleared: () -> Unit,
    onGoCheckIn: (String) -> Unit,
    onStateChange: (LightPanel2State) -> Unit,
    onDeleteModeChange: (Boolean) -> Unit,
    onSelectionReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        // 记录所有已显示过的Tab，保持其内容在composition中
        var displayedTabs by remember { mutableStateOf(setOf<LightPanel2Tab>()) }
        
        // 当选择新Tab时，将其添加到已显示集合中
        if (selectedTab != null && !displayedTabs.contains(selectedTab!!)) {
            displayedTabs = displayedTabs + selectedTab!!
        }

        // 渲染所有已显示过的Tab内容，但只显示当前选中的Tab
        for (tab in displayedTabs) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // 只显示当前选中的Tab，其他Tab隐藏但保持在composition中
                    .graphicsLayer { alpha = if (tab == selectedTab) 1f else 0f }
                    // 使用graphicsLayer而不是visibility，保持组件在composition中
            ) {
                when (tab) {
                    LightPanel2Tab.LIGHT_UP -> {
                        LightUpContentOnly(
                            lightPanel2State = lightPanel2State,
                            lightCityList = lightCityList,
                            lightedProvinces = lightedProvinces,
                            lightenCityMode = lightenCityMode,
                            isDeleteMode = isDeleteMode,
                            onSelectionChanged = onSelectionChanged
                        )
                    }

                    LightPanel2Tab.CORNER -> {
                        CornerContent(
                            lightedProvinceCount = lightedProvinceCount,
                            lightCityList = lightCityList,
                            onGoCheckIn = onGoCheckIn
                        )
                    }

                    LightPanel2Tab.CHECK_IN -> {
                        CheckInContent(
                            lightCityList = lightCityList,
                            checkInRecords = checkInRecords,
                            currentProvinceAdcode = selectedProvinceAdcode,
                            onAddCheckIn = onAddCheckIn,
                            onAddCheckInRich = onAddCheckInRich,
                            onProvinceFilterCleared = onProvinceFilterCleared
                        )
                    }
                }
            }
        }
    }

    if (isExpanded && selectedTab == LightPanel2Tab.LIGHT_UP) {
        BottomActionButtons(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            lightPanel2State = lightPanel2State,
            isDeleteMode = isDeleteMode,
            lightenCityMode = lightenCityMode,
            selectedCityCodes = selectionState.selectedCityCodes,
            unselectedCityCodes = selectionState.unselectedCityCodes,
            selectedProvinceCodes = selectionState.selectedProvinceCodes,
            unselectedProvinceCodes = selectionState.unselectedProvinceCodes,
            onStateChange = onStateChange,
            onDeleteModeChange = onDeleteModeChange,
            onSelectionReset = onSelectionReset
        )
    }
}

// ========== 编辑模式选中状态 ==========
data class SelectionState(
    val selectedCityCodes: Set<String> = emptySet(),
    val unselectedCityCodes: Set<String> = emptySet(),
    val selectedProvinceCodes: Set<String> = emptySet(),
    val unselectedProvinceCodes: Set<String> = emptySet()
)

/** 省份中文名 → 英文资源名映射 */
private fun provinceImageName(provinceName: String): String = when (provinceName) {
    "河北省" -> "hebei"
    "山西省" -> "shanxi"
    "辽宁省" -> "liaoning"
    "吉林省" -> "jilin"
    "黑龙江省" -> "heilongjiang"
    "江苏省" -> "jiangsu"
    "浙江省" -> "zhejiang"
    "安徽省" -> "anhui"
    "福建省" -> "fujian"
    "江西省" -> "jiangxi"
    "山东省" -> "shandong"
    "河南省" -> "henan"
    "湖北省" -> "hubei"
    "湖南省" -> "hunan"
    "广东省" -> "guangdong"
    "海南省" -> "hainan"
    "四川省" -> "sichuan"
    "贵州省" -> "guizhou"
    "云南省" -> "yunnan"
    "陕西省" -> "shaanxi"
    "甘肃省" -> "gansu"
    "青海省" -> "qinghai"
    "台湾省" -> "taiwan"
    "内蒙古自治区" -> "neimenggu"
    "广西壮族自治区" -> "guangxi"
    "西藏自治区" -> "xizang"
    "宁夏回族自治区" -> "ningxia"
    "新疆维吾尔自治区" -> "xinjiang"
    "北京市" -> "beijing"
    "上海市" -> "shanghai"
    "天津市" -> "tianjin"
    "重庆市" -> "chongqing"
    "香港特别行政区" -> "hongkong"
    "澳门特别行政区" -> "macau"
    else -> provinceName // fallback
}

/** 省份文旅介绍 */
private fun provinceTourismIntro(provinceName: String): String = when (provinceName) {
    "北京市" -> "北京，中华人民共和国首都，拥有3000多年建城史和800多年建都史。这里有故宫、长城、天坛等世界文化遗产，也有胡同、四合院等老北京风情，是现代与传统的完美融合。"
    "天津市" -> "天津，中国四大直辖市之一，素有 '河海要冲'之称。五大道万国建筑群、天津之眼摩天轮、古文化街等景点各具特色，狗不理包子、十八街麻花等美食享誉全国。"
    "河北省" -> "河北，环绕京津，拥有承德避暑山庄、山海关、北戴河等著名景点。这里是中华文明的重要发源地之一，燕赵文化源远流长。"
    "山西省" -> "山西，表里山河，五台山、云冈石窟、平遥古城、壶口瀑布等世界级景点云集。山西面食闻名天下，老陈醋更是享誉中外。"
    "内蒙古自治区" -> "内蒙古，中国最辽阔的草原牧区，呼伦贝尔草原、鄂尔多斯沙漠、阿尔山天池等自然景观壮美。蒙古族文化浓郁，烤全羊、马奶酒令人难忘。"
    "辽宁省" -> "辽宁，东北三省之一，沈阳故宫、大连海滨、本溪水洞等景点兼具历史与自然之美。共和国工业长子，振兴东北的排头兵。"
    "吉林省" -> "吉林，长白山、松花湖、雾凇奇观闻名遐迩。冰雪旅游胜地，被誉为'滑雪天堂'，延边朝鲜族民俗文化独具魅力。"
    "黑龙江省" -> "黑龙江，中国最北省份，哈尔滨冰雪大世界、五大连池、北极村等特色鲜明。冰雪资源丰富，俄式风情浓郁。"
    "上海市" -> "上海，中国最大城市，国际化大都市。外滩万国建筑群、陆家嘴天际线、豫园老城厢交相辉映，购物天堂与美食之都。"
    "江苏省" -> "江苏，江南水乡代表，苏州园林、南京明孝陵、扬州瘦西湖等美不胜收。吴韵汉风，人文荟萃，是中国经济最发达的省份之一。"
    "浙江省" -> "浙江，杭州西湖、乌镇水乡、普陀山等风景如画。浙商遍布全球，数字经济发展引领全国，是创新创业的热土。"
    "安徽省" -> "安徽，黄山奇松怪石、宏村徽派建筑、九华山佛国圣地。徽商文化、文房四宝、徽菜美食共同构成独特的徽州魅力。"
    "福建省" -> "福建，武夷山双世遗、厦门鼓浪屿、福建土楼等世界闻名。闽南文化、妈祖信仰、茶文化深深扎根于这片山海之间。"
    "江西省" -> "江西，庐山雄奇、三清山秀美、婺源油菜花海如画。景德镇瓷器千年传承，红色革命根据地井冈山承载着厚重的历史记忆。"
    "山东省" -> "山东，齐鲁大地，孔子故乡。泰山雄伟、青岛海滨、威海仙境、曲阜三孔，文化与自然交相辉映，鲁菜更是八大菜系之首。"
    "河南省" -> "河南，华夏文明发源地之一，洛阳龙门石窟、安阳殷墟、嵩山少林寺闻名天下。豫剧、烩面、胡辣汤承载着中原大地的烟火气息。"
    "湖北省" -> "湖北，千湖之省，武汉黄鹤楼、宜昌三峡、武当山古建筑群令人神往。热干面、武昌鱼、恩施土家风情独具魅力。"
    "湖南省" -> "湖南，张家界奇峰异石、凤凰古城浪漫多姿、岳阳楼千古名篇。湘菜火辣鲜香，湘江两岸人文荟萃，红色文化底蕴深厚。"
    "广东省" -> "广东，粤港澳大湾区核心省份，广州塔、深圳世界之窗、丹霞山等景点热门。粤菜享誉世界，广府文化、潮汕文化多元交融。"
    "广西壮族自治区" -> "广西，桂林山水甲天下，阳朔漓江如诗如画。德天跨国瀑布、北海银滩等景点美不胜收，壮族民俗文化丰富多彩。"
    "海南省" -> "海南，中国唯一热带岛屿省份，三亚天涯海角、亚龙湾、蜈支洲岛等度假圣地。椰风海韵，四季如春，是避寒疗养的理想之地。"
    "重庆市" -> "重庆，山城重庆以火锅、轻轨穿楼、洪崖洞夜景闻名。长江、嘉陵江两江交汇，武隆喀斯特地貌壮丽奇特。"
    "四川省" -> "四川，天府之国，成都大熊猫、九寨沟仙境、峨眉山佛光、乐山大佛等名扬四海。川菜火锅麻辣鲜香，巴蜀文化底蕴深厚。"
    "贵州省" -> "贵州，黄果树瀑布气势磅礴、荔波小七孔玲珑秀美、千户苗寨民族风情浓郁。大数据产业蓬勃发展，是数字经济的后起之秀。"
    "云南省" -> "云南，七彩云南，大理苍山洱海、丽江古城、香格里拉、西双版纳热带雨林等多彩多姿。25个少数民族在此和谐共生。"
    "西藏自治区" -> "西藏，世界屋脊，布达拉宫巍峨壮丽、纳木错圣湖清澈纯净、珠穆朗玛峰世界之巅。藏传佛教文化神秘而深邃。"
    "陕西省" -> "陕西，千年古都西安，兵马俑、大雁塔、华山、壶口瀑布等名胜闻名中外。秦腔、凉皮、肉夹馍诠释着三秦大地的豪迈与热情。"
    "甘肃省" -> "甘肃，丝绸之路黄金段，莫高窟壁画震撼世界、七彩丹霞地貌绚丽多彩、嘉峪关长城雄关屹立。兰州牛肉面香飘万里。"
    "青海省" -> "青海，青海湖碧波万顷、茶卡盐湖天空之镜、可可西里无人区神秘壮美。三江源是中华民族的母亲河发源地。"
    "宁夏回族自治区" -> "宁夏，塞上江南，贺兰山岩画、沙湖景区、西夏王陵诉说着古老文明。枸杞、滩羊等特产闻名遐迩。"
    "新疆维吾尔自治区" -> "新疆，占全国六分之一面积，天山天池、喀纳斯湖、吐鲁番葡萄沟等美景如画。多民族文化交融，歌舞之乡、瓜果之乡。"
    "台湾省" -> "台湾，日月潭、阿里山、台北故宫博物院等景点闻名遐迩。台湾小吃享誉世界，半导体产业领先全球。"
    "香港特别行政区" -> "香港，东方之珠，维多利亚港夜景璀璨、太平山顶俯瞰全城、迪士尼乐园欢乐无限。购物天堂、美食天堂，中西文化完美交融。"
    "澳门特别行政区" -> "澳门，世界旅游休闲中心，大三巴牌坊见证历史、威尼斯人度假村富丽堂皇、葡式蛋挞香酥可口。中西合璧，别具风情。"
    else -> "${provinceName}拥有独特的自然风光和丰富的文化遗产，等待您亲自探索和发现。"
}

/**
 * 点亮记录详情弹窗
 * 展示省份图片总览、点亮时间和文旅介绍
 */
@Composable
private fun ProvinceDetailDialog(
    item: ProvinceTimelineItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageRes = remember(item.provinceName) {
        context.resources.getIdentifier(
            provinceImageName(item.provinceName), "drawable", context.packageName
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // ---- 图片总览区域 ----
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    if (imageRes != 0) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = item.provinceName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6),
                                            Color(0xFF1D4ED8)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.provinceName.take(1),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // 半透明渐变遮罩（底部）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xB3000000)
                                    )
                                )
                            )
                    )

                    // 省份名称（左上角）
                    Text(
                        text = item.provinceName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )

                    // 点亮时间（右下角）
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "点亮时间",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(item.latestLightTime),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "${item.cityCount}个城市已点亮",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // 关闭按钮（右上角）
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ---- 文旅介绍区域 ----
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // 省份名称标题
                    Text(
                        text = item.provinceName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${item.cityCount}个城市 · 最近点亮 ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.latestLightTime)}",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(Modifier.height(16.dp))

                    // 文旅介绍文字
                    Text(
                        text = "文旅介绍",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = provinceTourismIntro(item.provinceName),
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 22.sp
                    )
                }

                // 关闭按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF3B82F6))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "关闭",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}