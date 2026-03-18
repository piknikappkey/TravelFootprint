// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/MapState.kt
package com.example.travel_footprint_android.presentation.viewmodel

import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.domain.service.HandDrawStyle

data class MapState(
    val isLoading: Boolean = false,
    val journey: Journey? = null,
    val footprints: List<Footprint> = emptyList(),
    val selectedFootprint: Footprint? = null,
    val cameraPosition: CameraPosition = CameraPosition(),
    val mapStyle: HandDrawStyle = HandDrawStyle.WATERCOLOR,
    val isTrailVisible: Boolean = true,
    val error: String? = null
)

data class CameraPosition(
    val latitude: Double = 39.9042,
    val longitude: Double = 116.4074,
    val zoom: Float = 12f,
    val tilt: Float = 0f,
    val bearing: Float = 0f
)

data class GalleryState(
    val isLoading: Boolean = false,
    val photos: List<MediaItem> = emptyList(),
    val selectedPhoto: MediaItem? = null,
    val groupedByJourney: Map<Long, List<MediaItem>> = emptyMap(),
    val error: String? = null
)

data class MediaItem(
    val id: Long,
    val path: String,
    val thumbnailPath: String,
    val journeyId: Long,
    val journeyTitle: String,
    val footprintId: Long,
    val createTime: String,
    val caption: String
)