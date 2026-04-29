package com.example.travel_footprint_android.presentation2.components.journey_panel2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.medium_text.MediumText
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
                clip = false
            )
            .background(
                color = BGLight1,
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
            )
        // 移除原有的 heightIn 和 wrapContentHeight，交由内部 Column 管理高度
    ) {
        // 内部用 Column 分隔固定标题和可滚动区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f)   // 限制面板最大高度，内容少时自适应
        ) {
            // 固定标题
            Headline(
                text = journeySelected.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 10.dp)
            )
            // 可滚动内容
            Column(
                modifier = Modifier
                    .weight(1f) // 填充剩余高度
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.padding(10.dp))
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
                    modifier = Modifier.padding(horizontal = 20.dp),
                    aspectRatio = 1.2f,
                    addIconSize = .3f
                )
                Spacer(Modifier.padding(10.dp))
                // 描述内容
                MediumText(
                    text = journeySelected.description,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
                Spacer(Modifier.padding(10.dp))
                // 图集
                Reminiscence(
                    journey = journeySelected,
                    updateJourney = updateJourney
                )
            }
        }
    }
}