package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomLightColorScheme = lightColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    secondary = SecondaryText,
    onSecondary = Color.White,
    background = AppBackground,
    onBackground = PrimaryText,
    surface = MainSurface,
    onSurface = PrimaryText,
    surfaceVariant = SecondarySurface,
    onSurfaceVariant = PrimaryText,
    outline = BorderColor,
    error = StatusOverdue
)

private val CustomDarkColorScheme = darkColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    secondary = SecondaryText,
    onSecondary = Color.White,
    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF2D2D2D),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF444444),
    error = StatusOverdue
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
