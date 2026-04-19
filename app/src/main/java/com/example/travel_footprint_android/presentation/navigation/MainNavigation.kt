//// app/src/main/java/com/example/travel_footprint_android/presentation/navigation/MainNavigation.kt
//package com.example.travel_footprint_android.presentation.navigation
//
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navOptions
//import com.example.travel_footprint_android.presentation.screen.FootprintScreen
//import com.example.travel_footprint_android.presentation.screen.LightenScreen
//import com.example.travel_footprint_android.presentation.screen.JourneyListScreen
//import com.example.travel_footprint_android.presentation.screen.MapScreen
//
//
//@Composable
//fun MainNavigation(
//    navController: NavHostController = rememberNavController()
//) {
//    Scaffold(
//        bottomBar = {
//            BottomNavigationBar(navController = navController)
//        }
//    ) { paddingValues ->
//        NavHost(
//            navController = navController,
//            startDestination = Screen.Journey.route,
//            modifier = Modifier.padding(paddingValues)
//        ) {
//            composable(
//                route = Screen.Lighten.route,
//                enterTransition = { null },
//                exitTransition = { null },
//                popEnterTransition = { null },
//                popExitTransition = { null }
//            ) {
//                LightenScreen()
//            }
//            composable(
//                route = Screen.Footprint.route,
//                enterTransition = { null },
//                exitTransition = { null },
//                popEnterTransition = { null },
//                popExitTransition = { null }
//            ) {
//                FootprintScreen()
//                composable(Screen.Journey.route) {
//                    JourneyListScreen(
//                        onNavigateToMap = { journeyId ->
//                            navController.navigate(Screen.Map.createRoute(journeyId))
//                        }
//                    )
//                }
//                composable(
//                    route = Screen.Map.route,
//                    arguments = Screen.Map.arguments
//                ) { backStackEntry ->
//                    val journeyId = backStackEntry.arguments?.getLong("journeyId") ?: 0L
//                    MapScreen(
//                        journeyId = journeyId,
//                        onNavigateBack = { navController.popBackStack() }
//                    )
//                }
//            }
//        }
//    }
//}