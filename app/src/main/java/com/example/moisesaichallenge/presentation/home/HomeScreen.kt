package com.example.moisesaichallenge.presentation.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moisesaichallenge.presentation.home.playlists.list.PlaylistsScreen
import com.example.moisesaichallenge.presentation.home.search.SearchScreen
import kotlinx.coroutines.launch

private enum class HomeTab { Search, Playlists }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToCreatePlaylist: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToEditPlaylist: (Long) -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(HomeTab.Search.ordinal) }
    val selectedTab = HomeTab.entries[selectedTabIndex]
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (selectedTab) {
                HomeTab.Search -> TopAppBar(
                    title = { Text("Songs", style = MaterialTheme.typography.displayMedium) },
                    actions = {
                        if (!isSearchVisible) {
                            IconButton(onClick = { isSearchVisible = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HomeTab.Playlists -> TopAppBar(
                    title = { Text("Playlists", style = MaterialTheme.typography.displayMedium) },
                    actions = {
                        IconButton(onClick = onNavigateToCreatePlaylist) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create playlist",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Search,
                    onClick = { selectedTabIndex = HomeTab.Search.ordinal },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.surface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Playlists,
                    onClick = { selectedTabIndex = HomeTab.Playlists.ordinal },
                    icon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
                    label = { Text("Playlists") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.surface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            HomeTab.Search -> SearchScreen(
                innerPadding = innerPadding,
                isSearchVisible = isSearchVisible,
                onHideSearch = { isSearchVisible = false },
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToAlbum = onNavigateToAlbum,
                onShowSnackbar = { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
            )
            HomeTab.Playlists -> PlaylistsScreen(
                innerPadding = innerPadding,
                onPlaylistClick = onNavigateToPlaylist,
                onEditPlaylist = onNavigateToEditPlaylist
            )
        }
    }
}
