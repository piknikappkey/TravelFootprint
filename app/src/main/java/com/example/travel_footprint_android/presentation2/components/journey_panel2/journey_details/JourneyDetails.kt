package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.button.button_delete.ButtonDelete
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3

@Composable
fun JourneyDetails(
    modifier: Modifier = Modifier,
    journeySelected: Journey, // 当前选中的旅程
    updateJourney: (Journey) -> Unit,
    deleteJourney: (Journey) -> Unit,
    navigate: (JourneyPanel2State, Journey?) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        // 返回按钮
        Image(
            modifier = Modifier
                .size(26.dp)
                .padding(start = 5.dp)
                .clickable(onClick = {
                    navigate(JourneyPanel2State.JOURNEY_LIST, null)
                }),
            painter = painterResource(id = R.drawable.ic_left2),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )

        // 标题
        Headline(
            text = journeySelected.title,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp, horizontal = 3.dp)
        )

        IconEdit() {
            navigate(JourneyPanel2State.JOURNEY_EDIT, journeySelected)
        }

        Spacer(Modifier.width(10.dp))
    }
    // 可滚动内容
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.padding(5.dp))
        // 封面图片
        ImageSquare2(
            imgPath = journeySelected.coverImagePath,
            updateImgPath = { file ->
                journeySelected.coverImagePath = file.absolutePath
                updateJourney(journeySelected)
            },
            deleteImgPath = { imgPath ->
                journeySelected.coverImagePath = ""
                updateJourney(journeySelected)
            },
            modifier = Modifier.padding(horizontal = 60.dp),
            aspectRatio = 1.2f,
            addIconSize = .3f
        )
        Spacer(Modifier.padding(10.dp))
        // 描述内容
        TextMedium(
            text = "旅程描述：",
            firstLine = 0,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
        Spacer(Modifier.padding(2.dp))
        TextMedium(
            text = journeySelected.description,
            firstLine = 2,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
        Spacer(Modifier.padding(10.dp))
        // 回忆
        TextMedium(
            text = "旅程回忆",
            firstLine = 0,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
        Spacer(Modifier.padding(5.dp))
        Reminiscence(
            journey = journeySelected,
            updateJourney = updateJourney
        )
        Spacer(Modifier.padding(5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(20.dp))
            ButtonDelete {
                deleteJourney(journeySelected)
                navigate(JourneyPanel2State.JOURNEY_LIST, null)
            }
            Spacer(Modifier.weight(1f))
            ButtonMain(title = "旅程足迹") {

            }
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.padding(5.dp))
    }
}