package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_delete.ButtonDelete
import com.example.travel_footprint_android.presentation2.components.button.button_save.ButtonSave
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.cover.JourneyEditCover
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.description.JourneyEditDescription
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.images.JourneyEditImages
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.location.JourneyEditLocation
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.title.JourneyEditTitle
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.util.Date

@Composable
fun JourneyEdit(
    modifier: Modifier = Modifier,
    journeySelected: Journey? = null,
    navigate: (JourneyPanel2State, Journey?) -> Unit,
    addJourney: (Journey) -> Unit,
    updateJourney: (Journey) -> Unit,
    deleteJourney: (Journey) -> Unit,
    ) {
    var journey by remember { mutableStateOf(
        journeySelected?.copy()
            ?: Journey(
                title = "新的开始",
                description = "这是一段新的旅程",
                startDate = Date(),
                endDate = Date(),
                coverStyle = "",
                coverImagePath = "",
                journeyImagePaths = List(0, { i -> "" })
            )
        )
    }


    BGBox {
        Column {
            // 顶部内容
            JourneyHead(
                journey,
                journeySelected,
                navigate,
                addJourney,
                updateJourney,

            )
            // 可滚动内容
            JourneyContent(
                modifier,
                journey,
                journeySelected,
                { j -> journey = j.copy() },
                { j ->
                    deleteJourney(j)
                    JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, null)
                },
            )
        }
    }
}


@Composable
fun JourneyHead(
    journey: Journey,
    journeySelected: Journey? = null,
    navigate: (JourneyPanel2State, Journey?) -> Unit,
    addJourney: (Journey) -> Unit,
    updateJourney: (Journey) -> Unit,
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
                    if(journeySelected == null) {
                        navigate(JourneyPanel2State.JOURNEY_LIST, null)
                    } else {
                        navigate(JourneyPanel2State.JOURNEY_LIST, journeySelected)
                    }
                }),
            painter = painterResource(id = R.drawable.ic_left_long),
            contentDescription = "返回图标",
            colorFilter = ColorFilter.tint(SecondColor3),
        )

        // 标题
        Headline(
            text = "开启新旅程",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp, horizontal = 3.dp)
        )

        ButtonSave(
            onClick = {
                if (journeySelected == null) {
                    addJourney(journey)
                } else {
                    updateJourney(journey)
                }
                navigate(JourneyPanel2State.JOURNEY_LIST, null)
            }
        )

        Spacer(Modifier.width(10.dp))
    }
}

@Composable
fun JourneyContent(
    modifier: Modifier = Modifier,
    journey: Journey,
    journeySelected: Journey? = null,
    setJourney: (Journey) -> Unit,
    deleteJourney: (Journey) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.padding(2.dp))

        BGBox(
            modifier = Modifier
                .padding(horizontal = 10.dp)
        ) {
            BGImgBox(
                imgList = listOf<Int>(R.drawable.bg_rectangular_1__3__0, R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2),
            ) {
                Column {
                    Spacer(Modifier.padding(3.dp))
                    // 旅程标题编辑----------------------------------------
                    JourneyEditTitle(
                        journey = journey,
                        onValueChange = { text ->  setJourney(journey.copy(title = text)) }
                    )
                    LineBetween()

                    // 封面图片编辑----------------------------------------
                    JourneyEditCover(
                        journey = journey,
                        updateImgPath = { file ->
                            setJourney(journey.copy(coverImagePath = file.absolutePath))
                            file
                        },
                        deleteImgPath = { imgPath ->
                            setJourney(journey.copy(coverImagePath = ""))
                        }
                    )
                    LineBetween()

                    // 旅程描述编辑----------------------------------------
                    JourneyEditDescription(
                        journey = journey,
                        onValueChange = { text -> setJourney(journey.copy(description = text)) }
                    )
                    LineBetween()

                    // 旅程地址----------------------------------------
                    JourneyEditLocation(
                        journey = journey,
                        setJourney = { j ->
                            setJourney(j.copy())
                        }
                    )
                    LineBetween()

                    // 回忆编辑----------------------------------------
                    JourneyEditImages(
                        journey = journey,
                        updateJourney = { j ->
                            // 触发ui更新
                            val newList =
                                List(j.journeyImagePaths.size, { i -> j.journeyImagePaths[i] })
                            setJourney(j.copy(journeyImagePaths = List(0, { i -> "" })))
                            setJourney(j.copy(journeyImagePaths = newList))
                        }
                    )
                    Spacer(Modifier.padding(10.dp))

                    if(journeySelected != null) {
                        Row {
                            Spacer(Modifier.weight(1f))
                            ButtonDelete(
                                title = "删除该旅程",
                                paddingValues = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                deleteJourney(journeySelected)
                            }
                            Spacer(Modifier.width(10.dp))
                        }

                        Spacer(Modifier.padding(10.dp))
                    }
                }
            }
        }
        Spacer(Modifier.padding(10.dp))
    }
}