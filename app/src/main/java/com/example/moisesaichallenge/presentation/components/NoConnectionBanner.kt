package com.example.moisesaichallenge.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.moisesaichallenge.R
import kotlinx.coroutines.delay

private val GreenConnected = Color(0xFF388E3C)
private const val CONNECTED_DISPLAY_MS = 2000L

@Composable
fun NoConnectionBanner(isOnline: Boolean, modifier: Modifier = Modifier) {
    var showBanner by remember { mutableStateOf(!isOnline) }
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            isConnected = false
            showBanner = true
        } else if (showBanner) {
            isConnected = true
            delay(CONNECTED_DISPLAY_MS)
            showBanner = false
        }
    }

    AnimatedVisibility(
        visible = showBanner,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(if (isConnected) GreenConnected else Color.Red)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isConnected) stringResource(R.string.status_connected) else stringResource(R.string.status_no_internet),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}
