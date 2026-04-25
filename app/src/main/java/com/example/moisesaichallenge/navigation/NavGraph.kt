package com.example.moisesaichallenge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moisesaichallenge.presentation.album.AlbumScreen
import com.example.moisesaichallenge.presentation.home.HomeScreen
import com.example.moisesaichallenge.presentation.player.PlayerScreen
import com.example.moisesaichallenge.presentation.splash.SplashScreen

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_HOME = "home"
private const val ROUTE_PLAYER = "player"
private const val ROUTE_ALBUM = "album/{collectionId}"

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
            HomeScreen(
                onNavigateToPlayer = { navController.navigate(ROUTE_PLAYER) },
                onNavigateToAlbum = { collectionId -> navController.navigate("album/$collectionId") }
            )
        }
        composable(ROUTE_PLAYER) {
            PlayerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAlbum = { collectionId -> navController.navigate("album/$collectionId") }
            )
        }
        composable(
            route = ROUTE_ALBUM,
            arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
        ) {
            AlbumScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(ROUTE_PLAYER) }
            )
        }
    }
}
