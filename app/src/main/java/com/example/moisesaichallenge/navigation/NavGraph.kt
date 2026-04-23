package com.example.moisesaichallenge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moisesaichallenge.presentation.home.HomeScreen
import com.example.moisesaichallenge.presentation.splash.SplashScreen

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_HOME = "home"

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_SPLASH) {
        composable(ROUTE_SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_HOME) {
            HomeScreen()
        }
    }
}
