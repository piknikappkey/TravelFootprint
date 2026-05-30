package com.example.travel_footprint_android.presentation2.components.light_panel2.corner

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2Tab
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private enum class ProvinceRegion(val label: String, val adcodePrefixes: List<String>) {
    ALL("全部", emptyList()),
    NORTH("华北", listOf("11", "12", "13", "14", "15")),
    EAST("华东", listOf("31", "32", "33", "34", "35", "36", "37")),
    SOUTH("华南", listOf("41", "42", "43", "44", "45", "46")),
    SOUTHWEST("西南", listOf("50", "51", "52", "53", "54")),
    NORTHWEST("西北", listOf("61", "62", "63", "64", "65")),
    NORTHEAST("东北", listOf("21", "22", "23")),
    HKMT("港澳台", listOf("71", "81", "82"))
}

data class ProvinceDetail(
    val provinceName: String,
    val provinceAdcode: String,
    val cityCount: Int,
    val cityNames: List<String>,
    val isLighted: Boolean
)

private data class BucketListItem(
    val name: String,
    val priority: Int,
    val isProvince: Boolean
)

private const val EARTH_RADIUS_KM = 6371.0


@Composable
fun CornerContent(
    lightedProvinceCount: Int,
    lightCityList: List<LightedCity>,
    onViewFootprint: ((LightedCity) -> Unit)? = null,
    onGoCheckIn: ((provinceAdcode: String) -> Unit)? = null,
    onExportFootprint: (() -> Unit)? = null
) {
    val totalProvinces = LightPanel2Tab.TOTAL_PROVINCE_COUNT
    val coveragePercent = if (totalProvinces > 0) {
        (lightedProvinceCount.toFloat() / totalProvinces * 100).toInt()
    } else 0
    val remainingProvinces = totalProvinces - lightedProvinceCount

    val totalCities = lightCityList.size

    val totalMileage = remember(lightCityList) {
        calculateTotalMileage(lightCityList)
    }

    val provincesData = remember(lightCityList) {
        lightCityList
            .groupBy { it.provinceAdcode }
            .map { (_, cities) ->
                ProvinceDetail(
                    provinceName = cities.first().provinceName,
                    provinceAdcode = cities.first().provinceAdcode,
                    cityCount = cities.size,
                    cityNames = cities.map { it.cityName },
                    isLighted = true
                )
            }
            .sortedByDescending { it.cityCount }
    }

    val allChineseProvinces = remember {
        getAllChineseProvinces()
    }

    val allProvincesData = remember(provincesData, allChineseProvinces) {
        val lightedAdcodes = provincesData.map { it.provinceAdcode }.toSet()
        val unlightedProvinces = allChineseProvinces
            .filter { it.adcode !in lightedAdcodes }
            .map { (name, adcode) ->
                ProvinceDetail(
                    provinceName = name,
                    provinceAdcode = adcode,
                    cityCount = 0,
                    cityNames = emptyList(),
                    isLighted = false
                )
            }
        provincesData + unlightedProvinces
    }

    var selectedRegion by remember { mutableStateOf(ProvinceRegion.ALL) }
    var sortByCount by remember { mutableStateOf(true) }
    var showMapView by remember { mutableStateOf(false) }

    val filteredProvinces = remember(selectedRegion, sortByCount, allProvincesData) {
        val filtered = if (selectedRegion == ProvinceRegion.ALL) {
            allProvincesData
        } else {
            allProvincesData.filter { province ->
                selectedRegion.adcodePrefixes.any { prefix ->
                    province.provinceAdcode.startsWith(prefix)
                }
            }
        }
        if (sortByCount) {
            filtered.sortedByDescending { it.cityCount }
        } else {
            filtered.sortedBy { it.provinceAdcode }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Module1TopDashboard(
            lightedProvinceCount = lightedProvinceCount,
            totalProvinces = totalProvinces,
            coveragePercent = coveragePercent,
            remainingProvinces = remainingProvinces,
            totalCities = totalCities,
            totalMileage = totalMileage
        )

        Spacer(Modifier.height(20.dp))

        Module2ProvinceDetails(
            provincesData = filteredProvinces,
            selectedRegion = selectedRegion,
            onRegionSelected = { selectedRegion = it },
            sortByCount = sortByCount,
            onSortToggle = { sortByCount = !sortByCount },
            onViewFootprint = onViewFootprint,
            onGoCheckIn = onGoCheckIn
        )

        Spacer(Modifier.height(20.dp))

        Module3TravelCorner(lightCityList = lightCityList)

        Spacer(Modifier.height(20.dp))

        Module4BottomActions(
            showMapView = showMapView,
            onToggleView = { showMapView = !showMapView },
            onExport = onExportFootprint
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Module1TopDashboard(
    lightedProvinceCount: Int,
    totalProvinces: Int,
    coveragePercent: Int,
    remainingProvinces: Int,
    totalCities: Int,
    totalMileage: Int
) {
    Column {
        Text(
            text = "旅行数据概览",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                value = "$lightedProvinceCount",
                valueUnit = "个",
                subtitle = "覆盖全国${coveragePercent}%版图",
                label = "已点亮省份",
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                value = "$remainingProvinces",
                valueUnit = "个",
                subtitle = if (remainingProvinces > 17) {
            "继续加油"
        } else if(0<remainingProvinces&&remainingProvinces<=17){
            "即将全部点亮"
        } else {
            "已全部点亮！"
                },
                label = "待解锁省份",
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                value = "$totalCities",
                valueUnit = "个",
                subtitle = "累计旅行${totalMileage}km",
                label = "打卡城市数",
            )
        }

        Spacer(Modifier.height(16.dp))

        GradientProgressBar(
            percent = coveragePercent,
            lightedCount = lightedProvinceCount,
            totalCount = totalProvinces
        )
    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    value: String,
    valueUnit: String,
    subtitle: String,
    label: String,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                 colors =  listOf(
                     Color(0xFF3B82F6),
                     Color(0xFF3B82F6)
                 )
                )
            )
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = valueUnit,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GradientProgressBar(
    percent: Int,
    lightedCount: Int,
    totalCount: Int
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percent) {
        animatedProgress.snapTo(0f)
        delay(100)
        animatedProgress.animateTo(
            targetValue = percent / 100f,
            animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .height(28.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth(animatedProgress.value)
                .height(28.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF60A5FA), // 柔和蓝
                            Color(0xFF93C5FD)  // 浅蓝
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✈",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "已点亮 $lightedCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (percent > 30) Color.White else Color(0xFF4B5563)
                )
            }
            Text(
                text = "剩余 ${totalCount - lightedCount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (percent > 50) Color.White.copy(alpha = 0.85f) else Color(0xFF6B7280)
            )
        }

        if (animatedProgress.value > 0.3f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (animatedProgress.value * 280 - 10).dp.coerceIn(10.dp, 260.dp))
            ) {
                Text(
                    text = "✈",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun Module2ProvinceDetails(
    provincesData: List<ProvinceDetail>,
    selectedRegion: ProvinceRegion,
    onRegionSelected: (ProvinceRegion) -> Unit,
    sortByCount: Boolean,
    onSortToggle: () -> Unit,
    onViewFootprint: ((LightedCity) -> Unit)?,
    onGoCheckIn: ((String) -> Unit)?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "省份点亮详情",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSortToggle
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "排序",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (sortByCount) "按数量" else "按地区",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProvinceRegion.values().forEach { region ->
                RegionTag(
                    label = region.label,
                    isSelected = region == selectedRegion,
                    onClick = { onRegionSelected(region) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        provincesData.forEach { data ->
            ProvinceCard(
                data = data,
                onViewFootprint = onViewFootprint,
                onGoCheckIn = onGoCheckIn
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RegionTag(
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
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun ProvinceCard(
    data: ProvinceDetail,
    onViewFootprint: ((LightedCity) -> Unit)?,
    onGoCheckIn: ((String) -> Unit)?
) {
    var isPressed by remember { mutableStateOf(false) }

    val scaleValue = if (isPressed) 0.97f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleValue)
            .clip(RoundedCornerShape(12.dp))
            .background(if (data.isLighted) Color(0xFFF0F7FF) else Color(0xFFF9FAFB))
            .border(
                width = 1.dp,
                color = if (data.isLighted) Color(0xFFBFDBFE) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (data.isLighted) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFD1D5DB), Color(0xFFD1D5DB))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.provinceName.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (data.isLighted) Color.White else Color(0xFF9CA3AF)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = data.provinceName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (data.isLighted) Color(0xFF1F2937) else Color(0xFF9CA3AF)
                    )
                    if (!data.isLighted) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "待解锁",
                                fontSize = 10.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                if (data.isLighted && data.cityNames.isNotEmpty()) {
                    Text(
                        text = "已点亮${data.cityCount}个城市",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3B82F6)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = data.cityNames.take(3).joinToString("、"),
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "暂无已点亮城市",
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                if (data.isLighted) {
                    ActionButton(
                        text = "查看足迹",
                        icon = Icons.Default.Map,
                        onClick = { onViewFootprint?.let { /* navigate */ } },
                        compact = true
                    )
                    Spacer(Modifier.height(4.dp))
                }
                ActionButton(
                    text = if (data.isLighted) "去打卡" else "去打卡",
                    icon = Icons.Default.LocationOn,
                    onClick = { onGoCheckIn?.invoke(data.provinceAdcode) },
                    compact = true,
                    isSecondary = !data.isLighted
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    compact: Boolean = false,
    isSecondary: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSecondary) Color(0xFFF3F4F6)
                else Color(0xFFEFF6FF)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSecondary) Color(0xFF6B7280) else Color(0xFF3B82F6),
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSecondary) Color(0xFF6B7280) else Color(0xFF3B82F6)
        )
    }
}

@Composable
private fun Module3TravelCorner(lightCityList: List<LightedCity>) {
    Column {
        Text(
            text = "我的旅行角落",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "足迹墙",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4B5563)
        )
        Spacer(Modifier.height(8.dp))

        if (lightCityList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有点亮任何城市，开始你的旅行吧！",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lightCityList.take(10).forEach { city ->
                    FootprintCard(city = city)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FootprintCard(city: LightedCity) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE),
                        Color(0xFFF0F9FF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(600f, 600f)
                )
            )
            .border(
                1.dp, Color(0xFFBAE6FD).copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = city.cityName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(city.lightedTime),
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
            if (city.remark.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = city.remark,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BucketListSection() {
    val bucketList = remember { mutableStateListOf<BucketListItem>() }
    var showInput by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    Column {
        if (bucketList.isEmpty() && !showInput) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showInput = true }
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "添加你想去的城市或省份",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        } else {
            bucketList.forEachIndexed { index, item ->
                BucketListRow(
                    item = item,
                    onRemove = { bucketList.removeAt(index) }
                )
                Spacer(Modifier.height(6.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showInput = true }
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
            }
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
                        value = inputText,
                        onValueChange = { if (it.length <= 20) inputText = it },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF1F2937)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "输入省份或城市名称...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                inner()
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
                                if (inputText.isNotBlank()) {
                                    bucketList.add(
                                        BucketListItem(
                                            name = inputText.trim(),
                                            priority = bucketList.size + 1,
                                            isProvince = false
                                        )
                                    )
                                    inputText = ""
                                    showInput = false
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BucketListRow(
    item: BucketListItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF0F7FF))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isProvince) Icons.Default.Terrain else Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
            Text(
                text = if (item.isProvince) "省份" else "城市",
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            (1..3).forEach { starLevel ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "优先级$starLevel",
                    tint = if (starLevel <= item.priority) Color(0xFFF59E0B) else Color(0xFFE5E7EB),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRemove
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    fontSize = 10.sp,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Module4BottomActions(
    showMapView: Boolean,
    onToggleView: () -> Unit,
    onExport: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onExport?.invoke() }
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "导出",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "导出足迹",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleView
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showMapView) Icons.Default.GridView else Icons.Default.Map,
                    contentDescription = "切换视图",
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (showMapView) "列表视图" else "地图缩略",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
            }
        }
    }
}

private fun calculateTotalMileage(cities: List<LightedCity>): Int {
    if (cities.size < 2) return 0
    val sorted = cities.sortedBy { it.lightedTime }
    var total = 0.0
    for (i in 0 until sorted.size - 1) {
        total += haversineDistance(
            sorted[i].latitude, sorted[i].longitude,
            sorted[i + 1].latitude, sorted[i + 1].longitude
        )
    }
    return total.toInt()
}

private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
}

private data class ChineseProvince(
    val name: String,
    val adcode: String
)

private fun getAllChineseProvinces(): List<ChineseProvince> = listOf(
    ChineseProvince("北京市", "110000"),
    ChineseProvince("天津市", "120000"),
    ChineseProvince("河北省", "130000"),
    ChineseProvince("山西省", "140000"),
    ChineseProvince("内蒙古自治区", "150000"),
    ChineseProvince("辽宁省", "210000"),
    ChineseProvince("吉林省", "220000"),
    ChineseProvince("黑龙江省", "230000"),
    ChineseProvince("上海市", "310000"),
    ChineseProvince("江苏省", "320000"),
    ChineseProvince("浙江省", "330000"),
    ChineseProvince("安徽省", "340000"),
    ChineseProvince("福建省", "350000"),
    ChineseProvince("江西省", "360000"),
    ChineseProvince("山东省", "370000"),
    ChineseProvince("河南省", "410000"),
    ChineseProvince("湖北省", "420000"),
    ChineseProvince("湖南省", "430000"),
    ChineseProvince("广东省", "440000"),
    ChineseProvince("广西壮族自治区", "450000"),
    ChineseProvince("海南省", "460000"),
    ChineseProvince("重庆市", "500000"),
    ChineseProvince("四川省", "510000"),
    ChineseProvince("贵州省", "520000"),
    ChineseProvince("云南省", "530000"),
    ChineseProvince("西藏自治区", "540000"),
    ChineseProvince("陕西省", "610000"),
    ChineseProvince("甘肃省", "620000"),
    ChineseProvince("青海省", "630000"),
    ChineseProvince("宁夏回族自治区", "640000"),
    ChineseProvince("新疆维吾尔自治区", "650000"),
    ChineseProvince("台湾省", "710000"),
    ChineseProvince("香港特别行政区", "810000"),
    ChineseProvince("澳门特别行政区", "820000")
)