package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.travel_footprint_android.presentation2.components.button.button_delete.ButtonDelete
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.util.Date

@Composable
fun FootprintEdit(
    footprintSelected: Footprint?,
    journeySelected: Journey,
    addFootprint: (Journey, Footprint) -> Unit,
    updateFootprint: (Footprint) -> Unit,
    deleteFootprint: (Footprint) -> Unit,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
) {
    var footprint by remember { mutableStateOf(
        footprintSelected?.copy()
            ?: Footprint(
                journeyId = journeySelected.id,
                title = journeySelected.title + "的足迹",
                description = "这是一个新的足迹",
                createTime = Date(),
                address = journeySelected.address,
                rating = 1
            )
    ) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(Modifier.height(10.dp))
        // 顶部导航栏
        HeadRow(
            footprintSelected,
            footprint,
            journeySelected,
            addFootprint,
            updateFootprint,
            journeyPanelHeightState,
            setJourneyPanelHeightState,
        )
        Spacer(Modifier.height(10.dp))

        Content(
            footprint,
            footprintSelected,
            journeySelected,
            { f -> footprint = f.copy() },
            deleteFootprint
        )
    }
}

@Composable
private fun HeadRow(
    footprintSelected: Footprint?,
    footprint: Footprint,
    journeySelected: Journey,
    addFootprint: (Journey, Footprint) -> Unit,
    updateFootprint: (Footprint) -> Unit,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST, journeyData = journeySelected)
                }),
            painter = painterResource(id = R.drawable.ic_left2),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )
        Spacer(Modifier.width(5.dp))
        Headline(
            text = if(footprintSelected == null) "新增足迹" else "编辑足迹",
            fontSize = 18.sp
        )
        Spacer(Modifier.weight(1f))
        // 保存按钮
        ButtonSave(
            onClick = {
                if (footprintSelected == null) {
                    addFootprint(journeySelected, footprint.copy())
                } else {
                    updateFootprint(footprint.copy())
                }
                JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST, journeyData = journeySelected)
            }
        )
        Spacer(Modifier.width(10.dp))

        IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })
        Spacer(Modifier.width(10.dp))
    }
}

@Composable
private fun Content(
    footprint: Footprint,
    footprintSelected: Footprint?,
    journeySelected: Journey,
    setFootprint: (Footprint) -> Unit,
    deleteFootprint: (Footprint) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        // 编辑内容区域
        Spacer(Modifier.padding(3.dp))

        // 足迹标题编辑
        FootprintEditCover(footprint, { text -> setFootprint(footprint.copy(title = text)) })

        LineBetween()

        // 足迹描述编辑
        FootprintDescription(footprint, { text -> setFootprint(footprint.copy(description = text)) })

        LineBetween()

        // 足迹地址编辑
        FootprintEditLocation(
            footprint,
            setFootprint = { f -> setFootprint(f.copy())},
        )

        LineBetween()

        // 个人评分编辑
        FootprintRating(footprint, { rating -> setFootprint(footprint.copy(rating = rating)) })

        LineBetween()

        Spacer(Modifier.padding(10.dp))

        if(footprintSelected != null) {
            Row {
                Spacer(Modifier.weight(1f))
                ButtonDelete(
                    title = "删除该旅程",
                    paddingValues = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                ) {
                    deleteFootprint(footprintSelected)
                    JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST, journeySelected)
                }
                Spacer(Modifier.width(10.dp))
            }

            Spacer(Modifier.padding(10.dp))
        }
    }
}