package io.github.chos1n11111.dongqiudipure.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006D3B),
    secondary = Color(0xFF4D6355),
    tertiary = Color(0xFF755A00),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFF7FAF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF65DC96),
    secondary = Color(0xFFB4CCBB),
    tertiary = Color(0xFFEBC24A),
    background = Color(0xFF101512),
    surface = Color(0xFF101512),
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
