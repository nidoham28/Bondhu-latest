package com.nidoham.bondhu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nidoham.bondhu.supabase.auth.AuthRepository
import com.nidoham.bondhu.ui.screen.HomeScreen
import com.nidoham.bondhu.ui.screen.LoginScreen
import com.nidoham.bondhu.ui.theme.BondhuTheme
import com.nidoham.bondhu.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val supabase       = (application as BondhuApp).supabase
        val authRepository = AuthRepository(supabase)

        setContent {
            BondhuTheme {
                val viewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.factory(authRepository)
                )

                // Source of truth — Supabase session drives which screen is shown
                val isLoggedIn by authRepository.sessionState
                    .collectAsStateWithLifecycle(initialValue = false)

                var showContent by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(300.milliseconds)
                    showContent = true
                }

                if (showContent) {
                    AnimatedContent(
                        targetState = isLoggedIn,
                        transitionSpec = { fadeIn(tween(400)).togetherWith(fadeOut(tween(400))) },
                        label = "auth_home_transition"
                    ) { loggedIn ->
                        if (loggedIn) {
                            HomeScreen()
                        } else {
                            LoginScreen(
                                viewModel = viewModel,
                                // ✅ No-op: when signIn() succeeds, Supabase updates
                                // its session → isLoggedIn flips to true →
                                // AnimatedContent recomposes and shows HomeScreen.
                                // Calling a @Composable here would be a compile error.
                                onNavigateToHome = {}
                            )
                        }
                    }
                }
            }
        }
    }
}