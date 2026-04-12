package com.example.travel_footprint_android.presentation2.navigation

//enum class NavPath2 {
//    LIGHTEN,
//    JOURNEY
//}

class NavPath2(
    val name: String,
)

object NavPathObj2 {
    val lighten = NavPath2("点亮")
    val journey = NavPath2("旅程")
    val list = listOf(lighten, journey)
}