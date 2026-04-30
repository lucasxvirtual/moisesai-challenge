package com.example.moisesaichallenge.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.moisesaichallenge.presentation.album.AlbumScreen
import com.example.moisesaichallenge.presentation.home.HomeScreen
import com.example.moisesaichallenge.presentation.player.PlayerScreen
import com.example.moisesaichallenge.presentation.home.playlists.create.CreatePlaylistScreen
import com.example.moisesaichallenge.presentation.home.playlists.details.PlaylistDetailScreen
import com.example.moisesaichallenge.presentation.splash.SplashScreen
import com.example.moisesaichallenge.presentation.tablet.TabletLayout

private const val TRANSITION_DURATION = 350

@Composable
fun NavGraph(windowSizeClass: WindowSizeClass) {
    val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Splash,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION)
            )
        }
    ) {
        composable<Splash>(
            enterTransition = { fadeIn(tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION)) }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Home) {
                        popUpTo<Splash> { inclusive = true }
                    }
                }
            )
        }
        composable<Home> {
            if (isTablet) {
                TabletLayout()
            } else {
                HomeScreen(
                    onNavigateToPlayer = { navController.navigate(Player) },
                    onNavigateToAlbum = { collectionId -> navController.navigate(Album(collectionId)) },
                    onNavigateToCreatePlaylist = { navController.navigate(CreatePlaylist()) },
                    onNavigateToPlaylist = { playlistId -> navController.navigate(PlaylistDetail(playlistId)) },
                    onNavigateToEditPlaylist = { playlistId -> navController.navigate(CreatePlaylist(playlistId)) }
                )
            }
        }
        composable<Player> {
            PlayerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAlbum = { collectionId -> navController.navigate(Album(collectionId)) }
            )
        }
        composable<Album> {
            AlbumScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(Player) }
            )
        }
        composable<CreatePlaylist> {
            CreatePlaylistScreen(
                onBack = { navController.popBackStack() },
                onCreated = { newId ->
                    navController.navigate(PlaylistDetail(newId)) {
                        popUpTo<CreatePlaylist> { inclusive = true }
                    }
                }
            )
        }
        composable<PlaylistDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<PlaylistDetail>()
            PlaylistDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate(Player) },
                onNavigateToEdit = { navController.navigate(CreatePlaylist(route.playlistId)) }
            )
        }
    }
}
