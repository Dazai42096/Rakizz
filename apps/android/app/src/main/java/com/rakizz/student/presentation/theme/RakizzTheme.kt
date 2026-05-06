package com.rakizz.student.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RakizzLightColorScheme = lightColorScheme(
    primary = RakizzColors.Primary,
    onPrimary = RakizzColors.White,

    secondary = RakizzColors.Accent,
    onSecondary = RakizzColors.TextMain,

    background = RakizzColors.Background,
    onBackground = RakizzColors.TextMain,

    surface = RakizzColors.Card,
    onSurface = RakizzColors.TextMain,

    error = RakizzColors.Error,
    onError = RakizzColors.White
)

@Composable
fun RakizzTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RakizzLightColorScheme,
        content = content
    )
}