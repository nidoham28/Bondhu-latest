package com.nidoham.bondhu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nidoham.bondhu.ui.theme.BondhuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BondhuTheme {
                // Scaffold handles insets, status bar padding, and FAB/snackbar slots.
                // Replace the TODO content with your main screen composable.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}