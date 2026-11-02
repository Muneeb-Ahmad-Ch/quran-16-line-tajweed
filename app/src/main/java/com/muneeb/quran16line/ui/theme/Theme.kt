package com.muneeb.quran16line.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5F4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E8DF),
    onPrimaryContainer = Color(0xFF0B2A20),
    secondaryContainer = Color(0xFFEEF2EE),
    background = Color(0xFFFBF8F1),
    onBackground = Color(0xFF1D1D1D),
    surface = Color(0xFFFBF8F1),
    onSurface = Color(0xFF1D1D1D),
    surfaceVariant = Color(0xFFF1ECE0),
    onSurfaceVariant = Color(0xFF6E685C),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF6F2E8),
    surfaceContainerHigh = Color(0xFFF1ECE0),
    outlineVariant = Color(0xFFE7E1D3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FC3A0),
    onPrimary = Color(0xFF0D1A14),
    primaryContainer = Color(0xFF1E3A2F),
    onPrimaryContainer = Color(0xFFCBEBDC),
    secondaryContainer = Color(0xFF233028),
    background = Color(0xFF121412),
    onBackground = Color(0xFFE9E9E6),
    surface = Color(0xFF121412),
    onSurface = Color(0xFFE9E9E6),
    surfaceVariant = Color(0xFF1C1F1C),
    onSurfaceVariant = Color(0xFF9A9E98),
    surfaceContainerLow = Color(0xFF1A1D1A),
    surfaceContainer = Color(0xFF1C1F1C),
    surfaceContainerHigh = Color(0xFF242824),
    outlineVariant = Color(0xFF2A2E2A),
)

@Composable
fun QuranTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
