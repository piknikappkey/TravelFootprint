package com.example.travel_footprint_android.presentation.components.light_panel2.checkin

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.LightedCity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.withContext
import java.io.File

// ========== 数据模型 ==========

/**
 * 打卡记录数据类
 *
 * @property cityAdcode 城市行政编码
 * @property cityName 城市名称
 * @property note 打卡文字备注
 * @property time 打卡时间
 * @property tags 打卡标签列表，如 #美食、#风景
 * @property photoPaths 打卡照片本地文件路径列表
 */
data class CheckInRecord(
    val cityAdcode: String,
    val cityName: String,
    val note: String,
    val time: Date,
    val tags: List<String> = emptyList(),
    val photoPaths: List<String> = emptyList()
)

// ========== 筛选与常量 ==========

/** 打卡列表筛选条件 */
private enum class CheckInFilter(val label: String) {
    ALL("全部"),
    CHECKED_IN("已打卡"),
    UNCHECKED("未打卡")
}

/** 预设打卡标签，用户可直接点击快速添加 */
private val PRESET_TAGS = listOf("#美食", "#风景", "#出差", "#旅游", "#路过", "#工作","日常")

/** 打卡文字最大字数限制 */
private const val MAX_NOTE_LENGTH = 150

// ========== 主打卡页面 ==========

/**
 * 打卡内容页 — 按省份分组展示已点亮城市的打卡状态
 *
 * @param lightCityList 已点亮城市列表
 * @param checkInRecords 已有打卡记录列表
 * @param currentCityAdcode 当前定位城市编码（高亮显示）
 * @param currentProvinceAdcode 当前筛选省份编码（仅显示该省城市）
 * @param onAddCheckIn 简单打卡回调 (adcode, cityName, note)
 * @param onAddCheckInRich 完整打卡回调 (adcode, cityName, note, tags, photoPaths)
 * @param onCityClick 城市点击回调
 * @param onProvinceFilterCleared 清除省份筛选回调
 */
