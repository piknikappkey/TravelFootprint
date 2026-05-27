package com.example.travel_footprint_android.presentation2.components.light_panel2.corner

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2Tab

@Composable
fun CornerContent(
    lightedProvinceCount: Int,
    lightCityList: List<LightedCity>
) {
    val totalProvinces = LightPanel2Tab.TOTAL_PROVINCE_COUNT
    val coveragePercent = if (totalProvinces > 0) {
        (lightedProvinceCount.toFloat() / totalProvinces * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "省份点亮进度",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Spacer(Modifier.height(16.dp))

        ProgressRing(
            percent = coveragePercent,
            lightedCount = lightedProvinceCount,
            totalCount = totalProvinces
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "已点亮", value = "$lightedProvinceCount", color = Color(0xFF3B82F6))
            StatItem(label = "未点亮", value = "${totalProvinces - lightedProvinceCount}", color = Color(0xFFD1D5DB))
            StatItem(label = "完成度", value = "$coveragePercent%", color = Color(0xFF10B981))
        }

        if (lightCityList.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFE5E7EB))
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "省份点亮详情",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            val provinceCityData = remember(lightCityList) {
                lightCityList
                    .groupBy { it.provinceAdcode }
                    .map { (_, cities) ->
                        val totalCitiesInProvince = cities.size
                        ProvinceCityProgress(
                            provinceName = cities.first().provinceName,
                            cityCount = totalCitiesInProvince
                        )
                    }
                    .sortedByDescending { it.cityCount }
            }

            provinceCityData.forEach { data ->
                ProvinceCityProgressRow(data = data)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private data class ProvinceCityProgress(
    val provinceName: String,
    val cityCount: Int
)

@Composable
private fun ProvinceCityProgressRow(data: ProvinceCityProgress) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF9FAFB))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.provinceName.take(1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = data.provinceName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "已点亮 ${data.cityCount} 个城市",
            fontSize = 12.sp,
            color = Color(0xFF3B82F6),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProgressRing(
    percent: Int,
    lightedCount: Int,
    totalCount: Int
) {
    Box(
        modifier = Modifier
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6))
                .fillMaxWidth()
                .height(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3B82F6))
                    .fillMaxWidth(percent / 100f)
                    .height(20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$percent%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (percent > 30) Color.White else Color(0xFF6B7280)
                )
                Text(
                    text = "$lightedCount/$totalCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (percent > 30) Color.White else Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}