package com.example.travel_footprint_android.presentation2.components.journey_panel2

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_edit.FootprintEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list.FootprintList
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.JourneyEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.JourneyList3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.location_button.LocationButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.FOOTPRINT_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.FOOTPRINT_LIST
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_LIST
import com.example.travel_footprint_android.ui.theme.SecondColor2


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel7(
    modifier: Modifier = Modifier,
    journeyList: List<Journey>,
    aniTime: Int,
    journeyViewModel: JourneyViewModel = hiltViewModel(),
) {
    val journeyPanel2State = JourneyNavController.journeyNavController.value
    val journeySelected = JourneyNavController.journeyData.value
    val footprintSelected = JourneyNavController.footprintData.value

    val configuration = LocalConfiguration.current
    val density = configuration.densityDpi.toFloat() / 160f
    val screenHeightPixels = configuration.screenHeightDp * density

    var currentHeightRatio by remember { mutableFloatStateOf(0.4f) }
    var isDragging by remember { mutableStateOf(false) }

    val aniJourneyHeight = if (isDragging) {
        currentHeightRatio
    } else {
        animateFloatAsState(
            targetValue = currentHeightRatio,
            animationSpec = tween(durationMillis = 300),
            label = "journeyPanelHeight"
        ).value
    }

    val togglePanelHeight = { _: Boolean ->
        if (!isDragging) {
            currentHeightRatio = if (currentHeightRatio < 0.5f) 0.6f else 0.4f
        }
    }

    val onDragDelta = { deltaY: Float ->
        val ratioDelta = -deltaY / screenHeightPixels
        currentHeightRatio = (currentHeightRatio + ratioDelta).coerceIn(0.2f, 0.8f)
    }

    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                // 测量原本的内容
                val placeable = measurable.measure(constraints)
                val offsetPx = 60.dp.roundToPx()

                // 布局高度 = 原高度 - 偏移量（最小为 0）
                val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)

                layout(placeable.width, layoutHeight) {
                    // 把内容放到向上偏移的位置
                    placeable.placeRelative(0, -offsetPx)
                }
            }
    ) {
        LocationButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-70).dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                    clip = true
                )
        ) {
            BGImgBox(listOf(R.drawable.bg_rectangular_1__3__0)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(configuration.screenHeightDp.dp * aniJourneyHeight)
                ) {
                    // 控制面板高度组件
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = { isDragging = true },
                                    onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                                    onDragEnd = { isDragging = false }
                                )
                            }
                            .drawBehind {
                                // 在绘制区域底部画一条线
                                drawLine(
                                    color = SecondColor2,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(2.dp)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                        )
                    }

                    AnimatedContent(
                        modifier = Modifier.clip(shape = RoundedCornerShape(0.dp)),
                        targetState = journeyPanel2State,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(durationMillis = aniTime)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 4 },
                                        animationSpec = tween(durationMillis = aniTime)
                                    )) togetherWith
                                    (fadeOut(animationSpec = tween(durationMillis = aniTime)) +
                                            slideOutVertically(
                                                targetOffsetY = { -it / 4 },
                                                animationSpec = tween(durationMillis = aniTime)
                                            ))
                        },
                        label = "vertical_slide_fade_animation"
                    ) { state ->
                        when(state) {
                            JOURNEY_LIST -> {
                                JourneyList3(
                                    journeyList = journeyList,
                                    updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                    journeySelected = journeySelected,
                                    currentHeightRatio > 0.5f,
                                    togglePanelHeight,
                                )
                            }
                            JOURNEY_EDIT -> {
                                JourneyEdit(
                                    modifier = Modifier.weight(1f),
                                    journeySelected = journeySelected,
                                    navigate = { state, journey -> JourneyNavController.navigate(state, journey)},
                                    addJourney = { j -> journeyViewModel.createJourney(j) },
                                    updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                    deleteJourney = { j -> journeyViewModel.deleteJourney(j) },
                                    currentHeightRatio > 0.5f,
                                    togglePanelHeight,
                                )
                            }

                            FOOTPRINT_LIST -> {
                                journeySelected?.let {
                                    FootprintList(
                                        it,
                                        currentHeightRatio > 0.5f,
                                        togglePanelHeight,
                                    )
                                }
                            }
                            FOOTPRINT_EDIT -> {
                                journeySelected?.let {
                                    FootprintEdit(
                                        footprintSelected = footprintSelected,
                                        journeySelected = it,
                                        addFootprint = { j, f ->
                                            journeyViewModel.addFootprintsForJourney(
                                                journey = j,
                                                footprint = f
                                            )
                                        },
                                        updateFootprint = { f ->
                                            journeyViewModel.updateFootprint(f.copy())
                                        },
                                        deleteFootprint = { f ->
                                            journeyViewModel.deleteFootprint(f.copy())
                                        },
                                        currentHeightRatio > 0.5f,
                                        togglePanelHeight,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}