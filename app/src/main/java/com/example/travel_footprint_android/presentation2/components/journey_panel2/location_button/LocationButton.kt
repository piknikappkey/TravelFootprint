package com.example.travel_footprint_android.presentation2.components.journey_panel2.location_button

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
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel
import kotlin.math.roundToInt

@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    aniMoveTime: Long = 1500,
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(key = "JourneyMap3"),
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var outerWidth by remember { mutableFloatStateOf(0f) }
    var outerHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val buttonSizePx = with(density) { 50.dp.toPx() }
    val paddingPx = with(density) { 16.dp.toPx() }

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                outerWidth = size.width.toFloat()
                outerHeight = size.height.toFloat()
            }
    ) {
        FloatingActionButton(
            onClick = {
                Log.d("JourneyPanel2", "LocationButton clicked, requesting new location with aniMoveTime=$aniMoveTime")
                journeyMap3ViewModel.startLocation(aniMoveTime)
            },
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        val maxX = outerWidth - buttonSizePx
                        val maxY = outerHeight - buttonSizePx - 250
                        offsetX = offsetX.coerceIn(0f, maxX)
                        offsetY = offsetY.coerceIn(0f, maxY)
                    }
                }
        ) {
            Icon(
                modifier = Modifier.padding(1.dp),
                imageVector = Icons.Default.MyLocation,
                contentDescription = "定位到当前位置"
            )
        }
    }

    LaunchedEffect(outerWidth, outerHeight) {
        if (outerWidth > 0f && outerHeight > 0f) {
            val maxX = outerWidth - buttonSizePx - paddingPx
            val maxY = outerHeight - buttonSizePx - paddingPx - 250
            if (offsetX == 0f && offsetY == 0f) {
                offsetX = maxX
                offsetY = maxY
            } else {
                offsetX = offsetX.coerceAtMost(maxX)
                offsetY = offsetY.coerceAtMost(maxY)
            }
        }
    }
}