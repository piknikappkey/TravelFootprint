package com.example.travel_footprint_android.presentation2.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.viewmodel.LightenViewModel
import com.example.travel_footprint_android.presentation2.components.light_panel2.LightPanel2
import com.example.travel_footprint_android.presentation2.components.svg_map.SVGMap

/**
 * 包装器组件：在不修改原 LightenScreen2 的情况下添加城市点亮功能
 */
@Composable
fun LightenScreenWrapper(
    lightenViewModel: LightenViewModel = hiltViewModel()
) {
    // 使用原来的 UI 状态
    val uiState by lightenViewModel.uiState.collectAsState()
    val lightedProvinces = uiState.lightedProvinces

    // 添加模式切换状态
    var isCityMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 原来的地图和面板（复制自 LightenScreen2）
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                // 使用原来的 SVGMap，但包装 onLightCityClick
                SVGMap(
                    modifier = Modifier.fillMaxSize(),
                    onLightCityClick = { adcode, name ->
                        if (isCityMode) {
                            // 城市模式：点亮城市
                            lightenViewModel.lightCityByAdcode(
                                cityAdcode = adcode,
                                cityName = name
                            )
                        } else {
                            // 省份模式：点亮省份（原有逻辑）
                            lightenViewModel.lightProvince(
                                provinceAdcode = adcode,
                                provinceName = name,
                                remark = "从地图选择点亮"
                            )
                        }
                    },
                    lightedProvinces = lightedProvinces
                )

                // 添加切换按钮（不修改原代码）
                ModeSwitchButton(
                    isCityMode = isCityMode,
                    onToggle = { isCityMode = !isCityMode },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }

            // 原来的 LightPanel2
            LightPanel2(
                modifier = Modifier,
                lightenCityMode = if (isCityMode) LightenCityMode.CITY else LightenCityMode.PROVINCE,
                lightenViewModel = lightenViewModel
            )
        }
    }
}

@Composable
private fun ModeSwitchButton(
    isCityMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (isCityMode) Color(0xFF2196F3) else Color(0xFF4CAF50),
        shadowElevation = 4.dp,
        onClick = onToggle
    ) {
        Text(
            text = if (isCityMode) "🌆 城市模式" else "🏔 省份模式",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}


enum class LightenCityMode {
    CITY,
    PROVINCE
}