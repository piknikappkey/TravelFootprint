package com.example.travel_footprint_android.presentation.components.journey_panel

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_edit.FootprintEdit
import com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_list.FootprintList
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail.JourneyDetail
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.JourneyEdit
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list.JourneyList
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.FOOTPRINT_EDIT
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.FOOTPRINT_LIST
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.JOURNEY_DETAIL
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.JOURNEY_EDIT
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State.JOURNEY_LIST
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanelState
import com.example.travel_footprint_android.presentation.viewmodel.JourneyViewModel


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel7(
    modifier: Modifier = Modifier,
    aniTime: Int,
    journeyViewModel: JourneyViewModel = hiltViewModel(key = "journey"),
    panelState: JourneyPanelState,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    val journeyUiState by journeyViewModel.uiState.collectAsState()
    val journeyList = journeyUiState.journeys

    val journeyPanel2State = panelState.currentPage
    val journeySelected = panelState.selectedJourney
    val footprintSelected = panelState.selectedFootprint

    val configuration = LocalConfiguration.current
    val density = remember { configuration.densityDpi.toFloat() / 160f }
    val screenHeightPixels = remember { configuration.screenHeightDp * density }

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
                val placeable = measurable.measure(constraints)
                val offsetPx = 60.dp.roundToPx()
                val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)
                layout(placeable.width, layoutHeight) {
                    placeable.placeRelative(0, -offsetPx)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                        onDragEnd = { isDragging = false }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(4.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                    clip = true
                )
        ) {
            BGImgBox(listOf(R.drawable.bg_rectangular_1__3__0)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(configuration.screenHeightDp.dp * aniJourneyHeight)
                ) {
                    AnimatedContent(
                        modifier = Modifier.clip(shape = RoundedCornerShape(0.dp))
                            .align(Alignment.BottomCenter),
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
                        when (state) {
                            JOURNEY_LIST -> {
                                JourneyList(
                                    journeyList = journeyList,
                                    journeyPanelHeightState = currentHeightRatio > 0.5f,
                                    setJourneyPanelHeightState = togglePanelHeight,
                                    setIsDragging = { b -> isDragging = b },
                                    onDragDelta = onDragDelta,
                                    onPanelNavigate = onPanelNavigate,
                                )
                            }

                            JOURNEY_DETAIL -> {
                                journeySelected?.let {
                                    JourneyDetail(
                                        journey = it,
                                        updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                        journeyPanelHeightState = currentHeightRatio > 0.5f,
                                        setJourneyPanelHeightState = togglePanelHeight,
                                        setIsDragging = { b -> isDragging = b },
                                        onDragDelta = onDragDelta,
                                        onPanelNavigate = onPanelNavigate,
                                    )
                                }
                            }

                            JOURNEY_EDIT -> {
                                JourneyEdit(
                                    journeySelected = journeySelected,
                                    navigate = { state, journey ->
                                        onPanelNavigate(state, journey, null)
                                    },
                                    addJourney = { j -> journeyViewModel.createJourney(j) },
                                    updateJourney = { j -> journeyViewModel.updateJourney(j) },
                                    deleteJourney = { j -> journeyViewModel.deleteJourney(j) },
                                    journeyPanelHeightState = currentHeightRatio > 0.5f,
                                    setJourneyPanelHeightState = togglePanelHeight,
                                    setIsDragging = { b -> isDragging = b },
                                    onDragDelta = onDragDelta,
                                )
                            }

                            FOOTPRINT_LIST -> {
                                journeySelected?.let {
                                    FootprintList(
                                        it,
                                        currentHeightRatio > 0.5f,
                                        togglePanelHeight,
                                        setIsDragging = { b -> isDragging = b },
                                        onDragDelta = onDragDelta,
                                        onPanelNavigate = onPanelNavigate,
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
                                        setIsDragging = { b -> isDragging = b },
                                        onDragDelta = onDragDelta,
                                        onPanelNavigate = onPanelNavigate,
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
