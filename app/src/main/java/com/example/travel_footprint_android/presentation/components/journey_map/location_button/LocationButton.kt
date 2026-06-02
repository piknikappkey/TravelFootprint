/**
 * 可拖拽的浮动定位按钮组件
 * 
 * 用途：
 * - 在地图面板上提供一个可自由拖拽的定位按钮，用于触发当前位置定位服务
 * - 用户可将按钮拖拽到容器内任意位置（Y方向底部预留250px空间），避免遮挡地图关键区域
 * 
 * 功能：
 * - 点击按钮触发定位：调用 JourneyMap3ViewModel.startLocation() 启动高德定位服务
 * - 拖拽手势支持：通过 detectDragGestures 实现自由拖拽，拖动范围受容器边界约束
 * - 初始位置自动设置：首次加载时按钮自动定位到容器右下角
 * - 容器尺寸自适应：当容器尺寸变化时，自动校准按钮位置不超出边界
 * 
 * 关联组件：
 * - JourneyMap3ViewModel: 地图与定位 ViewModel（通过 Hilt 注入），提供 startLocation(aniMoveTime) 方法：
 *   该方法设置动画时长后调用 AMapLocationClient.startLocation() 触发单次高精度定位，
 *   定位结果回调中会以 animateCamera 平滑移动到当前位置
 * 
 * 实现逻辑：
 * - 使用 remember 管理偏移状态（offsetX, offsetY）和容器尺寸（outerWidth, outerHeight）
 * - Box 作为外层容器，通过 onSizeChanged 监听尺寸变化
 * - FloatingActionButton 使用 offset 修饰符根据偏移量定位，pointerInput + detectDragGestures 处理拖拽
 * - 拖拽边界约束：X 方向限制在 [0, 容器宽 - 按钮宽]，Y 方向额外减去 250px 预留底部空间
 * - LaunchedEffect 在容器尺寸首次确定时，将按钮初始位置设置到右下角（带 padding）
 */
package com.example.travel_footprint_android.presentation.components.journey_map.location_button

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.presentation.components.journey_map.viewmodel.JourneyMap3ViewModel
import kotlin.math.roundToInt

// 可拖拽定位按钮：FAB 风格按钮，支持拖拽移动和点击定位
@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    aniMoveTime: Long = 1500, // 定位时相机移动动画时长（毫秒）
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(key = "JourneyMap3"), // Hilt 注入地图 ViewModel
) {
    // 按钮偏移量（像素），拖拽时动态更新
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    // 外层容器宽高，通过 onSizeChanged 获取
    var outerWidth by remember { mutableFloatStateOf(0f) }
    var outerHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    // 按钮尺寸（50dp）和边距（16dp）转换为像素值，用于边界计算
    val buttonSizePx = with(density) { 50.dp.toPx() }
    val paddingPx = with(density) { 16.dp.toPx() }

    // 外层容器：监听尺寸变化，为拖拽边界计算提供依据
    Box(
        modifier = modifier
            .onSizeChanged { size ->
                outerWidth = size.width.toFloat()
                outerHeight = size.height.toFloat()
            }
    ) {
        // 浮动操作按钮：点击触发定位，支持拖拽移动
        FloatingActionButton(
            onClick = {
                Log.d("JourneyPanel2", "LocationButton clicked, requesting new location with aniMoveTime=$aniMoveTime")
                journeyMap3ViewModel.startLocation(aniMoveTime) // 启动定位服务
            },
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) } // 根据偏移量定位
                .size(50.dp)
                .pointerInput(Unit) {
                    // 拖拽手势检测：实时更新偏移量并约束在边界内
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        // 边界约束：X 方向不超过容器宽度，Y 方向预留 250px 底部空间
                        val maxX = outerWidth - buttonSizePx
                        val maxY = outerHeight - buttonSizePx - 250
                        offsetX = offsetX.coerceIn(0f, maxX)
                        offsetY = offsetY.coerceIn(0f, maxY)
                    }
                }
        ) {
            // 定位图标
            Icon(
                modifier = Modifier.padding(1.dp),
                imageVector = Icons.Default.MyLocation,
                contentDescription = "定位到当前位置"
            )
        }
    }

    // 容器尺寸变化时触发：首次获取尺寸时将按钮初始位置设到右下角
    LaunchedEffect(outerWidth, outerHeight) {
        if (outerWidth > 0f && outerHeight > 0f) {
            // 计算边界最大值（扣除按钮尺寸、边距和底部预留空间）
            val maxX = outerWidth - buttonSizePx - paddingPx
            val maxY = outerHeight - buttonSizePx - paddingPx - 250
            if (offsetX == 0f && offsetY == 0f) {
                // 首次初始化：定位到右下角
                offsetX = maxX
                offsetY = maxY
            } else {
                // 容器尺寸变化后：确保按钮不超出新的边界
                offsetX = offsetX.coerceAtMost(maxX)
                offsetY = offsetY.coerceAtMost(maxY)
            }
        }
    }
}