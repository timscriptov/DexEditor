package com.mcal.common.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

var themeState = mutableStateOf(AppTheme.SYSTEM)

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200,

    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

var isDarkTheme = false

@Composable
fun ApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (themeState.value) {
        AppTheme.SYSTEM -> {
            if (darkTheme) {
                isDarkTheme = true
                DarkColorPalette
            } else {
                isDarkTheme = false
                LightColorPalette
            }
        }

        AppTheme.LIGHT -> {
            isDarkTheme = false
            LightColorPalette
        }

        AppTheme.DARK -> {
            isDarkTheme = true
            DarkColorPalette
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}