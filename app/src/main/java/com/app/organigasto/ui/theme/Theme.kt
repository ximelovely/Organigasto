package com.app.organigasto.ui.theme

import android.app.Activity
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
    primary = PurpuraPrimario,
    onPrimary = Color.White,
    secondary = PurpuraClaro,
    onSecondary = Black,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    primaryContainer = PurpuraSecundario,
    onPrimaryContainer = Color.White,
    outline = PurpuraPrimario.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = PurpuraPrimario,
    onPrimary = Color.White,
    secondary = PurpuraSecundario,
    onSecondary = Color.White,
    tertiary = PurpuraClaro,
    background = CremaFondo,
    surface = Color.White,
    onBackground = PurpuraSecundario,
    onSurface = PurpuraSecundario,
    primaryContainer = PurpuraClaro.copy(alpha = 0.3f),
    onPrimaryContainer = PurpuraSecundario,
    outline = PurpuraClaro
)

@Composable
fun OrganigastoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreference: Int = 0, // 0: System, 1: Light, 2: Dark
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        1 -> false
        2 -> true
        else -> darkTheme
    }
    
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
