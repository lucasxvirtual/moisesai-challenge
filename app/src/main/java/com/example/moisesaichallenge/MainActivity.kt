package com.example.moisesaichallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moisesaichallenge.core.network.ConnectionStatusProvider
import com.example.moisesaichallenge.navigation.NavGraph
import com.example.moisesaichallenge.presentation.components.NoConnectionBanner
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var connectionStatusProvider: ConnectionStatusProvider

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            MoisesaiChallengeTheme {
                val isOnline by connectionStatusProvider.isOnline.collectAsState()
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(windowSizeClass = windowSizeClass)
                    NoConnectionBanner(isOnline = isOnline)
                }
            }
        }
    }
}
