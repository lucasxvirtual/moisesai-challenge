package com.example.moisesaichallenge.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun NoConnectionBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Red)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No internet connection",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
