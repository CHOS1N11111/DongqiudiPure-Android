package io.github.chos1n11111.dongqiudipure.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006D3B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF4D6355),
    secondaryContainer = Color(0xFFD0E8D7),
    onSecondaryContainer = Color(0xFF0A2012),
    tertiary = Color(0xFF755A00),
    background = Color(0xFFF7FAF7),
    onBackground = Color(0xFF181D19),
    surface = Color(0xFFF7FAF7),
    onSurface = Color(0xFF181D19),
    surfaceContainer = Color(0xFFEDF2ED),
    surfaceContainerHigh = Color(0xFFE6ECE6),
    onSurfaceVariant = Color(0xFF414942),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF65DC96),
    onPrimary = Color(0xFF00391A),
    secondary = Color(0xFFB4CCBB),
    secondaryContainer = Color(0xFF354B3D),
    onSecondaryContainer = Color(0xFFD0E8D7),
    tertiary = Color(0xFFEBC24A),
    background = Color(0xFF101512),
    onBackground = Color(0xFFE0E4DF),
    surface = Color(0xFF101512),
    onSurface = Color(0xFFE0E4DF),
    surfaceContainer = Color(0xFF1C211D),
    surfaceContainerHigh = Color(0xFF262B27),
    onSurfaceVariant = Color(0xFFC0C9C0),
)

@Composable
fun DongqiudiPureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
