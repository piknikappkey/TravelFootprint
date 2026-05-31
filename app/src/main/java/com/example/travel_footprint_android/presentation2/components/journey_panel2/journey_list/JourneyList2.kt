package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation2.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view.JourneyListView3
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun JourneyList2(
    journeyList: List<Journey>,
    updateJourney: (Journey) -> Unit,
    journeySelected: Journey?,
    ) {
    BGImgBox(
        listOf(R.drawable.bg_rectangular_1__3__0),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                if (journeySelected != null) {
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
                // 固定标题
                Headline(
                    text = "我的旅程",
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 5.dp, horizontal = 10.dp)
                )

                if (journeySelected != null) {
                    IconEdit() {
                        JourneyNavController.navigate(
                            JourneyPanel2State.JOURNEY_EDIT,
                            journeySelected
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
            }

            // 可滚动内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                if (journeyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .heightIn(250.dp),
                    ) {
                        TextMedium(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = "目前还没有旅程内容哦~\n点击右下角添加按钮，新增你的足迹~",
                            fontSize = 15.sp,
                        )
                    }
                } else {
                    // 旅程列表
                    JourneyListView3(
                        journeyList = journeyList,
                        journeySelected = journeySelected,
                        updateJourney = updateJourney
                    )
                }
                if (journeySelected == null) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        BGBox {
                            IconAdd(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(48.dp),
                                clickable = {
                                    JourneyNavController.navigate(
                                        JourneyPanel2State.JOURNEY_EDIT,
                                        null
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}