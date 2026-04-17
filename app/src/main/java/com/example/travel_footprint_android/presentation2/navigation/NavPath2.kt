package com.example.travel_footprint_android.presentation2.navigation

import com.example.travel_footprint_android.R

class NavPath2(
    val name: String,
    val icon: Int,
)

object NavPathObj2 {
    val lighten = NavPath2("点亮", R.drawable.ic_journey_nav_item)
    val journey = NavPath2("旅程", R.drawable.ic_light_nav_item)
    val list = listOf(lighten, journey)
}