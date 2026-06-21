package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.custom_scrollbar.VerticalCustomScrollbar
import com.example.travel_footprint_android.presentation.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation.components.journey_panel.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun JourneyDetail(
    journeySelected: Journey?,
    updateJourney: (Journey) -> Unit,
    journeyPanelExpandedState: Boolean,
    setJourneyPanelOffset: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    val journey = remember { journeySelected?.copy() }

    if(journey == null) {
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HeadRow(
            journey = journey,
            journeyPanelExpandedState = journeyPanelExpandedState,
            setJourneyPanelOffset = setJourneyPanelOffset,
            setIsDragging = setIsDragging,
            onDragDelta = onDragDelta,
            onPanelNavigate = onPanelNavigate
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val detailScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(detailScrollState)
            ) {
                Content(journey = journey, updateJourney = updateJourney)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                ToFootprintButton(
                    journey = journey,
                    onPanelNavigate = onPanelNavigate
                )
            }

            VerticalCustomScrollbar(
                scrollState = detailScrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun HeadRow(
    journey: Journey,
    journeyPanelExpandedState: Boolean,
    setJourneyPanelOffset: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    onPanelNavigate(JourneyPanel2State.JOURNEY_LIST, null, null)
                }),
            painter = painterResource(id = R.drawable.ic_left_long),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )

        Headline(
            text = "旅程详情",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp, horizontal = 10.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { setIsDragging(true) },
                        onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                        onDragEnd = { setIsDragging(false) }
                    )
                },
        )

        Spacer(Modifier.width(10.dp))
        IconEdit {
            onPanelNavigate(JourneyPanel2State.JOURNEY_EDIT, journey, null)
        }
        Spacer(Modifier.width(10.dp))
        IcJourneyHeightButton(journeyPanelExpandedState, { setJourneyPanelOffset(!journeyPanelExpandedState) })
        Spacer(Modifier.width(10.dp))
    }
}

@Composable
private fun Content(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(15.dp, 10.dp, 15.dp, 50.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(8.dp),
                clip = true,
            ),
    ) {
        BGImgBox(R.drawable.bg_rectangular_1__2__1, R.drawable.bg_rectangular_1__2__2) {
            Column {
                Spacer(Modifier.height(5.dp))
                JourneyDetailTitle(
                    title = journey.title,
                )

                Spacer(Modifier.padding(1.dp))

                JourneyDetailCoverSection(
                    journey = journey,
                    updateJourney = updateJourney,
                )

                JourneyDetailDescription(
                    description = journey.description,
                )

                Spacer(Modifier.padding(2.dp))

                JourneyDetailAddressDate(
                    startDate = journey.startDate.time,
                    address = journey.address,
                )

                JourneyDetailReminiscenceSection(
                    journey = journey,
                    updateJourney = updateJourney,
                )

                Spacer(Modifier.padding(2.dp))
            }
        }
    }
}