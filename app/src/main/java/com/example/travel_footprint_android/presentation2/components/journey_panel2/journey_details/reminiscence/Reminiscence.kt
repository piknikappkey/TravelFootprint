package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.reminiscence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Reminiscence(
    journey: Journey,
    updateJourney: (Journey) -> Unit
) {
    val imgSize = 80.dp
    val aspectRatio = 1f
    val iconSize = .4f
    val elevation = 2.dp
    val shape = RoundedCornerShape(5.dp)
    val delIconSize = 15.dp
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),  // 水平间距
            verticalArrangement = Arrangement.spacedBy(4.dp)     // 垂直行间距
        ) {
            journey.journeyImagePaths.forEach { imgPath ->
                Box(
                    modifier = Modifier
                        .height(imgSize)
                        .width(imgSize)
                ) {
                    ImageSquare2(
                        imgPath = imgPath,
                        updateImgPath = { file ->  },
                        deleteImgPath = { imgPath ->
                            journey.journeyImagePaths -= imgPath
                            updateJourney(journey)
                        },
                        modifier = Modifier.padding(horizontal = 5.dp),
                        aspectRatio = aspectRatio,
                        addIconSize = iconSize,
                        elevation = elevation,
                        shape = shape,
                        delIconSize = delIconSize
                    )
                }
            }
            Box(
                modifier = Modifier
                    .height(imgSize)
                    .width(imgSize)
            ) {
                ImageSquare2(
                    imgPath = "",
                    updateImgPath = { file ->
                        journey.journeyImagePaths += file.absolutePath
                        updateJourney(journey)
                    },
                    deleteImgPath = { imgPath -> },
                    modifier = Modifier.padding(horizontal = 5.dp),
                    aspectRatio = aspectRatio,
                    addIconSize = iconSize,
                    elevation = elevation,
                    shape = shape,
                    delIconSize = delIconSize
                )
            }
        }
    }
}