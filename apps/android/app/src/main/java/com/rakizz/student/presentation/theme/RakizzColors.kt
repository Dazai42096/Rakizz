package com.rakizz.student.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class RakizzThemeMode {
    LIGHT,
    DARK
}

data class RakizzPalette(
    val background: Color,
    val backgroundSoft: Color,
    val card: Color,
    val cardSoft: Color,
    val cardBorder: Color,
    val primary: Color,
    val primaryDark: Color,
    val primarySoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val textMain: Color,
    val textSecond: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val white: Color
)

private val RakizzDarkPalette = RakizzPalette(
    background = Color(0xFF000000),
    backgroundSoft = Color(0xFF020817),

    card = Color(0xFF171717),
    cardSoft = Color(0xFF1B1F27),
    cardBorder = Color(0xFF2A2F3A),

    primary = Color(0xFF2457D6),
    primaryDark = Color(0xFF0C2F80),
    primarySoft = Color(0xFF0E254F),

    accent = Color(0xFF6EA7FF),
    accentSoft = Color(0xFF10264F),

    textMain = Color(0xFFF7F8FA),
    textSecond = Color(0xFF9AA3B2),
    textMuted = Color(0xFF667085),

    success = Color(0xFF30D158),
    warning = Color(0xFFF5B301),
    error = Color(0xFFFF3B30),

    white = Color(0xFFFFFFFF)
)

private val RakizzLightPalette = RakizzPalette(
    background = Color(0xFFF7FBFF),
    backgroundSoft = Color(0xFFEAF4FF),

    card = Color(0xFFFFFFFF),
    cardSoft = Color(0xFFF1F7FF),
    cardBorder = Color(0xFFD6E5F7),

    primary = Color(0xFF1976D2),
    primaryDark = Color(0xFF063B7A),
    primarySoft = Color(0xFFDCEEFF),

    accent = Color(0xFF37B6F2),
    accentSoft = Color(0xFFE8F5FF),

    textMain = Color(0xFF0B2A4A),
    textSecond = Color(0xFF41637F),
    textMuted = Color(0xFF7890A8),

    success = Color(0xFF18A058),
    warning = Color(0xFFF2A900),
    error = Color(0xFFD32F2F),

    white = Color(0xFFFFFFFF)
)

object RakizzThemeController {

    var mode by mutableStateOf(RakizzThemeMode.DARK)
        private set

    fun isCurrentlyDark(): Boolean {
        return mode == RakizzThemeMode.DARK
    }

    fun toggleTheme() {
        mode = if (mode == RakizzThemeMode.DARK) {
            RakizzThemeMode.LIGHT
        } else {
            RakizzThemeMode.DARK
        }
    }

    fun setLight() {
        mode = RakizzThemeMode.LIGHT
    }

    fun setDark() {
        mode = RakizzThemeMode.DARK
    }
}

object RakizzColors {

    private val palette: RakizzPalette
        get() {
            return if (RakizzThemeController.isCurrentlyDark()) {
                RakizzDarkPalette
            } else {
                RakizzLightPalette
            }
        }

    val Background: Color
        get() = palette.background

    val BackgroundSoft: Color
        get() = palette.backgroundSoft

    val Card: Color
        get() = palette.card

    val CardSoft: Color
        get() = palette.cardSoft

    val CardBorder: Color
        get() = palette.cardBorder

    val Primary: Color
        get() = palette.primary

    val PrimaryDark: Color
        get() = palette.primaryDark

    val PrimarySoft: Color
        get() = palette.primarySoft

    val Accent: Color
        get() = palette.accent

    val AccentSoft: Color
        get() = palette.accentSoft

    val TextMain: Color
        get() = palette.textMain

    val TextSecond: Color
        get() = palette.textSecond

    val TextMuted: Color
        get() = palette.textMuted

    val Success: Color
        get() = palette.success

    val Warning: Color
        get() = palette.warning

    val Error: Color
        get() = palette.error

    val White: Color
        get() = palette.white
}

@Composable
fun RakizzTheme(
    darkTheme: Boolean = RakizzThemeController.isCurrentlyDark(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) {
        RakizzDarkPalette
    } else {
        RakizzLightPalette
    }

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.white,
            secondary = palette.accent,
            onSecondary = palette.white,
            background = palette.background,
            onBackground = palette.textMain,
            surface = palette.card,
            onSurface = palette.textMain,
            error = palette.error,
            onError = palette.white
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.white,
            secondary = palette.accent,
            onSecondary = palette.white,
            background = palette.background,
            onBackground = palette.textMain,
            surface = palette.card,
            onSurface = palette.textMain,
            error = palette.error,
            onError = palette.white
        )
    }

    MaterialTheme(
        colorScheme = materialColors,
        content = content
    )
}