package com.finley.android.shared.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val AppColorScheme = darkColorScheme(
    primary = Rose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1A2E),
    onPrimaryContainer = InkWhite,
    secondary = Violet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF241F4E),
    onSecondaryContainer = InkWhite,
    tertiary = Sky,
    onTertiary = Color.White,
    background = Night,
    onBackground = InkWhite,
    surface = Midnight,
    onSurface = InkWhite,
    surfaceVariant = Color(0xFF232850),
    onSurfaceVariant = InkMuted,
    error = Color(0xFFFF5252),
    onError = Color.White,
    outline = Color(0xFF3A4060)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = getTypography(),
        shapes = AppShapes,
        content = content
    )
}
