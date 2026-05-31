package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark3
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.FontDark5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JourneyItem4(
    journey: Journey,
    journeyClick: () -> Unit = {},
    showDetail: Boolean,
    updateJourney: (Journey) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = if(showDetail) 10.dp else 25.dp) // 外边距
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
    ) {
        BGImgBox(
            listOf(R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2),
        ) {
            val modifier = if (showDetail) Modifier else Modifier.clickable(onClick = journeyClick)
            Row(
                modifier = modifier
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!showDetail) {
                    Spacer(Modifier.width(3.dp))
                    ImageSquare2(
                        imgPath = journey.coverImagePath,
                        modifier = Modifier.width(100.dp).padding(horizontal = 5.dp),
                        aspectRatio = 1.2f,
                        addIconSize = .3f,
                        shape = RoundedCornerShape(5.dp)
                    )
                }
                RightContent(journey, showDetail, updateJourney)
            }
        }
    }
}

@Composable
private fun RightContent(
    journey: Journey,
    showDetail: Boolean,
    updateJourney: (Journey) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 5.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if(showDetail) Arrangement.Center else Arrangement.Start
        ) {
            TextMedium(
                text = journey.title,
                fontSize = if(showDetail) 20.sp else 15.sp,
                color = if(showDetail) FontDark3 else FontDark4,
            )
            Spacer(Modifier.padding(9.dp))
        }

        Spacer(Modifier.padding(1.dp))

        // 封面
        if(showDetail) {
            ImageSquare2(
                imgPath = journey.coverImagePath,
                updateImgPath = { file ->
                    journey.coverImagePath = file.absolutePath
                    updateJourney(journey)
                    file
                },
                deleteImgPath = { imgPath ->
                    journey.coverImagePath = ""
                    updateJourney(journey)
                },
                modifier = Modifier.padding(horizontal = 40.dp),
                aspectRatio = 1.2f,
                addIconSize = .3f
            )
            LineBetween(paddingUp = 12.dp)
        }

        // 描述（不显示细节时：两行以内，多余用...代替）
        val truncatedDesc = if (journey.description.length > 14) {
            journey.description.substring(0, 14) + "... ..."
        } else {
            journey.description
        }

        if(showDetail) {
            TextMedium(
                text = "旅程描述：",
                firstLine = 0,
                modifier = Modifier.padding(horizontal = 15.dp),
                fontSize = 17.sp
            )
            Spacer(Modifier.padding(2.dp))
        }

        TextSmall(
            modifier = Modifier.padding(start = if(showDetail) 15.dp else 2.dp, end = if(showDetail) 15.dp else 0.dp),
            text = if(showDetail) journey.description else truncatedDesc,
            color = if(showDetail) FontDark4 else FontDark5,
            firstLine = 1,
            fontSize = if(showDetail) 16.sp else 14.sp,
            minLines = if(showDetail) 1 else 2,
            maxLines = if(showDetail) Int.MAX_VALUE else 2,
        )

        Spacer(Modifier.padding(2.dp))

        // 显示地址与日期
        if(showDetail) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                // 开始日期
                val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateStr = fullDateFormat.format(journey.startDate)
                // 显示地址与日期
                Spacer(Modifier.width(10.dp))
                TextSmall(
                    text = dateStr,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(0.dp).offset(y = 5.dp)
                )
                Spacer(Modifier.weight(1f))
                val region = journey.address.split("\n")[0]
                val location = journey.address.split("\n").last()
                Column {
                    TextSmall(
                        text = location,
                        firstLine = 0,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                    TextSmall(
                        text = region,
                        firstLine = 2,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 15.dp),
                    )
                }
            }
            LineBetween()
        } else {
            // 开始日期
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val shortDateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

            val currentYear = yearFormat.format(Date())
            val startYear = yearFormat.format(journey.startDate)

            val dateStr = if (currentYear == startYear) {
                // 同一年，只显示月日
                shortDateFormat.format(journey.startDate)
            } else {
                // 不同年，显示完整日期
                fullDateFormat.format(journey.startDate)
            }
            Row {
                Spacer(Modifier.width(10.dp))
                TextSmall(
                    text = journey.address.split("\n").last()
                )
                Spacer(Modifier.weight(1f))
                TextSmall(
                    text = dateStr,
                )
                Spacer(Modifier.width(5.dp))
            }
        }


        if(showDetail) {
            TextMedium(
                text = "旅程回忆",
                firstLine = 0,
                modifier = Modifier.padding(horizontal = 15.dp),
                fontSize = 17.sp
            )
            Spacer(Modifier.padding(5.dp))
            Reminiscence(
                journey = journey,
                updateJourney = updateJourney
            )
            Row {
                Spacer(Modifier.weight(1f))
                ButtonMain(
                    onClick = {
                        JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST, journeyData = journey)
                    },
                    paddingValues = PaddingValues(0.dp)
                ) {
                    BGBox {
                        BGImgBox(
                            listOf(R.drawable.bg_simple_hor_small_small),
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 5.dp, horizontal = 12.dp)
                            ) {
                                TextMedium("旅程足迹")
                            }
                        }
                    }
                }
                Spacer(Modifier.width(5.dp))
            }

            Spacer(Modifier.padding(5.dp))
        }
        if(showDetail) {
            Spacer(Modifier.padding(10.dp))
        }
    }
}