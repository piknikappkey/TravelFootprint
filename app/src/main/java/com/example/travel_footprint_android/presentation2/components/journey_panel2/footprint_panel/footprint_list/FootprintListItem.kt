package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_list.footprint_list_panel.FootprintListPanel
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.text.SimpleDateFormat
import java.util.Locale

private const val ANIMATION_DURATION = 400

@Composable
fun FootprintListItem(
    footprint: Footprint,
    footprintClick: (Int?) -> Unit,
    isClicked: Boolean,
    journeySelected: Journey,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 5.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
            .clickable(onClick = { footprintClick(null) })
    ) {
        BGImgBox(
            listOf(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3)
        ) {
            Content(footprint, footprintClick, isClicked, journeySelected)
        }
    }
}

@Composable
fun Content(
    footprint: Footprint,
    footprintClick: (Int?) -> Unit,
    isClicked: Boolean,
    journeySelected: Journey,
) {
//    var isRecord by remember { mutableStateOf(false) }
//    LocationRecorder(isRecord = isRecord) { latitude, longitude -> }

    Column(
        modifier = Modifier.animateContentSize() // 使整体高度变化平滑
    ) {
        Spacer(Modifier.height(10.dp))
        // 顶部标题及功能栏
        HeadRow(footprint, footprintClick, isClicked, journeySelected)
        Spacer(Modifier.height(10.dp))
        // 足迹描述
        Description(footprint, isClicked)
        Spacer(Modifier.height(10.dp))
        // 足迹状态面板
        if(isClicked) {
            FootprintListPanel(footprint)
        }
        // 底部信息（足迹创建时间、足迹开始地址）
        BottomContent(footprint, isClicked)
        Spacer(Modifier.height(5.dp))
    }
}

// 顶部标题及功能栏
@Composable
fun HeadRow(
    footprint: Footprint,
    footprintClick: (Int?) -> Unit,
    isClicked: Boolean,
    journeySelected: Journey,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 固定左侧间距，按钮出现/消失时标题平滑移动
        Spacer(Modifier.width(5.dp))
        AnimatedVisibility(
            visible = isClicked,
            enter = expandHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(animationSpec = tween(ANIMATION_DURATION)),
            exit = shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
        ) {
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = { footprintClick(-1) }),
                painter = painterResource(id = R.drawable.ic_left2),
                contentDescription = "返回图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
        }
        TextMedium(text = footprint.title)
        Spacer(Modifier.weight(1f))
        AnimatedVisibility(
            visible = isClicked,
            enter = expandHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(animationSpec = tween(ANIMATION_DURATION)),
            exit = shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
        ) {
            IconEdit(
                modifier = Modifier.size(16.dp)
            ) {
                JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_EDIT, footprintData = footprint, journeyData = journeySelected)
            }
        }
        Spacer(Modifier.width(10.dp))
    }
}

// 足迹描述
@Composable
fun Description(
    footprint: Footprint,
    isClicked: Boolean,
) {
    val description = if (isClicked) {
        footprint.description
    } else {
        if (footprint.description.length > 20) {
            footprint.description.take(20) + "... ..."
        } else {
            footprint.description
        }
    }
    TextMedium(
        text = description,
        firstLine = 2,
        modifier = Modifier.padding(horizontal = 15.dp),
        fontSize = 17.sp,
        color = FontDark4
    )
}

// 底部信息（足迹创建时间、足迹开始地址）
@Composable
fun BottomContent(
    footprint: Footprint,
    isClicked: Boolean,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.animateContentSize() // 使行高变化平滑，日期文本跟随移动
    ) {
        TimeView(footprint)
        Spacer(Modifier.weight(1f))
        Location(footprint, isClicked)
    }
}

@Composable
fun TimeView(
    footprint: Footprint,
) {
    val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateStr = fullDateFormat.format(footprint.createTime)
    TextSmall(
        text = dateStr,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 10.dp)
    )
}

@Composable
fun Location(
    footprint: Footprint,
    isClicked: Boolean,
) {
    val region = footprint.address.split("\n")[0]
    val location = footprint.address.split("\n").last()

    Column(
        modifier = Modifier.animateContentSize()
    ) {
        TextSmall(
            text = location,
            firstLine = 0,
            modifier = Modifier.padding(end = 10.dp)
        )
        AnimatedVisibility(
            visible = isClicked,
            enter = expandHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(animationSpec = tween(ANIMATION_DURATION)),
            exit = shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
        ) {
            TextSmall(
                text = region,
                firstLine = 2,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 10.dp),
            )
        }
    }
}