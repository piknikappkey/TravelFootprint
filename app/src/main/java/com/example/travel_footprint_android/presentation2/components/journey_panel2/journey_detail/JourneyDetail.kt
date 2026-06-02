package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun JourneyDetail(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    Column {
        HeadRow(
            journey = journey,
            journeyPanelHeightState = journeyPanelHeightState,
            setJourneyPanelHeightState = setJourneyPanelHeightState,
            setIsDragging = setIsDragging,
            onDragDelta = onDragDelta,
            onPanelNavigate = onPanelNavigate
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
        }
    }
}

@Composable
private fun HeadRow(
    journey: Journey,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
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
        IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })
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
        BGImgBox(listOf(R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2)) {
            Column {
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