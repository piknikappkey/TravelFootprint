package com.example.travel_footprint_android.presentation.components.lighten

import androidx.activity.ComponentActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.bg_box.DraggableBox
import com.example.travel_footprint_android.presentation.components.journey_map.weather.WeatherViewModel
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

// 点亮页面专用的蓝色系颜色定义
private val LightenCardBackground = Color.White// 蓝色半透明背景
private val LightenCardBackgroundPressed = Color(0xE62196F3) // 蓝色按压状态
private val LightenTextPrimary = Color.Black // 白色主文字
private val LightenTextSecondary = Color.Black // 白色次要文字
private val LightenErrorText = Color(0xFFFFCDD2) // 浅红色错误文字
private val LightenRetryText = Color(0xFFB3E5FC) // 浅蓝色重试文字

/**
 * 点亮页面专用天气卡片组件
 *
 * 使用蓝色系半透明背景，与点亮页面的蓝色水彩风格相匹配。
 * 功能与原 WeatherCard 相同，但视觉风格更适合点亮页面。
 */
@Composable
fun LightenWeatherCard(
    weatherViewModel: WeatherViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
    ), // Hilt 注入天气 ViewModel（Activity 级作用域）
    modifier: Modifier = Modifier,
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()

    if (!weatherState.showWeatherCard) return

    val live = weatherState.liveWeather

    // 按压状态，用于控制拖拽时的缩放和透明度动画
    var isPress by remember { mutableStateOf(false) }

    val aniAlpha by animateFloatAsState(
        targetValue = if (isPress) 1f else 0.9f,
        animationSpec = tween(200),
    )
    val aniScale by animateFloatAsState(
        targetValue = if (isPress) 1.05f else 1f,
        animationSpec = tween(200),
    )

    // 圆角形状
    val cardShape = RoundedCornerShape(12.dp)

    DraggableBox(
        modifier = modifier,
        initialOffsetX = 10f,
        initialOffsetY = 10f,
        onDragStart = { isPress = true },
        onDragEnd = { _, _ -> isPress = false },
        onDragCancel = { isPress = false },
    ) {
        Row(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = aniScale
                    scaleY = aniScale
                    alpha = aniAlpha
                }
                .clip(cardShape)
                .background(
                    color = if (isPress) LightenCardBackgroundPressed else LightenCardBackground,
                    shape = cardShape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (live != null) {
                Image(
                    painter = painterResource(getWeatherImg(live.weather)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(32.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (weatherState.cityName != null) {
                        TextMedium(
                            text = weatherState.cityName!!,
                            fontSize = 12.sp,
                            color = LightenTextSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Headline(
                            text = "${live.temperature}°",
                            fontSize = 20.sp,
                            color = LightenTextPrimary
                        )
                        TextMedium(
                            text = live.weather,
                            fontSize = 13.sp,
                            color = LightenTextSecondary
                        )
                    }
                }
            } else if (weatherState.error != null) {
                // 天气请求失败：显示错误图标 + 错误信息 + 重试按钮
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = LightenTextSecondary,
                    modifier = Modifier.width(24.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextMedium(
                        text = weatherState.error!!,
                        fontSize = 12.sp,
                        color = LightenErrorText
                    )
                    TextMedium(
                        text = "点击重试",
                        fontSize = 11.sp,
                        color = LightenRetryText,
                        modifier = Modifier.clickable {
                            weatherViewModel.loadWeatherForCurrentLocation()
                        }
                    )
                }
            } else {
                TextMedium(
                    text = "加载天气...",
                    fontSize = 12.sp,
                    color = LightenTextSecondary
                )
            }
        }
    }
}

/**
 * 根据天气描述获取对应的天气图标资源 ID
 */
private fun getWeatherImg(weather: String): Int {
    return when {
        weather.contains("晴") -> R.drawable.ic_fair
        weather.contains("云") -> R.drawable.ic_cloudy
        weather.contains("阴") -> R.drawable.ic_overcast
        weather.contains("雨") -> R.drawable.ic_rainy
        weather.contains("雪") -> R.drawable.ic_snowy
        weather.contains("雾") || weather.contains("霾") -> R.drawable.ic_foggy
        weather.contains("风") -> R.drawable.ic_windy
        weather.contains("雷") -> R.drawable.ic_thundery
        weather.contains("雹") -> R.drawable.ic_hailstorm
        else -> R.drawable.ic_fair
    }
}
