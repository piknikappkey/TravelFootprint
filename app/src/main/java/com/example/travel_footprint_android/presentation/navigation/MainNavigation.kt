package com.example.travel_footprint_android.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travel_footprint_android.presentation.screen.FootprintScreen
import com.example.travel_footprint_android.presentation.screen.LightenScreen

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Lighten.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Lighten.route) {
                LightenScreen()
            }
            composable(Screen.Footprint.route) {
                FootprintScreen()
            }
        }
    }
}
