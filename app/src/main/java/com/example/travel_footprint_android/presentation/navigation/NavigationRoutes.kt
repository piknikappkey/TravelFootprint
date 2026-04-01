package com.example.travel_footprint_android.presentation.navigation

sealed class Screen(val route: String) {
    object Lighten : Screen("lighten")
    object Footprint : Screen("footprint")
}
