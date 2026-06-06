package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FootprintDetails(
    footprintSelected: Footprint,
    journeySelected: Journey,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
) {
//    // 是否记录用户定位
//    var isRecord by remember { mutableStateOf(true) }
//    // 位置记录组件 - 当 isRecord 为 true 时持续获取定位
//    LocationRecorder(isRecord = isRecord) { latitude, longitude ->
//
//    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(Modifier.height(10.dp))
        // 顶部导航栏
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            Image(
                modifier = Modifier
                    .size(26.dp)
                    .padding(start = 5.dp)
                    .clickable(onClick = {
                        onPanelNavigate(JourneyPanel2State.FOOTPRINT_EDIT, journeySelected, footprintSelected)
                    }),
                painter = painterResource(id = R.drawable.ic_left2),
                contentDescription = "返回图标",
                colorFilter = ColorFilter.tint(SecondColor3),
            )
            Spacer(Modifier.width(5.dp))
            TextMedium(
                text = footprintSelected.title
            )
            Spacer(Modifier.weight(1f))
            IconEdit() {
                onPanelNavigate(JourneyPanel2State.FOOTPRINT_EDIT, journeySelected, footprintSelected)
            }
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.height(10.dp))
        // 描述内容
        TextMedium(
            text = "足迹描述：",
            firstLine = 0,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
        Spacer(Modifier.padding(2.dp))
        TextMedium(
            text = footprintSelected.description,
            firstLine = 2,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
        Spacer(Modifier.padding(2.dp))
        // 旅程地点以及时间
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            // 开始日期
            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = fullDateFormat.format(footprintSelected.createTime)
            // 显示地址与日期
            Spacer(Modifier.width(10.dp))
            TextSmall(
                text = dateStr,
                fontSize = 11.sp,
                modifier = Modifier.padding(0.dp).offset(y = 5.dp)
            )
            Spacer(Modifier.weight(1f))
            val region = footprintSelected.address.split("\n")[0]
            val location = footprintSelected.address.split("\n").last()
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
