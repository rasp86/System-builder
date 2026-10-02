package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = TerminalBackground,
    primaryContainer = CyberCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyberGreen,
    onSecondary = TerminalBackground,
    secondaryContainer = CyberGreenDim,
    onSecondaryContainer = TextPrimary,
    tertiary = CyberAmber,
    onTertiary = TerminalBackground,
    background = TerminalBackground,
    onBackground = TextPrimary,
    surface = TerminalCard,
    onSurface = TextPrimary,
    surfaceVariant = TerminalCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = TerminalCardBorder,
    error = CyberPink,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
