package com.ddl.unirides.ui.theme

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

// Esquema de colores - Tema Claro
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,                    // Azul brillante para botones y acciones
    onPrimary = TextOnPrimary,                // Blanco sobre azul
    primaryContainer = BlueLight,             // Azul muy claro para containers
    onPrimaryContainer = BluePrimaryDark,     // Azul oscuro sobre containers claros

    secondary = BlueAccent,
    onSecondary = TextOnPrimary,
    secondaryContainer = BlueLight,
    onSecondaryContainer = BluePrimaryDark,

    tertiary = BluePrimaryLight,
    onTertiary = TextOnPrimary,

    error = ErrorRed,
    onError = TextOnPrimary,

    background = BackgroundLight,             // Gris muy claro
    onBackground = TextPrimaryLight,          // Negro/gris oscuro

    surface = SurfaceLight,                   // Blanco puro
    onSurface = TextPrimaryLight,             // Negro/gris oscuro
    surfaceVariant = SurfaceVariantLight,     // Gris claro
    onSurfaceVariant = TextSecondaryLight,    // Gris medio

    outline = OutlineLight,
    outlineVariant = DividerLight
)

// Esquema de colores - Tema Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,                    // Azul brillante para botones y acciones
    onPrimary = TextOnPrimary,                // Blanco sobre azul
    primaryContainer = BlueLightDark,         // Azul oscuro para containers
    onPrimaryContainer = BluePrimaryLight,    // Azul claro sobre containers oscuros

    secondary = BlueAccent,
    onSecondary = TextOnPrimary,
    secondaryContainer = BlueLightDark,
    onSecondaryContainer = BluePrimaryLight,

    tertiary = BluePrimaryLight,
    onTertiary = TextPrimaryDark,

    error = ErrorRed,
    onError = TextOnPrimary,

    background = BackgroundDark,              // Negro/gris muy oscuro
    onBackground = TextPrimaryDark,           // Blanco

    surface = SurfaceDark,                    // Gris oscuro
    onSurface = TextPrimaryDark,              // Blanco
    surfaceVariant = SurfaceVariantDark,      // Gris oscuro variante
    onSurfaceVariant = TextSecondaryDark,     // Gris claro

    outline = OutlineDark,
    outlineVariant = DividerDark
)

@Composable
fun UniRidesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está disponible en Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}