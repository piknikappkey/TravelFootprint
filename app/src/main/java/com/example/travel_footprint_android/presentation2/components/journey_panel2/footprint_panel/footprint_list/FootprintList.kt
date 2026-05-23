package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun FootprintList(
    footprints: List<Footprint>,
    journeySelected: Journey,
) {
    var clickItemIndex by remember { mutableStateOf(-1) }

    Log.d("FootprintList", "footprints = ${footprints}")

    BGImgBox(
        imgList = listOf<Int>(R.drawable.bg_rectangular_1__3__0, R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2),
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = { clickItemIndex = -1})
        ) {
            Spacer(Modifier.height(10.dp))
            HeadRow(journeySelected)
            LineBetween(paddingUp = 2.dp, paddingDown = 2.dp, )
            Content(
                footprints,
                journeySelected,
                clickItemIndex,
                { i -> clickItemIndex = i}
            )
        }
    }
}

@Composable
private fun HeadRow(
    journeySelected: Journey,
) {
    Row {
        Spacer(Modifier.width(10.dp))
        // 返回按钮
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    JourneyNavController.navigate(JourneyPanel2State.JOURNEY_DETAILS, journeyData = journeySelected)
                }),
            painter = painterResource(id = R.drawable.ic_left2),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )
        Spacer(Modifier.width(5.dp))
        Headline(
            text = "${journeySelected.title}——足迹",
            fontSize = 18.sp
        )
    }
}

@Composable
private fun Content(
    footprints: List<Footprint>,
    journeySelected: Journey,
    clickItemIndex: Int,
    setClickItemIndex: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(200.dp)
    ) {
        if (footprints.isEmpty()) {
            TextMedium(
                modifier = Modifier
                    .align(Alignment.Center),
                text = "目前还没有足迹内容哦~\n点击右下角添加按钮，新增你的足迹~",
                fontSize = 15.sp,
            )
        } else {
            // 足迹列表
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 1000.dp)
                    .padding(horizontal = 20.dp)
            ) {
                itemsIndexed(footprints, key = { i, it -> it.id}) { index, footprint ->
                    FootprintListItem(
                        footprint = footprint,
                        footprintClick = { i ->
                            setClickItemIndex(i ?: index)
                        },
                        (index == clickItemIndex),
                        journeySelected
                    )
                }
                item {
                    Spacer(Modifier.height(70.dp))
                }
            }
        }

        // 添加足迹按钮
        FootprintListAddIcon(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            clickable = {
                // 跳转到足迹编辑页面
                JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_EDIT, journeyData = journeySelected)
            }
        )
    }
}