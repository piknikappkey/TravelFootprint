package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation2.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view.JourneyListView4
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun JourneyList3(
    journeyList: List<Journey>,
    updateJourney: (Journey) -> Unit,
    journeySelected: Journey?,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    ) {
    val starTime = System.currentTimeMillis()

    Column{
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                if(journeySelected != null) {
                    Image(
                        modifier = Modifier
                            .size(26.dp)
                            .padding(start = 5.dp)
                            .clickable(onClick = {
                                JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, null)
                            }),
                        painter = painterResource(id = R.drawable.ic_left_long),
                        contentDescription = "返回图标",
                        colorFilter = ColorFilter.tint(SecondColor3),
                    )
                }
            }
            // 固定标题
            Headline(
                text = "我的旅程",
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
            )

            Column(
                modifier = Modifier.animateContentSize()
            ) {
                if(journeySelected != null) {
                    IconEdit() {
                        JourneyNavController.navigate(JourneyPanel2State.JOURNEY_EDIT, journeySelected)
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })

            Spacer(Modifier.width(10.dp))
        }

        // 可滚动内容
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
                // 旅程列表
                JourneyListView4(journeyList = journeyList, journeySelected =  journeySelected, updateJourney =  updateJourney)
            }

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column {
                    AnimatedVisibility(
                        visible = journeySelected == null,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f)
                    ) {
                        BGBox {
                            IconAdd(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(48.dp),
                                clickable = {
                                    JourneyNavController.navigate(JourneyPanel2State.JOURNEY_EDIT, null)
                                },
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column {
                    AnimatedVisibility(
                        visible = journeySelected != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f)
                    ) {
                        ButtonMain(
                            onClick = {
                                JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST, journeyData = journeySelected)
                            },
                            paddingValues = PaddingValues(0.dp)
                        ) {
                            BGBox {
                                BGImgBox(
                                    listOf(R.drawable.bg_simple_hor_small_small)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(vertical = 5.dp, horizontal = 12.dp)
                                    ) {
                                        TextMedium("前往足迹->")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Log.d("ComposeTime", "JourneyList3: ${System.currentTimeMillis() - starTime}")
}