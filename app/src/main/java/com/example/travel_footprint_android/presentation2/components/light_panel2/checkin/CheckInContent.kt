package com.example.travel_footprint_android.presentation2.components.light_panel2.checkin

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

data class CheckInRecord(
    val cityAdcode: String,
    val cityName: String,
    val note: String,
    val time: Date,
    val tags: List<String> = emptyList(),
    val photoPaths: List<String> = emptyList()
)

private enum class CheckInFilter(val label: String) {
    ALL("全部"),
    CHECKED_IN("已打卡"),
    UNCHECKED("未打卡")
}

private val PRESET_TAGS = listOf("#美食", "#风景", "#出差", "#旅游", "#路过", "#工作")

private const val MAX_NOTE_LENGTH = 150

@Composable
fun CheckInContent(
    lightCityList: List<LightedCity>,
    checkInRecords: List<CheckInRecord>,
    currentCityAdcode: String? = null,
    onAddCheckIn: (String, String, String) -> Unit,
    onAddCheckInRich: ((String, String, String, List<String>) -> Unit)? = null,
    onCityClick: ((String) -> Unit)? = null
) {
    var activeFilter by remember { mutableStateOf(CheckInFilter.ALL) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var highlightedCity by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(2000)
            successMessage = null
        }
    }

    LaunchedEffect(highlightedCity) {
        if (highlightedCity != null) {
            delay(1000)
            highlightedCity = null
        }
    }

    val filteredGrouped = remember(activeFilter, groupedCities, checkInRecords) {
        groupedCities.mapValues { (_, cities) ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredGrouped.forEach { (provinceName, cities) ->
                        item(key = "header_$provinceName") {
                            ProvinceGroupHeader(
                                provinceName = provinceName,
                                cityCount = cities.size,
                                checkedCount = cities.count { c ->
                                    checkInRecords.any { it.cityAdcode == c.cityAdcode }
                                }
                            )
                        }
                        items(cities, key = { it.cityAdcode }) { city ->
                            CheckInCityItem(
                                city = city,
                                existingRecord = checkInRecords.find { it.cityAdcode == city.cityAdcode },
                                isHighlighted = highlightedCity == city.cityAdcode,
                                isCurrentLocation = city.cityAdcode == currentCityAdcode,
                                onCheckIn = { note, tags ->
                                    if (onAddCheckInRich != null) {
                                        onAddCheckInRich(city.cityAdcode, city.cityName, note, tags)
                                    } else {
                                        onAddCheckIn(city.cityAdcode, city.cityName, note)
                                    }
                                    highlightedCity = city.cityAdcode
                                    successMessage = "${city.cityName} 打卡成功"
                                },
                                onClick = {
                                    val record = checkInRecords.find { it.cityAdcode == city.cityAdcode }
                                    if (record != null) {
                                        onCityClick?.invoke(city.cityAdcode)
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }

        if (successMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF065F46).copy(alpha = 0.95f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✅", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = successMessage ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

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
        Text(
            text = "$checkedCount/$cityCount",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CheckInCityItem(
    city: LightedCity,
    existingRecord: CheckInRecord?,
    isHighlighted: Boolean,
    isCurrentLocation: Boolean,
    onCheckIn: (String, List<String>) -> Unit,
    onClick: () -> Unit
) {
    var showInput by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    val submitScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val bgColor = when {
        isHighlighted -> Color(0xFFDCFCE7)
        isCurrentLocation -> Color(0xFFEFF6FF)
        existingRecord != null -> Color(0xFFF0FDF4)
        else -> Color(0xFFF9FAFB)
    }

    val borderColor = when {
        isHighlighted -> Color(0xFF22C55E)
        isCurrentLocation -> Color(0xFFBFDBFE)
        existingRecord != null -> Color(0xFFBBF7D0)
        else -> Color(0xFFE5E7EB)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (existingRecord != null) Color(0xFF22C55E)
                                else Color(0xFFD1D5DB)
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = city.cityName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (existingRecord != null) Color(0xFF1F2937) else Color(0xFF6B7280)
                    )
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
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已打卡",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (!showInput) {
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

            if (existingRecord != null) {
                Spacer(Modifier.height(6.dp))
                if (existingRecord.note.isNotBlank()) {
                    Text(
                        text = existingRecord.note,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (existingRecord.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        existingRecord.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }
                }
                if (existingRecord.photoPaths.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "📷 ${existingRecord.photoPaths.size}张照片",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                Spacer(Modifier.height(6.dp))
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
                    isSubmitting = isSubmitting
                )
                Spacer(Modifier.height(8.dp))
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
                                else if (noteText.isNotBlank() || selectedTags.isNotEmpty()) Color(0xFF3B82F6)
                                else Color(0xFFD1D5DB)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if ((noteText.isNotBlank() || selectedTags.isNotEmpty()) && !isSubmitting) {
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
                                            onCheckIn(noteText.trim(), selectedTags.toList())
                                            isSubmitting = false
                                            noteText = ""
                                            selectedTags.clear()
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
                    isSubmitting = isSubmitting
                )
                Spacer(Modifier.height(8.dp))
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
                                        onCheckIn(noteText.trim(), selectedTags.toList())
                                        isSubmitting = false
                                        noteText = ""
                                        selectedTags.clear()
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
}

@Composable
private fun NoteInputSection(
    noteText: String,
    onNoteChange: (String) -> Unit,
    selectedTags: List<String>,
    onTagToggle: (String) -> Unit,
    isSubmitting: Boolean
) {
    Column {
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
                Text(
                    text = "${noteText.length}/$MAX_NOTE_LENGTH",
                    fontSize = 10.sp,
                    color = if (noteText.length > MAX_NOTE_LENGTH - 20) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

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
                            if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF3F4F6)
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
                        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