@Composable
fun CheckInContent(
    lightCityList: List<LightedCity>,
    checkInRecords: List<CheckInRecord>,
    currentCityAdcode: String? = null,
    currentProvinceAdcode: String? = null,
    onAddCheckIn: (String, String, String) -> Unit,
    onAddCheckInRich: ((String, String, String, List<String>, List<String>) -> Unit)? = null,
    onCityClick: ((String) -> Unit)? = null,
    onProvinceFilterCleared: (() -> Unit)? = null
) {
    // ----- 状态定义 -----
    var activeFilter by remember { mutableStateOf(CheckInFilter.ALL) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var highlightedCity by remember { mutableStateOf<String?>(null) }

    // 根据省份编码获取省份名称（用于过滤栏展示）
    val filteredProvinceName = remember(currentProvinceAdcode, lightCityList) {
        currentProvinceAdcode?.let { adcode ->
            lightCityList.firstOrNull { it.provinceAdcode == adcode }?.provinceName
        }
    }

    // ----- 数据分组与排序 -----
    /**
     * 将城市按省份分组，排序规则：
     * 1. 有打卡记录的城市排在前面（按打卡时间降序）
     * 2. 当前定位城市排在该省份最前面
     * 3. 省份之间按名称字母序排列，但包含当前定位城市的省份优先
     */
    val groupedCities = remember(lightCityList, currentCityAdcode) {
        val sorted = lightCityList.sortedByDescending { city ->
            val record = checkInRecords.find { it.cityAdcode == city.cityAdcode }
            record?.time ?: Date(0)
        }

        val withCurrentCity = if (currentCityAdcode != null) {
            val current = sorted.filter { it.cityAdcode == currentCityAdcode }
            val rest = sorted.filter { it.cityAdcode != currentCityAdcode }
            current + rest
        } else sorted

        withCurrentCity.groupBy { it.provinceName }
            .toSortedMap { a, b ->
                val aHasCurrent = withCurrentCity.any { it.provinceName == a && it.cityAdcode == currentCityAdcode }
                val bHasCurrent = withCurrentCity.any { it.provinceName == b && it.cityAdcode == currentCityAdcode }
                when {
                    aHasCurrent && !bHasCurrent -> -1
                    !aHasCurrent && bHasCurrent -> 1
                    else -> a.compareTo(b)
                }
            }
    }

    // ----- 副作用：自动清除提示消息和高亮 -----
    /** 打卡成功提示 2 秒后自动消失 */
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(2000)
            successMessage = null
        }
    }

    /** 城市高亮 1 秒后自动消失 */
    LaunchedEffect(highlightedCity) {
        if (highlightedCity != null) {
            delay(1000)
            highlightedCity = null
        }
    }

    // ----- 按筛选条件过滤 -----
    /**
     * 根据当前筛选条件（全部/已打卡/未打卡）及省份过滤，
     * 过滤掉空组后得到最终展示列表
     */
    val filteredGrouped = remember(activeFilter, groupedCities, checkInRecords, filteredProvinceName) {
        val provinceFiltered = if (filteredProvinceName != null) {
            groupedCities.filterKeys { it == filteredProvinceName }
        } else {
            groupedCities
        }
        provinceFiltered.mapValues { (_, cities) ->
            when (activeFilter) {
                CheckInFilter.ALL -> cities
                CheckInFilter.CHECKED_IN -> cities.filter { c ->
                    checkInRecords.any { it.cityAdcode == c.cityAdcode }
                }
                CheckInFilter.UNCHECKED -> cities.filter { c ->
                    checkInRecords.none { it.cityAdcode == c.cityAdcode }
                }
            }
        }.filter { (_, cities) -> cities.isNotEmpty() }
    }

    // ----- UI 布局 -----
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // 筛选条件标签栏
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CheckInFilter.values().forEach { filter ->
                    FilterTab(
                        label = filter.label,
                        isSelected = filter == activeFilter,
                        onClick = { activeFilter = filter }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 省份筛选提示栏 — 显示当前筛选的省份及返回全部按钮
            if (filteredProvinceName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = filteredProvinceName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "返回全部",
                        fontSize = 12.sp,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onProvinceFilterCleared?.invoke() }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // 空状态 — 无已点亮城市时的提示
            if (lightCityList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无已点亮城市，无法打卡",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            } else {
                // 城市打卡列表 — 按省份分组展示
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredGrouped.forEach { (provinceName, cities) ->
                        // 省份分组标题
                        item(key = "header_$provinceName") {
                            ProvinceGroupHeader(
                                provinceName = provinceName,
                                cityCount = cities.size,
                                checkedCount = cities.count { c ->
                                    checkInRecords.any { it.cityAdcode == c.cityAdcode }
                                }
                            )
                        }
                        // 每个城市的打卡卡片
                        items(cities, key = { it.cityAdcode }) { city ->
                            CheckInCityItem(
                                city = city,
                                existingRecord = checkInRecords.find { it.cityAdcode == city.cityAdcode },
                                isHighlighted = highlightedCity == city.cityAdcode,
                                isCurrentLocation = city.cityAdcode == currentCityAdcode,
                                onCheckIn = { note, tags, photoPaths ->
                                    if (onAddCheckInRich != null) {
                                        onAddCheckInRich(city.cityAdcode, city.cityName, note, tags, photoPaths)
                                    } else {
                                        onAddCheckIn(city.cityAdcode, city.cityName, note)
                                    }
                                    highlightedCity = city.cityAdcode
                                    successMessage = "${city.cityName} 打卡成功"
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }

        // 打卡成功悬浮提示（顶部居中）
//        if (successMessage != null) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(
//                        brush = Brush.linearGradient(
//                            colors = listOf(
//                                Color(0xFF10B981),
//                                Color(0xFF059669)
//                            )
//                        ),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    .shadow(
//                        elevation = 8.dp,
//                        shape = RoundedCornerShape(12.dp),
//                        ambientColor = Color(0xFF10B981).copy(alpha = 0.3f),
//                        spotColor = Color(0xFF10B981).copy(alpha = 0.3f)
//                    )
//                    .padding(horizontal = 20.dp, vertical = 12.dp)
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.CheckCircle,
//                        contentDescription = "成功",
//                        tint = Color.White,
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Spacer(Modifier.width(10.dp))
//                    Text(
//                        text = successMessage ?: "操作成功",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = Color.White
//                    )
//                }
//            }
//        }
    }
}

// ========== 文件工具函数 ==========

/**
 * 将 Uri 指向的图片文件复制到应用内部存储，返回本地文件路径
 *
 * @param context Android 上下文
 * @param uri 图片文件的 Content URI
 * @param fileName 保存的文件名
 * @return 本地文件的绝对路径，失败返回 null
 */
private fun copyUriToFile(context: Context, uri: Uri, fileName: String): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val dir = File(context.filesDir, "checkin_photos")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            file.absolutePath
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 生成唯一的照片文件名，格式: photo_时间戳_随机数.jpg
 */
private fun generatePhotoFileName(): String {
    return "photo_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
}

// ========== 筛选标签组件 ==========

/**
 * 打卡筛选标签按钮
 *
 * @param label 标签显示文字
 * @param isSelected 是否选中
 * @param onClick 点击回调
 */
@Composable
private fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF3B82F6) else Color(0xFFF3F4F6))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF6B7280)
        )
    }
}

