package com.screentranslator.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkNexiqColors.brandPrimary,
    onPrimary = Color(0xFF090D14),
    primaryContainer = DarkNexiqColors.brandPrimaryContainer,
    onPrimaryContainer = DarkNexiqColors.brandPrimaryOnContainer,
    secondary = DarkNexiqColors.brandPrimary,
    onSecondary = DarkNexiqColors.surfaceBase,
    background = DarkNexiqColors.surfaceBase,
    onBackground = DarkNexiqColors.textPrimary,
    surface = DarkNexiqColors.surfaceContainer,
    onSurface = DarkNexiqColors.textPrimary,
    surfaceVariant = DarkNexiqColors.surfaceElevated,
    onSurfaceVariant = DarkNexiqColors.textSecondary,
    outline = DarkNexiqColors.borderSubtle,
    outlineVariant = DarkNexiqColors.borderMedium,
    error = DarkNexiqColors.error,
    onError = DarkNexiqColors.textPrimary,
    errorContainer = DarkNexiqColors.errorContainer,
    onErrorContainer = DarkNexiqColors.error
)

private val LightColorScheme = lightColorScheme(
    primary = LightNexiqColors.brandPrimary,
    onPrimary = LightNexiqColors.surfaceContainer,
    primaryContainer = LightNexiqColors.brandPrimaryContainer,
    onPrimaryContainer = LightNexiqColors.brandPrimaryOnContainer,
    secondary = LightNexiqColors.brandPrimary,
    onSecondary = LightNexiqColors.surfaceContainer,
    background = LightNexiqColors.surfaceBase,
    onBackground = LightNexiqColors.textPrimary,
    surface = LightNexiqColors.surfaceContainer,
    onSurface = LightNexiqColors.textPrimary,
    surfaceVariant = LightNexiqColors.surfaceElevated,
    onSurfaceVariant = LightNexiqColors.textSecondary,
    outline = LightNexiqColors.borderSubtle,
    outlineVariant = LightNexiqColors.borderMedium,
    error = LightNexiqColors.error,
    onError = LightNexiqColors.surfaceContainer,
    errorContainer = LightNexiqColors.errorContainer,
    onErrorContainer = LightNexiqColors.error
)

@Composable
fun ScreenTranslatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val nexiqColors = if (darkTheme) DarkNexiqColors else LightNexiqColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalNexiqColors provides nexiqColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ScreenTranslatorTypography,
            shapes = NexiqShapes,
            content = content
        )
    }
}
