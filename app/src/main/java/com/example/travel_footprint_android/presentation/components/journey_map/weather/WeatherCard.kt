package com.example.travel_footprint_android.presentation.components.journey_map.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun WeatherCard(
    weatherState: WeatherUiState,
    modifier: Modifier = Modifier,
) {
    val live = weatherState.liveWeather ?: return

    var offsetX by remember { mutableStateOf(10f) }
    var offsetY by remember { mutableStateOf(10f) }

    var isPress by remember { mutableStateOf(false) }

    val aniAloha by animateFloatAsState(
        targetValue = if(isPress) 1f else .85f,
        animationSpec = tween(300)
    )

    val aniScale by animateFloatAsState(
        targetValue = if(isPress) 1.2f else 1f,
        animationSpec = tween(300)
    )

    AnimatedVisibility(visible = weatherState.showWeatherCard) {
        Row(
            modifier = modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isPress = true },
                        onDragEnd = { isPress = false },
                        onDragCancel = { isPress = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = aniScale
                    scaleY = aniScale
                }
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = aniAloha))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = weatherIcon(live.weather),
                fontSize = 24.sp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (weatherState.cityName != null) {
                    Text(
                        text = weatherState.cityName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "${live.temperature}°",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = live.weather,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
