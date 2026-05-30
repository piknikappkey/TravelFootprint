package com.example.travel_footprint_android.presentation2.components.button.button_draggable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.MainColor3
import kotlin.math.roundToInt

@Composable
fun ButtonDraggable(
    modifier: Modifier = Modifier,
    bgColor: Color = MainColor3,
    onClick: () -> Unit = {},
    showRainDialog: Boolean,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var outerWidth by remember { mutableFloatStateOf(0f) }
    var outerHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    val buttonSizePx = with(density) { 48.dp.toPx() }

    val aniAngle by animateFloatAsState(
        targetValue = if (showRainDialog) 360f else 0f,
        animationSpec = tween(durationMillis = 300),
    )

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                if (outerWidth == 0f && outerHeight == 0f) {
                    val paddingPx = with(density) { 10.dp.toPx() }
                    outerWidth = size.width.toFloat()
                    outerHeight = size.height.toFloat()
                    offsetX = outerWidth - buttonSizePx - paddingPx
                    offsetY = paddingPx + 60
                }
            }
    ) {
        val shape = RoundedCornerShape(16.dp)

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(36.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = shape,
                    clip = false
                )
                .background(
                    color = bgColor,
                    shape = shape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .pointerInput(outerWidth, outerHeight) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        val maxX = outerWidth - buttonSizePx
                        val maxY = outerHeight - buttonSizePx
                        offsetX = offsetX.coerceIn(0f, maxX)
                        offsetY = offsetY.coerceIn(0f, maxY)
                    }
                }
                .rotate(aniAngle),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
