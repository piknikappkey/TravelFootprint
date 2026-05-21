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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.JourneyDetails
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.JourneyEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.JourneyList
import com.example.travel_footprint_android.presentation2.components.journey_panel2.location_button.LocationButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_DETAILS
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_EDIT
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State.JOURNEY_LIST
import com.example.travel_footprint_android.ui.theme.BGLight1

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun JourneyPanel2(
    modifier: Modifier = Modifier,
    journeyList: List<Journey>, // 旅程列表
    updateJourney: (Journey) -> Unit,
    addJourney: (Journey) -> Unit,
    deleteJourney: (Journey) -> Unit,
    aniTime: Int,
) {
    // 面板状态
    val journeyPanel2State = JourneyNavController.journeyNavController.value

    // 当前选中的旅程
    val journeySelected = JourneyNavController.journeyData.value


    Box(
        modifier = modifier
            .layout { measurable, constraints ->
                // 测量原本的内容
                val placeable = measurable.measure(constraints)
                val offsetPx = 30.dp.roundToPx()

                // 布局高度 = 原高度 - 偏移量（最小为 0）
                val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)

                layout(placeable.width, layoutHeight) {
                    // 把内容放到向上偏移的位置
                    placeable.placeRelative(0, -offsetPx)
                }
            }
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
                    clip = true
                )
                .background(
                    color = BGLight1,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                )
                .animateContentSize(  // 关键修饰符
                    animationSpec = tween(durationMillis = aniTime)
                )
        ) {
            // 内部用 Column 分隔固定标题和可滚动区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f)   // 限制面板最大高度，内容少时自适应
            ) {
                when(journeyPanel2State) {
                    JOURNEY_LIST -> {
                        JourneyList(
                            journeyList = journeyList,
                            navigate = { state, journey -> JourneyNavController.navigate(state, journey)}
                        )
                    }
                    JOURNEY_DETAILS -> {
                        journeySelected?.let {
                            JourneyDetails(
                                modifier = Modifier.weight(1f),
                                journeySelected = it,
                                updateJourney = updateJourney,
                                deleteJourney = deleteJourney,
                                navigate = { state, journey -> JourneyNavController.navigate(state, journey)}

                            )
                        }
                    }
                    JOURNEY_EDIT -> {
                        JourneyEdit(
                            modifier = Modifier.weight(1f),
                            journeySelected = journeySelected,
                            navigate = { state, journey -> JourneyNavController.navigate(state, journey)},
                            addJourney = addJourney,
                            updateJourney = updateJourney,
                        )
                    }
                }
            }
        }
    }
}