package com.example.moisesaichallenge.presentation.tablet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.moisesaichallenge.navigation.Album
import com.example.moisesaichallenge.navigation.CreatePlaylist
import com.example.moisesaichallenge.navigation.Home
import com.example.moisesaichallenge.navigation.PlaylistDetail
import com.example.moisesaichallenge.presentation.album.AlbumScreen
import com.example.moisesaichallenge.presentation.home.HomeScreen
import com.example.moisesaichallenge.presentation.home.playlists.create.CreatePlaylistScreen
import com.example.moisesaichallenge.presentation.home.playlists.details.PlaylistDetailScreen
import com.example.moisesaichallenge.presentation.player.PlayerPane

@Composable
fun TabletLayout() {
    val leftNavController = rememberNavController()

    Row(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = leftNavController,
            startDestination = Home,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            composable<Home> {
                HomeScreen(
                    onNavigateToPlayer = { /* player always visible on the right */ },
                    onNavigateToAlbum = { id -> leftNavController.navigate(Album(id)) },
                    onNavigateToCreatePlaylist = { leftNavController.navigate(CreatePlaylist()) },
                    onNavigateToPlaylist = { id -> leftNavController.navigate(PlaylistDetail(id)) },
                    onNavigateToEditPlaylist = { id -> leftNavController.navigate(CreatePlaylist(id)) }
                )
            }
            composable<Album> {
                AlbumScreen(
                    onBack = { leftNavController.popBackStack() },
                    onNavigateToPlayer = { /* no-op */ }
                )
            }
            composable<CreatePlaylist> {
                CreatePlaylistScreen(
                    onBack = { leftNavController.popBackStack() },
                    onCreated = { newId ->
                        leftNavController.navigate(PlaylistDetail(newId)) {
                            popUpTo<CreatePlaylist> { inclusive = true }
                        }
                    }
                )
            }
            composable<PlaylistDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<PlaylistDetail>()
                PlaylistDetailScreen(
                    onBack = { leftNavController.popBackStack() },
                    onNavigateToPlayer = { /* no-op */ },
                    onNavigateToEdit = { leftNavController.navigate(CreatePlaylist(route.playlistId)) }
                )
            }
        }

        VerticalDivider()

        PlayerPane(
            onNavigateToAlbum = { id -> leftNavController.navigate(Album(id)) },
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        )
    }
}
