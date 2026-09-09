package com.app.organigasto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PurpuraPrimario,
    onPrimary = Color.White,
    secondary = PurpuraSecundario,
    onSecondary = Color.White,
    tertiary = PurpuraClaro,
    background = CremaFondo,
    surface = White,
    onBackground = PurpuraSecundario,
    onSurface = PurpuraSecundario,
)

@Composable
fun OrganigastoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
