package com.app.organigasto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpuraPrimario,
    onPrimary = Color.White,
    secondary = PurpuraClaro,
    onSecondary = Black,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
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
)

@Composable
fun OrganigastoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
