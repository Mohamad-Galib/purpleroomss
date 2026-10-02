package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleSubtle,
    onPrimaryContainer = PurplePrimary,
    secondary = PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = PurpleBorder,
    onSecondaryContainer = PurpleDark,
    tertiary = VerifiedGreen,
    onTertiary = Color.White,
    background = BackgroundLavender,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF3F0FA),
    onSurfaceVariant = TextSecondary,
    outline = PurpleBorder,
    outlineVariant = Color(0xFFE2DCF0),
    error = AlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
