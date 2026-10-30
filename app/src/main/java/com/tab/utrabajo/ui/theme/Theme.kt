package com.tab.utrabajo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF84ADFF),
    onPrimary = Color(0xFF002A73),
    primaryContainer = Color(0xFF003B9E),
    onPrimaryContainer = Color(0xFFDDE7FF),
    secondary = Color(0xFF5FE9D0),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFFD1FADF),
    background = Night,
    onBackground = Color(0xFFF2F4F7),
    surface = NightSurface,
    onSurface = Color(0xFFF2F4F7),
    surfaceVariant = Color(0xFF253246),
    onSurfaceVariant = Color(0xFFD0D5DD),
    outline = NightBorder,
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = Color(0xFF002A73),
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = BrandTealSoft,
    onSecondaryContainer = Color(0xFF054F45),
    tertiary = BrandAmber,
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = InkMuted,
    outline = Border,
    error = ErrorRed,
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF7A271A)
)

private val UTrabajoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun UTrabajoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = UTrabajoShapes,
        content = content
    )
}