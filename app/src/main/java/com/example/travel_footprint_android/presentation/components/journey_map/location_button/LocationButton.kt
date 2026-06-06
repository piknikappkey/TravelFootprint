/**
 * 固定定位按钮组件（已移除拖拽功能）
 *
 * 用途：
 * - 在地图面板上提供一个固定位置的定位按钮，用于触发当前位置定位服务
 * - 位置由外部调用方通过 modifier 控制
 *
 * 功能：
 * - 点击按钮触发定位：调用 JourneyMapViewModel.startLocation() 启动高德定位服务
 *
 * 关联组件：
 * - JourneyMapViewModel: 地图与定位 ViewModel（通过 Hilt 注入），提供 startLocation(aniMoveTime) 方法
 */
package com.example.travel_footprint_android.presentation.components.journey_map.location_button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMapViewModel

// 固定定位按钮：FAB 风格按钮，点击触发定位
@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    aniMoveTime: Long = 1500, // 定位时相机移动动画时长（毫秒）
    screenWidthPx: Int = 0, // 屏幕宽度（像素），>0 时启用底部面板补偿定位
    screenHeightPx: Int = 0, // 屏幕高度（像素）
    panelTopY: Int = 0, // 面板顶部距屏幕顶部的 Y 偏移（像素），即可见区域底部边界
    journeyMapViewModel: JourneyMapViewModel = hiltViewModel(key = "JourneyMap3"), // Hilt 注入地图 ViewModel
) {
    // 浮动操作按钮：点击触发定位
    FloatingActionButton(
        onClick = {
            journeyMapViewModel.startLocation(aniMoveTime, screenWidthPx, screenHeightPx, panelTopY)
        },
        modifier = modifier.size(50.dp),
    ) {
        // 定位图标
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "定位到当前位置",
        )
    }
}
