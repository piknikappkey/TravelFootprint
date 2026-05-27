package com.example.travel_footprint_android.presentation2.components.light_panel2.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.LightedCity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CheckInRecord(
    val cityAdcode: String,
    val cityName: String,
    val note: String,
    val time: Date
)

@Composable
fun CheckInContent(
    lightCityList: List<LightedCity>,
    checkInRecords: List<CheckInRecord>,
    onAddCheckIn: (String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        if (lightCityList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lightCityList, key = { it.cityAdcode }) { city ->
                    CheckInCityItem(
                        city = city,
                        existingRecord = checkInRecords.find { it.cityAdcode == city.cityAdcode },
                        onCheckIn = { note ->
                            onAddCheckIn(city.cityAdcode, city.cityName, note)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInCityItem(
    city: LightedCity,
    existingRecord: CheckInRecord?,
    onCheckIn: (String) -> Unit
) {
    var showInput by remember { mutableStateOf(existingRecord == null) }
    var noteText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = city.cityName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }

                if (existingRecord != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已打卡",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "已打卡",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (existingRecord != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = existingRecord.note,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(existingRecord.time),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
                Spacer(Modifier.height(8.dp))
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

            if (showInput) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        BasicTextField(
                            value = noteText,
                            onValueChange = { if (it.length <= 50) noteText = it },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = Color(0xFF1F2937)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (noteText.isEmpty()) {
                                        Text(
                                            text = "添加打卡备注...",
                                            fontSize = 13.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (noteText.isNotBlank()) {
                                        onCheckIn(noteText.trim())
                                        showInput = false
                                        noteText = ""
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
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