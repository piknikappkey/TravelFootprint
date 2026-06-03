package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween

@Composable
fun JourneyDetailCoverSection(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
) {
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
