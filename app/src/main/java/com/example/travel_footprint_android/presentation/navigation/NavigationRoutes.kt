// app/src/main/java/com/example/travel_footprint_android/presentation/navigation/NavigationRoutes.kt
package com.example.travel_footprint_android.presentation.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Journey : Screen("journey")
    object Map : Screen("map/{journeyId}") {
        val arguments = listOf(
            navArgument("journeyId") { type = NavType.LongType }
        )

        fun createRoute(journeyId: Long): String = "map/$journeyId"
    }
}