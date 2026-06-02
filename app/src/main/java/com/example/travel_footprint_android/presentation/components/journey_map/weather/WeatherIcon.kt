package com.example.travel_footprint_android.presentation.components.journey_map.weather

import androidx.compose.runtime.Composable

@Composable
fun weatherIcon(weather: String): String {
    return when {
        weather.contains("晴") -> "\u2600\uFE0F"
        weather.contains("云") -> "\u26C5"
        weather.contains("阴") -> "\u2601\uFE0F"
        weather.contains("雨") -> "\uD83C\uDF27\uFE0F"
        weather.contains("雪") -> "\u2744\uFE0F"
        weather.contains("雾") || weather.contains("霾") -> "\uD83C\uDF2B\uFE0F"
        weather.contains("风") -> "\uD83D\uDCA8"
        weather.contains("雷") -> "\u26C8\uFE0F"
        weather.contains("雹") -> "\uD83C\uDF26\uFE0F"
        else -> "\u2600\uFE0F"
    }
}
