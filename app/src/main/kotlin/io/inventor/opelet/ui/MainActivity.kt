package io.inventor.opelet.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import io.inventor.opelet.ui.theme.OpeletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Disable the contrast-enforced scrim on the navigation bar.
        // Without this, 3-button nav gets a translucent overlay even
        // when we explicitly set the bar to transparent.
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val darkTheme = isSystemInDarkTheme()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                )
                onDispose {}
            }

            OpeletTheme(darkTheme = darkTheme) {
                // Background fills the entire window (behind system bars).
                // Content is inset so it doesn't overlap status/nav bar text.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OpeletTheme.colors.background),
                ) {
                    OpeletNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding(),
                    )
                }
            }
        }
    }
}
