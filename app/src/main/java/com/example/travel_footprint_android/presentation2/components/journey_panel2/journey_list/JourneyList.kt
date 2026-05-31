package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view.JourneyListView2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

@Composable
fun JourneyList(
    journeyList: List<Journey>,
    navigate: (JourneyPanel2State, Journey?) -> Unit
) {
    BGImgBox(
        listOf(R.drawable.bg_rectangular_1__3__0, R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2),
    ) {
        Column {
            // 固定标题
            Headline(
                text = "我的旅程",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 10.dp)
            )
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
                    JourneyListView2(
                        Modifier.padding(
                            start = 20.dp,
                            top = 0.dp,
                            end = 20.dp,
                            bottom = 70.dp
                        ), journeyList, navigate
                    )
                }
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
                            clickable = { navigate(JourneyPanel2State.JOURNEY_EDIT, null) },
                        )
                    }
                }
            }
        }
    }
}