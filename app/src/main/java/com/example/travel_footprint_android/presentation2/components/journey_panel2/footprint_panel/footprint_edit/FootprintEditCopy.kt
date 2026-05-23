package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_edit

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.util.Date

@Composable
fun FootprintEditCopy(
    footprintSelect: Footprint?,
    journeySelect: Journey,
    addFootprint: (Footprint) -> Unit,
    updateFootprint: (Footprint) -> Unit,
) {
    var footprint by remember { mutableStateOf(
        footprintSelect?.copy()
            ?: Footprint(
            journeyId = journeySelect.id,
            title = journeySelect.title + "的足迹",
            description = "这是一个新的足迹",
            createTime = Date(),
            address = "",
            rating = 0
        )
    ) }

    Column {
        Spacer(Modifier.height(10.dp))
        Row {
            // 返回按钮
            Image(
                modifier = Modifier
                    .size(26.dp)
                    .padding(start = 5.dp)
                    .clickable(onClick = {
                        JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST)
                    }),
                painter = painterResource(id = R.drawable.ic_left2),
                contentDescription = "返回图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
            Spacer(Modifier.width(10.dp))
            TextMedium(
                text = if(footprintSelect == null) "新增足迹" else "编辑足迹"
            )
            Spacer(Modifier.weight(1f))
            ButtonSave(
                onClick = {
                    if (footprintSelect == null) {
                        addFootprint(footprint)
                    } else {
                        updateFootprint(footprint)
                    }
                    JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST)
                }
            )
            Spacer(Modifier.width(10.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(200.dp)
        ) {
            // 这里需要用户输入内容，编辑footprint
        }
    }
}