// ========== 省份分组标题组件 ==========

/**
 * 省份分组标题行，显示省份名称及已打卡/总数
 *
 * @param provinceName 省份名称
 * @param cityCount 该省已点亮城市总数
 * @param checkedCount 该省已打卡城市数
 */
@Composable
private fun ProvinceGroupHeader(
    provinceName: String,
    cityCount: Int,
    checkedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 蓝色圆点装饰
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = provinceName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF374151)
        )
        Spacer(Modifier.width(8.dp))
        // 打卡进度计数
        Text(
            text = "$checkedCount/$cityCount",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
        )
    }
}

// ========== 城市打卡卡片组件 ==========

/**
 * 单个城市的打卡卡片
 *
 * 功能：
 * - 未打卡：显示城市名 + 打卡按钮，点击展开输入框
 * - 已打卡：显示城市名 + 打卡时间 + 备注/标签/照片预览，点击可查看详情
 * - 当前定位城市：显示"当前位置"标记
 * - 刚打卡成功：绿色高亮背景
 *
 * @param city 城市数据
 * @param existingRecord 已有的打卡记录（null 表示未打卡）
 * @param isHighlighted 是否高亮（刚打卡成功的反馈效果）
 * @param isCurrentLocation 是否为当前定位城市
 * @param onCheckIn 提交打卡回调 (note, tags, photoPaths)
 */
