package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.cover

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import java.io.File

@Composable
fun JourneyEditCover(
    journey: Journey,
    updateImgPath: (File) -> File?,
    deleteImgPath: (String) -> Unit,
) {
    // 封面图片编辑
    TextMedium(
        text = "封面：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    Spacer(Modifier.padding(2.dp))
    ImageSquare2(
        imgPath = journey.coverImagePath,
        updateImgPath = updateImgPath,
        deleteImgPath = deleteImgPath,
        modifier = Modifier.padding(horizontal = 60.dp),
        aspectRatio = 1.2f,
        addIconSize = .3f,
        showDelIcon = true,
    )
}