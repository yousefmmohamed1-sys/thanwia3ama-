package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val RafiqDarkColorScheme = darkColorScheme(
    primary = OrangeAccent,
    onPrimary = Color.White,
    primaryContainer = OrangeMuted,
    onPrimaryContainer = OrangeBright,
    secondary = AmberGold,
    onSecondary = NavyVoid,
    secondaryContainer = NavyCardElevated,
    onSecondaryContainer = AmberGold,
    tertiary = TealInfo,
    onTertiary = NavyVoid,
    tertiaryContainer = NavyCard,
    onTertiaryContainer = TealInfo,
    background = NavyDeep,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextWhite,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSlateLight,
    surfaceContainerLowest = NavyVoid,
    surfaceContainerLow = NavySurface,
    surfaceContainer = NavyCard,
    surfaceContainerHigh = NavyCardElevated,
    surfaceContainerHighest = NavyOutline,
    outline = NavyOutline,
    outlineVariant = NavyCardElevated,
    error = CoralDanger,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = Color(0xFFFCA5A5)
)

private val RafiqLightColorScheme = lightColorScheme(
    primary = OrangeAccent,
    onPrimary = Color.White,
    primaryContainer = LightOrangeContainer,
    onPrimaryContainer = LightOnOrangeContainer,
    secondary = NavyCard,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0C4A6E),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightBackground,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = LightOutline,
    outline = LightOutline,
    outlineVariant = LightSurfaceVariant,
    error = CoralDanger,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

val RafiqShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun RafiqTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RafiqDarkColorScheme else RafiqLightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RafiqTypography,
            shapes = RafiqShapes,
            content = content
        )
    }
}
