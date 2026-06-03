/**
 * 可拖拽的浮动定位按钮组件
 * 
 * 用途：
 * - 在地图面板上提供一个可自由拖拽的定位按钮，用于触发当前位置定位服务
 * - 用户可将按钮拖拽到容器内任意位置，避免遮挡地图关键区域
 * 
 * 功能：
 * - 点击按钮触发定位：调用 JourneyMap3ViewModel.startLocation() 启动高德定位服务
 * - 拖拽手势支持：通过 DraggableBox 封装实现自由拖拽，拖动范围受容器边界约束
 * - 初始位置自动设置：首次加载时按钮自动定位到容器右下角
 * - 容器尺寸自适应：当容器尺寸变化时，自动校准按钮位置不超出边界
 * 
 * 关联组件：
 * - DraggableBox: bg_box 包下的通用可拖动容器组件
 * - JourneyMapViewModel: 地图与定位 ViewModel（通过 Hilt 注入），提供 startLocation(aniMoveTime) 方法
 */
package com.example.travel_footprint_android.presentation.components.journey_map.location_button

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.bg_box.DraggableBox
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel

// 可拖拽定位按钮：FAB 风格按钮，支持拖拽移动和点击定位
@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    aniMoveTime: Long = 1500, // 定位时相机移动动画时长（毫秒）
    journeyMapViewModel: JourneyMapViewModel = hiltViewModel(key = "JourneyMap3"), // Hilt 注入地图 ViewModel
) {
    val density = LocalDensity.current
    // 按钮尺寸（50dp）转换为像素值，用于边界计算
    val buttonSizePx = with(density) { 50.dp.toPx() }

    // 记录父容器首次测量到的尺寸，用于计算右下角初始位置
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }

    DraggableBox(
        modifier = modifier,
        initialOffsetX = (containerWidth - buttonSizePx).coerceAtLeast(0f),
        initialOffsetY = (containerHeight - buttonSizePx).coerceAtLeast(0f),
        shape = null, // FAB 自带形状，不需要额外裁剪
        onOuterSizeChanged = { w, h ->
            // 仅在首次获取到容器尺寸时记录，后续尺寸变化由 DraggableBox 内部自动校准
            if (containerWidth == 0f) {
                containerWidth = w
                containerHeight = h
            }
        },
    ) {
        // 浮动操作按钮：点击触发定位，支持拖拽移动
        FloatingActionButton(
            onClick = {
                Log.d("JourneyPanel2", "LocationButton clicked, requesting new location with aniMoveTime=$aniMoveTime")
                journeyMapViewModel.startLocation(aniMoveTime) // 启动定位服务
            },
            modifier = Modifier.size(50.dp),
        ) {
            // 定位图标
            Icon(
                modifier = Modifier.padding(1.dp),
                imageVector = Icons.Default.MyLocation,
                contentDescription = "定位到当前位置",
            )
        }
    }
}
