package com.nidoham.bondhu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---------- Light Color Scheme ----------
private val LightColorScheme = lightColorScheme(
    // Primary
    primary                = TelegramBlue,
    onPrimary              = OnPrimary,
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = OnPrimaryContainer,
    // Secondary
    secondary              = Secondary,
    onSecondary            = OnSecondary,
    secondaryContainer     = SecondaryContainer,
    onSecondaryContainer   = OnSecondaryContainer,
    // Tertiary
    tertiary               = Tertiary,
    onTertiary             = OnTertiary,
    tertiaryContainer      = TertiaryContainer,
    onTertiaryContainer    = OnTertiaryContainer,
    // Error
    error                  = Error,
    onError                = OnError,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,
    // Backgrounds & Surfaces
    background             = Background,
    onBackground           = OnBackground,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceVariant,
    onSurfaceVariant       = OnSurfaceVariant,
    // Outlines
    outline                = Outline,
    outlineVariant         = OutlineVariant,
)

// ---------- Dark Color Scheme ----------
private val DarkColorScheme = darkColorScheme(
    // Primary
    primary                = DarkTelegramBlue,
    onPrimary              = DarkOnPrimary,
    primaryContainer       = DarkPrimaryContainer,
    onPrimaryContainer     = DarkOnPrimaryContainer,
    // Secondary
    secondary              = DarkSecondary,
    onSecondary            = DarkOnSecondary,
    secondaryContainer     = DarkSecondaryContainer,
    onSecondaryContainer   = DarkOnSecondaryContainer,
    // Tertiary
    tertiary               = DarkTertiary,
    onTertiary             = DarkOnTertiary,
    tertiaryContainer      = DarkTertiaryContainer,
    onTertiaryContainer    = DarkOnTertiaryContainer,
    // Error
    error                  = DarkError,
    onError                = DarkOnError,
    errorContainer         = DarkErrorContainer,
    onErrorContainer       = DarkOnErrorContainer,
    // Backgrounds & Surfaces
    background             = DarkBackground,
    onBackground           = DarkOnBackground,
    surface                = DarkSurface,
    onSurface              = DarkOnSurface,
    surfaceVariant         = DarkSurfaceVariant,
    onSurfaceVariant       = DarkOnSurfaceVariant,
    // Outlines
    outline                = DarkOutline,
    outlineVariant         = DarkOutlineVariant,
)

@Composable
fun BondhuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Android 12+) is disabled by default so the custom
    // Telegram-blue palette is always respected. Pass true to let the system
    // wallpaper colours take over on supported devices.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Sync the status bar colour with the surface colour so the app feels
    // fully edge-to-edge and consistent with Material3 guidelines.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}