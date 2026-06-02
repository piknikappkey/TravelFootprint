package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation.components.journey_panel.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium

@Composable
fun JourneyList(
    journeyList: List<Journey>,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
    Column{
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Headline(
                text = "我的旅程",
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
            IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })
            Spacer(Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (journeyList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp, 5.dp, 20.dp, 20.dp)
                        .background(Color.White.copy(.5f), RoundedCornerShape(5.dp))
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                    ) {
                        TextMedium(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = "目前还没有旅程内容哦~\n点击右下角添加按钮，新增你的足迹~",
                            fontSize = 15.sp,
                        )
                    }
                }
            } else {
                JourneyListView(
                    journeyList = journeyList,
                    onPanelNavigate = onPanelNavigate,
                )
            }

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f)
                    ) {
                        IconAdd(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            clickable = {
                                onPanelNavigate(JourneyPanel2State.JOURNEY_EDIT, null, null)
                            },
                        )
                    }
                }
            }
        }
    }
}
