package com.example.fluentread.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Dark color scheme based on Fluent Design principles.
 * Adjust colors as needed to fit your design requirements.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BlueAccent,
    secondary = BlueAccentDark,
    tertiary = SuccessGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

/**
 * Light color scheme based on Fluent Design principles.
 * Adjust colors as needed to fit your design requirements.
 */
private val LightColorScheme = lightColorScheme(
    primary = BlueAccent,
    secondary = BlueAccentDark,
    tertiary = SuccessGreen,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

/**
 * Composable function to apply the FluentRead theme.
 *
 * @param darkTheme Boolean flag to toggle between dark and light themes. Defaults to system setting.
 * @param content Composable content that will be styled with the theme.
 */
@Composable
fun FluentReadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
