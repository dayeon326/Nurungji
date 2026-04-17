package com.example.nurungji.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,

    secondary = SecondaryGreen,
    onSecondary = TextPrimary,

    tertiary = AccentYellow,
    onTertiary = TextPrimary,

    background = TextPrimary,
    onBackground = BackgroundCream,

    surface = Color(0xFF2F3F2F),
    onSurface = BackgroundCream,

    error = DestructiveRed,
    onError = Color.White,

    outline = BorderGreen
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,

    secondary = SecondaryGreen,
    onSecondary = TextPrimary,

    tertiary = AccentYellow,
    onTertiary = TextPrimary,

    background = BackgroundCream,
    onBackground = TextPrimary,

    surface = CardWhite,
    onSurface = TextPrimary,

    error = DestructiveRed,
    onError = Color.White,

    outline = BorderGreen
)

@Composable
fun NurungjiTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}