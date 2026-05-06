package com.example.travel_footprint_android.presentation2.components.journey_panel2

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_add.JourneyAdd
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.JourneyDetails
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.JourneyList
import com.example.travel_footprint_android.presentation2.components.journey_panel2.location_button.LocationButton
import com.example.travel_footprint_android.ui.theme.BGLight1

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel2(
    modifier: Modifier = Modifier,
    journeyList: List<Journey>, // 旅程列表
    updateJourney: (Journey) -> Unit,
) {
    // 面板状态
    var journeyPanel2State by remember { mutableStateOf(JourneyPanel2State.JOURNEY_LIST) }
    // 当前选中的旅程
    var journeySelected by remember { mutableStateOf<Journey?>(null) }

    Box(
        modifier = modifier
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
                    clip = false
                )
                .background(
                    color = BGLight1,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                )
                .animateContentSize(  // 关键修饰符
                    animationSpec = tween(durationMillis = 300)
                )
        ) {
            // 内部用 Column 分隔固定标题和可滚动区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f)   // 限制面板最大高度，内容少时自适应
            ) {
                when(journeyPanel2State) {
                    JourneyPanel2State.JOURNEY_LIST -> {
                        JourneyList(
                            journeyList = journeyList,
                            selectJourney = { item -> journeySelected = item },
                            setState = { state -> journeyPanel2State = state}
                        )
                    }
                    JourneyPanel2State.JOURNEY_DETAILS -> {
                        journeySelected?.let {
                            JourneyDetails(
                                modifier = Modifier.weight(1f),
                                journeySelected = it,
                                updateJourney = updateJourney,
                                setState = { state -> journeyPanel2State = state }
                            )
                        }
                    }
                    JourneyPanel2State.JOURNEY_DETAILS_EDIT -> {

                    }
                    JourneyPanel2State.JOURNEY_ADD -> {
                        JourneyAdd(
                            modifier = Modifier.weight(1f),
                            setState = { state -> journeyPanel2State = state }
                        )
                    }
                }
            }
        }
    }
}

enum class JourneyPanel2State {
    JOURNEY_LIST,
    JOURNEY_DETAILS,
    JOURNEY_DETAILS_EDIT,
    JOURNEY_ADD,
}