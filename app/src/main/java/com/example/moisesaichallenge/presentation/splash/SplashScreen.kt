package com.example.moisesaichallenge.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moisesaichallenge.R
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1500L

private val SplashGradientStart = Color(0xFF0086A0)
private val SplashGradientEnd = Color(0xFF000000)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onSplashFinished()
    }

    val diagonalGradientBrush = Brush.linearGradient(
        colors = listOf(
            SplashGradientStart,
            SplashGradientEnd,
            SplashGradientEnd
        ),
        start = Offset(Float.POSITIVE_INFINITY, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(diagonalGradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier.size(100.dp),
            tint = Color.Unspecified
        )
    }
}
