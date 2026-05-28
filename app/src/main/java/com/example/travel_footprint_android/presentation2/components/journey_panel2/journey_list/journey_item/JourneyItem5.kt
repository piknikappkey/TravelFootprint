package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark3
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.FontDark5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JourneyItem5(
    journey: Journey,
    journeyClick: () -> Unit = {},
    showDetail: Boolean,
    updateJourney: (Journey) -> Unit,
) {
    val starTime = System.currentTimeMillis()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = if (showDetail) 10.dp else 25.dp) // 外边距
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
    ) {
        val bgList = remember { listOf(R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2) }
        BGImgBox(
            bgList
        ) {
            val modifier = if(showDetail) Modifier else Modifier.clickable(onClick = journeyClick)
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .animateContentSize(
//                            animationSpec = tween(durationMillis = 300)
                        )
                ) {
                    if(!showDetail) {
                        Spacer(Modifier.width(3.dp))
                        ImageSquare2(
                            imgPath = journey.coverImagePath,
                            modifier = Modifier.width(110.dp),
                            aspectRatio = 1.2f,
                            addIconSize = .3f,
                            shape = RoundedCornerShape(5.dp),
                        )
                    }
                }
                RightContent(journey, showDetail, updateJourney)
            }
        }
    }
    if(showDetail) {
        Spacer(Modifier.padding(5.dp))
    }

    Log.d("ComposeTime", "JourneyItem5: ${System.currentTimeMillis() - starTime}")
}

@Composable
private fun RightContent(
    journey: Journey,
    showDetail: Boolean,
    updateJourney: (Journey) -> Unit,
) {
    // 1. 缓存日期格式化器，避免每次重组都创建新实例
    val fullDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val yearFormat = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
    val shortDateFormat = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }
    val currentYear = remember { yearFormat.format(Date()) }

    // 2. 缓存地址分割结果，避免重复 split 操作
    val addressParts = remember(journey.address) { journey.address.split("\n") }
    val region = addressParts.firstOrNull() ?: ""
    val location = addressParts.lastOrNull() ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 5.dp)
            .animateContentSize()
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
        }

        Spacer(Modifier.padding(1.dp))

        if(showDetail) {
            Column {
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
                    modifier = Modifier.padding(horizontal = 30.dp),
                    aspectRatio = 1.2f,
                    addIconSize = .3f
                )
                LineBetween(paddingUp = 12.dp)
            }
        }

        // 使用 derivedStateOf 缓存截断描述，仅当 description 实际变化时才重新计算，避免 showDetail 切换时无效执行字符串操作
        val truncatedDesc by remember {
            derivedStateOf {
                val desc = journey.description
                if (desc.length > 14) desc.substring(0, 14) + "... ..." else desc
            }
        }

        if(showDetail) {
            Column {
                TextMedium(
                    text = "旅程描述：",
                    firstLine = 0,
                    modifier = Modifier.padding(horizontal = 15.dp),
                    fontSize = 17.sp
                )
                Spacer(Modifier.padding(2.dp))
            }
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
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    val dateStr = fullDateFormat.format(journey.startDate)
                    Spacer(Modifier.width(10.dp))
                    TextSmall(
                        text = dateStr,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .padding(0.dp)
                            .offset(y = 5.dp)
                    )
                    Spacer(Modifier.weight(1f))
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
            }
        }
        if(!showDetail) {
            // 使用 derivedStateOf 缓存日期字符串计算，仅当 startDate 或 currentYear 变化时才重新计算
            val dateStr by remember {
                derivedStateOf {
                    val startYear = yearFormat.format(journey.startDate)
                    if (currentYear == startYear) {
                        shortDateFormat.format(journey.startDate)
                    } else {
                        fullDateFormat.format(journey.startDate)
                    }
                }
            }
            // 位置信息（字数不能太多）
            val loc by remember {
                derivedStateOf {
                    val l = location
                    if (l.length > 10) l.substring(0, 10) + "..." else l
                }
            }

            Row {
                Spacer(Modifier.width(10.dp))
                TextSmall(
                    text = loc
                )
                Spacer(Modifier.weight(1f))
                TextSmall(
                    text = dateStr,
                )
                Spacer(Modifier.width(5.dp))
            }
        }


        if(showDetail) {
            Column {
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
            }
        }
    }
}