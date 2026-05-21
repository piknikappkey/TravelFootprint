package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.viewmodel.FootprintPanel2State
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium

@Composable
fun FootprintList(
    footprint: List<Footprint>,
    journeySelected: Journey,
) {
    Column {
        Spacer(Modifier.height(10.dp))
        Row {
            Spacer(Modifier.width(10.dp))
            TextMedium(
                text = "${journeySelected.title}——足迹"
            )
        }
        LineBetween(paddingUp = 2.dp, paddingDown = 2.dp, )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(200.dp)
        ) {
            if (footprint.isEmpty()) {
                TextMedium(
                    modifier = Modifier
                        .align(Alignment.Center),
                    text = "目前还没有足迹内容哦~\n点击右下角添加按钮，新增你的足迹~",
                    fontSize = 15.sp,
                )
            } else {
                // 足迹列表
            }

            // 添加足迹按钮
            FootprintListAddIcon(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                clickable = {
                    // 跳转到足迹编辑页面
                    FootprintNavController.navigate(FootprintPanel2State.FOOTPRINT_EDIT)
                }
            )
        }
    }
}