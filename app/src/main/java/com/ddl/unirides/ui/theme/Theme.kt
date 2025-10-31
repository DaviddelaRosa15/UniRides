package com.ddl.unirides.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = TextPrimaryDark,

    secondary = SecondaryGreenLight,
    onSecondary = TextPrimary,
    secondaryContainer = SecondaryGreenDark,
    onSecondaryContainer = TextPrimaryDark,

    tertiary = AccentOrangeLight,
    onTertiary = TextPrimary,
    tertiaryContainer = AccentOrangeDark,
    onTertiaryContainer = TextPrimaryDark,

    error = ErrorRed,
    onError = TextPrimaryDark,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = TextSecondaryDark,

    outline = DividerDark,
    outlineVariant = Color(0xFF4A4A4A)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark,

    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = SecondaryGreenLight,
    onSecondaryContainer = SecondaryGreenDark,

    tertiary = AccentOrange,
    onTertiary = Color.White,
    tertiaryContainer = AccentOrangeLight,
    onTertiaryContainer = AccentOrangeDark,

    error = ErrorRed,
    onError = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = TextSecondary,

    outline = DividerLight,
    outlineVariant = Color(0xFFCACACA)
)

@Composable
fun UniRidesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está deshabilitado para usar nuestra paleta personalizada
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalView.current.context
            if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(context)
            else androidx.compose.material3.dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}