@Composable
private fun CheckInCityItem(
    city: LightedCity,
    existingRecord: CheckInRecord?,
    isHighlighted: Boolean,
    isCurrentLocation: Boolean,
    onCheckIn: (String, List<String>, List<String>) -> Unit
) {
    // ----- 状态 -----
    var showInput by remember { mutableStateOf(false) }    // 是否显示输入区域
    var showDetail by remember { mutableStateOf(false) }   // 是否展开详情（已有记录时）
    var noteText by remember { mutableStateOf("") }        // 备注输入文字
    val selectedTags = remember { mutableStateListOf<String>() }  // 选中标签
    var isSubmitting by remember { mutableStateOf(false) } // 是否正在提交
    val submitScale = remember { Animatable(1f) }          // 提交按钮缩放动画
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedPhotoPaths by remember(existingRecord) { mutableStateOf(existingRecord?.photoPaths ?: emptyList()) }
    var viewingPhotoPath by remember { mutableStateOf<String?>(null) }  // 正在查看大图的照片路径

    // 编辑已有记录时，将旧内容回填到输入框
    LaunchedEffect(showInput) {
        if (showInput && existingRecord != null) {
            noteText = existingRecord.note
            selectedTags.clear()
            selectedTags.addAll(existingRecord.tags)
        }
    }

    // 系统照片选择器（多选）
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val remaining = 9 - selectedPhotoPaths.size
        scope.launch {
            uris.take(remaining).forEach { uri ->
                val path = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    copyUriToFile(context, uri, generatePhotoFileName())
                }
                if (path != null) {
                    selectedPhotoPaths = selectedPhotoPaths + path
                }
            }
        }
    }

    // ----- 颜色计算 -----
    val bgColor = when {
        isHighlighted -> Color(0xFFF1FDF7)       // 打卡成功高亮 — 绿色
        isCurrentLocation -> Color(0xFFEFF6FF)   // 当前位置 — 蓝色调
        existingRecord != null -> Color(0xFFF0FDF4) // 已打卡 — 浅绿
        else -> Color(0xFFF9FAFB)               // 未打卡 — 灰色
    }

    val borderColor = when {
        isHighlighted -> Color(0xFFFFDCE5)       // 高亮边框 — 粉色
        isCurrentLocation -> Color(0xFFBFDBFE)   // 当前位置边框 — 蓝色
        existingRecord != null -> Color(0xFFBBF7D0) // 已打卡边框 — 绿色
        else -> Color(0xFFE5E7EB)               // 默认边框 — 浅灰
    }

    // ----- 卡片主体 -----
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()   // 输入框展开/收起动画
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (existingRecord != null) {
                        showDetail = !showDetail
                        if (showDetail) showInput = false
                    } else {
                        showInput = !showInput
                    }
                }
            )
            .padding(12.dp)
    ) {
        Column {
            // ---- 顶部行：城市名 + 状态标记 + 打卡时间/按钮 ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = city.cityName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (existingRecord != null) Color(0xFF1F2937) else Color(0xFF6B7280)
                    )
                    // 当前位置标记
                    if (isCurrentLocation) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDBEAFE))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "当前位置",
                                fontSize = 9.sp,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (existingRecord != null) {
                    // 已打卡 — 显示打卡时间
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val configuration = LocalConfiguration.current
                        val formattedTime = remember(configuration, existingRecord.time) {
                            SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(existingRecord.time)
                        }
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                } else if (!showInput) {
                    // 未打卡且未展开 — 显示打卡按钮
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3B82F6))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showInput = true }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "打卡",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "打卡",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            // ---- 已有打卡记录 — 摘要展示 ----
            if (existingRecord != null) {
                Spacer(Modifier.height(6.dp))
                // 备注预览
                if (existingRecord.note.isNotBlank()) {
                    Text(
                        text = existingRecord.note,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 标签预览
                if (existingRecord.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        existingRecord.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
//                                    .background(Color(0xFFA9A9A9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = Color(0xFF675252)
                                )
                            }
                        }
                    }
                }
                // 照片数量提示
                if (existingRecord.photoPaths.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${existingRecord.photoPaths.size}张照片",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                // ---- 展开详情 ----
                if (showDetail) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(existingRecord.time),
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    if (existingRecord.note.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = existingRecord.note,
                            fontSize = 13.sp,
                            color = Color(0xFF9CA2B0)
                        )
                    }
                    // 照片缩略图列表（可横向滚动）
                    if (existingRecord.photoPaths.isNotEmpty()) {
                         Spacer(Modifier.height(8.dp))
                         Box(
                             modifier = Modifier.clickable(
                                 interactionSource = remember { MutableInteractionSource() },
                                 indication = null,
                                 onClick = { /* 拦截点击，防止收起详情 */ }
                             )
                         ) {
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .horizontalScroll(rememberScrollState()),
                                 horizontalArrangement = Arrangement.spacedBy(6.dp)
                             ) {
                                 existingRecord.photoPaths.forEach { path ->
                                     Box(
                                         modifier = Modifier
                                             .size(80.dp)
                                             .clip(RoundedCornerShape(6.dp))
                                             .clickable(
                                                 interactionSource = remember { MutableInteractionSource() },
                                                 indication = null,
                                                 onClick = { viewingPhotoPath = path }
                                             )
                                     ) {
                                         AsyncImage(
                                             model = ImageRequest.Builder(LocalContext.current)
                                                 .data(File(path))
                                                 .crossfade(true)
                                                 .size(200)
                                                 .build(),
                                             contentDescription = "打卡照片",
                                             modifier = Modifier
                                                 .fillMaxSize()
                                                 .clipToBounds(),
                                             contentScale = ContentScale.Crop
                                         )
                                     }
                                 }
                             }
                         }
                     }
                }
                Spacer(Modifier.height(6.dp))
                // 修改打卡入口
                if (!showDetail) {
                    Text(
                        text = "修改打卡",
                        fontSize = 12.sp,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showInput = true }
                            )
                    )
                }
            }

            // ---- 新打卡输入区域（未打卡状态） ----
            if (showInput && existingRecord == null) {
                Spacer(Modifier.height(8.dp))
                NoteInputSection(
                    noteText = noteText,
                    onNoteChange = { if (it.length <= MAX_NOTE_LENGTH) noteText = it },
                    selectedTags = selectedTags,
                    onTagToggle = { tag ->
                        if (tag in selectedTags) selectedTags.remove(tag)
                        else selectedTags.add(tag)
                    },
                    isSubmitting = isSubmitting,
                    selectedPhotoPaths = selectedPhotoPaths,
                    onAddPhoto = { photoPickerLauncher.launch("image/*") },
                    onRemovePhoto = { path ->
                        selectedPhotoPaths = selectedPhotoPaths - path
                    },
                    maxPhotoCount = 9
                )
                Spacer(Modifier.height(8.dp))
                // 发送按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(submitScale.value)
                            .clip(CircleShape)
                            .background(
                                if (isSubmitting) Color(0xFF93C5FD)
                                else if (noteText.isNotBlank() || selectedTags.isNotEmpty() || selectedPhotoPaths.isNotEmpty()) Color(0xFF3B82F6)
                                else Color(0xFFD1D5DB)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if ((noteText.isNotBlank() || selectedTags.isNotEmpty() || selectedPhotoPaths.isNotEmpty()) && !isSubmitting) {
                                        scope.launch {
                                            isSubmitting = true
                                            submitScale.animateTo(
                                                1.2f,
                                                tween(150, easing = LinearEasing)
                                            )
                                            submitScale.animateTo(
                                                1f,
                                                tween(150, easing = LinearEasing)
                                            )
                                            delay(200)
                                            onCheckIn(noteText.trim(), selectedTags.toList(), selectedPhotoPaths)
                                            isSubmitting = false
                                            noteText = ""
                                            selectedTags.clear()
                                            selectedPhotoPaths = emptyList()
                                            showInput = false
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSubmitting) {
                            Text(
                                text = "...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ---- 修改打卡输入区域（已打卡状态） ----
            if (showInput && existingRecord != null) {
                Spacer(Modifier.height(8.dp))
                NoteInputSection(
                    noteText = noteText,
                    onNoteChange = { if (it.length <= MAX_NOTE_LENGTH) noteText = it },
                    selectedTags = selectedTags,
                    onTagToggle = { tag ->
                        if (tag in selectedTags) selectedTags.remove(tag)
                        else selectedTags.add(tag)
                    },
                    isSubmitting = isSubmitting,
                    selectedPhotoPaths = selectedPhotoPaths,
                    onAddPhoto = { photoPickerLauncher.launch("image/*") },
                    onRemovePhoto = { path ->
                        selectedPhotoPaths = selectedPhotoPaths - path
                    },
                    maxPhotoCount = 9
                )
                Spacer(Modifier.height(8.dp))
                // 更新按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(submitScale.value)
                            .clip(CircleShape)
                            .background(
                                if (isSubmitting) Color(0xFF93C5FD)
                                else Color(0xFF3B82F6)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    scope.launch {
                                        isSubmitting = true
                                        submitScale.animateTo(
                                            1.2f,
                                            tween(150, easing = LinearEasing)
                                        )
                                        submitScale.animateTo(
                                            1f,
                                            tween(150, easing = LinearEasing)
                                        )
                                        delay(200)
                                        onCheckIn(noteText.trim(), selectedTags.toList(), selectedPhotoPaths)
                                        isSubmitting = false
                                        noteText = ""
                                        selectedTags.clear()
                                        selectedPhotoPaths = emptyList()
                                        showInput = false
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSubmitting) {
                            Text(
                                text = "...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ========== 照片大图查看弹窗 ==========
    if (viewingPhotoPath != null && existingRecord != null) {
        val photoPaths = existingRecord.photoPaths
        var currentIdx by remember(viewingPhotoPath) {
            mutableIntStateOf(photoPaths.indexOf(viewingPhotoPath).coerceAtLeast(0))
        }

        Dialog(
            onDismissRequest = { viewingPhotoPath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewingPhotoPath = null }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 图片显示区域，支持左右滑动切换图片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { /* 拦截点击，防止收起 */ }
                        )
                        .pointerInput(currentIdx, photoPaths.size) {
                            if (photoPaths.size <= 1) return@pointerInput
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { totalDrag = 0f },
                                onDragEnd = {
                                    if (totalDrag > 100 && currentIdx > 0) {
                                        currentIdx--
                                    } else if (totalDrag < -100 && currentIdx < photoPaths.size - 1) {
                                        currentIdx++
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDrag += dragAmount
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(photoPaths[currentIdx]))
                            .crossfade(true)
                            .build(),
                        contentDescription = "照片大图",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                // 关闭按钮
                Text(
                    text = "✕",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewingPhotoPath = null }
                        )
                )

                // 图片页码指示器（多图时显示）
                if (photoPaths.size > 1) {
                    Text(
                        text = "${currentIdx + 1} / ${photoPaths.size}",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ========== 打卡输入区域组件 ==========

/**
 * 打卡输入区域 — 包含文字输入、标签选择、照片添加
 *
 * @param noteText 当前备注文字
 * @param onNoteChange 备注文字变化回调
 * @param selectedTags 已选标签列表
 * @param onTagToggle 标签切换回调
 * @param isSubmitting 是否正在提交中
 * @param selectedPhotoPaths 已选照片路径列表
 * @param onAddPhoto 添加照片回调
 * @param onRemovePhoto 移除照片回调
 * @param maxPhotoCount 最大照片数量
 */
@Composable
private fun NoteInputSection(
    noteText: String,
    onNoteChange: (String) -> Unit,
    selectedTags: List<String>,
    onTagToggle: (String) -> Unit,
    isSubmitting: Boolean,
    selectedPhotoPaths: List<String>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    maxPhotoCount: Int = 9
) {
    Column {
        // 文字输入框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                BasicTextField(
                    value = noteText,
                    onValueChange = onNoteChange,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF1F2937)
                    ),
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            // 空状态占位提示
                            if (noteText.isEmpty()) {
                                Text(
                                    text = "记录此行心情、景点、美食、见闻...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(Modifier.height(4.dp))
                // 字数计数，接近上限时变为红色警告
                Text(
                    text = "${noteText.length}/$MAX_NOTE_LENGTH",
                    fontSize = 10.sp,
                    color = if (noteText.length > MAX_NOTE_LENGTH - 20) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 预设标签列表（可横向滚动）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PRESET_TAGS.forEach { tag ->
                val isSelected = tag in selectedTags
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFFFFFFFF) else Color(0xFFF3F4F6)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF3B82F6) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isSubmitting,
                            onClick = { onTagToggle(tag) }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF575757)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 照片选择区域（可横向滚动）
        if (selectedPhotoPaths.isNotEmpty() || selectedPhotoPaths.size < maxPhotoCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 添加照片按钮
                if (selectedPhotoPaths.size < maxPhotoCount) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !isSubmitting,
                                onClick = onAddPhoto
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "${selectedPhotoPaths.size}/$maxPhotoCount",
                                fontSize = 9.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // 已选照片缩略图列表
                selectedPhotoPaths.forEach { path ->
                    Box(modifier = Modifier.size(60.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(path))
                                .crossfade(true)
                                .size(120)
                                .build(),
                            contentDescription = "照片预览",
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // 删除照片按钮
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.9f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    enabled = !isSubmitting,
                                    onClick = { onRemovePhoto(path) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
