package com.nidoham.bondhu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nidoham.bondhu.ui.theme.BondhuTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BondhuTheme {
                // Navigation is owned by the Activity, not the composable.
                SplashScreen(
                    onSplashComplete = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Full-screen splash shown at launch.
 *
 * @param onSplashComplete Called after the splash delay; the caller is responsible
 *   for any navigation. Defaults to a no-op so previews render without errors.
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit = {}) {
    LaunchedEffect(Unit) {
        delay(1_500L)
        onSplashComplete()
    }

    // Surface applies the theme background and ensures onBackground text colors work
    // correctly in both light and dark mode.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Bondhu logo",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-32).dp)
            ) {
                Text(
                    text = "Developed by",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
                Text(
                    text = "NidoHam",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// Preview is on a separate private function so the production composable
// stays free of preview-only concerns, and the @Preview never triggers
// the LaunchedEffect navigation by default (onSplashComplete = {}).
@Preview(showBackground = true, name = "Splash – Light")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Splash – Dark")
@Composable
private fun SplashScreenPreview() {
    BondhuTheme {
        SplashScreen()
    }
}