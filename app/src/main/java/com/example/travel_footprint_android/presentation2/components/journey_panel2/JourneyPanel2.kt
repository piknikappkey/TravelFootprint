package com.example.travel_footprint_android.presentation2.components.journey_panel2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.headline.Headline
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare
import com.example.travel_footprint_android.ui.theme.BGLight1

@Composable
fun JourneyPanel2(
    modifier: Modifier = Modifier,
    journeySelected: Journey, // 当前选中的旅程
    updateJourney: (Journey) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                clip = false  // 不裁剪阴影，保持默认
            )
            // 2. 背景：将形状传给 background，自动带有圆角
            .background(
                color = BGLight1,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
            )
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f) // 设置最大高度
            .wrapContentHeight() // 根据内容调整高度
            .verticalScroll(rememberScrollState()) // 可滚动
    ) {
        Column {
            // 统一标题
            Headline(
                text = journeySelected.title,
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 10.dp)
            )
            // 图片
            ImageSquare(journeySelected, updateJourney)
            // 描述

            // 回忆
        }
    }
}