package com.example.moisesaichallenge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color.Black,
    surface = Color.Black,
    background = Color.Black,
    onSurface = Color.White,
    onSurfaceVariant = Gray,
    surfaceContainer = Black10,
    primaryContainer = White25
)

@Composable
fun MoisesaiChallengeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}