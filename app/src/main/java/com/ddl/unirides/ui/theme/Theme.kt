package com.ddl.unirides.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores principal (dark theme - verde oscuro)
private val UniRidesColorScheme = darkColorScheme(
    primary = AccentCyan,                    // Turquesa para botones y acciones
    onPrimary = TextOnAccent,                // Texto oscuro sobre turquesa
    primaryContainer = PrimaryGreenLight,    // Containers
    onPrimaryContainer = TextPrimary,        // Texto blanco sobre containers

    secondary = AccentCyanLight,
    onSecondary = TextOnAccent,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = TextPrimary,

    tertiary = AccentCyanDark,
    onTertiary = TextPrimary,

    error = ErrorRed,
    onError = TextPrimary,

    background = BackgroundDark,             // Verde oscuro #1A2F26
    onBackground = TextPrimary,              // Texto blanco

    surface = SurfaceDark,                   // Verde medio #2D4A3E
    onSurface = TextPrimary,                 // Texto blanco
    surfaceVariant = SurfaceVariant,         // #3D5A4D
    onSurfaceVariant = TextSecondary,        // Gris claro

    outline = OutlineColor,
    outlineVariant = DividerColor
)

@Composable
fun UniRidesTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = UniRidesColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}