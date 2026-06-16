package com.example.travel_footprint_android.presentation.components.journey_map.weather

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
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark5

@Composable
fun WeatherCard(
    weatherViewModel: WeatherViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
    ), // Hilt 注入天气 ViewModel（Activity 级作用域）
    modifier: Modifier = Modifier,
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()

    if(!weatherState.showWeatherCard) return

    val live = weatherState.liveWeather

    // 按压状态，用于控制拖拽时的缩放和透明度动画
    var isPress by remember { mutableStateOf(false) }

    val aniAlpha by animateFloatAsState(
        targetValue = if (isPress) 1f else 0.9f,
        animationSpec = tween(200),
    )
    val aniScale by animateFloatAsState(
        targetValue = if (isPress) 1.1f else 1f,
        animationSpec = tween(200),
    )

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
                .background(Color(0xFFF8F8F8))
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
                            color = FontDark5
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Headline(
                            text = "${live.temperature}°",
                            fontSize = 20.sp
                        )
                        TextMedium(
                            text = live.weather,
                            fontSize = 13.sp,
                            color = FontDark5
                        )
                    }
                }
            } else if (weatherState.error != null) {
                // 天气请求失败：显示错误图标 + 错误信息 + 重试按钮
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = FontDark5,
                    modifier = Modifier.width(24.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextMedium(
                        text = weatherState.error!!,
                        fontSize = 12.sp,
                        color = FontDark5
                    )
                    TextMedium(
                        text = "点击重试",
                        fontSize = 11.sp,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.clickable {
                            weatherViewModel.loadWeatherForCurrentLocation()
                        }
                    )
                }
            } else {
                TextMedium(
                    text = "加载天气...",
                    fontSize = 12.sp,
                    color = FontDark5
                )
            }
        }
    }
}

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
