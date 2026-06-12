package com.nidoham.bondhu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nidoham.bondhu.ui.theme.BondhuTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install the splash screen BEFORE super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            BondhuTheme {
                // 2. Optional: keep the splash background for a brief moment,
                // then fade in your main content.
                var showContent by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(300.milliseconds) // a little longer than the system animation
                    showContent = true
                }

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                        // Your main screen goes here
                        // MainScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}