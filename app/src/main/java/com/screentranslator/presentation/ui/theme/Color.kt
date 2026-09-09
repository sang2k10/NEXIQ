package com.screentranslator.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * NEXIQ Design System — Master Color Tokens
 *
 * Single source of truth for brand and UI colors:
 * - BrandPrimary: NEXIQ Cyan #00A8E8 (Identity, active/selected, CTA, focus, progress)
 * - BrandSecondary: Translation Orange #FF6B4A (Translation overlay, output, guidance/discovery)
 * - Neutrals dominate (85%+ of viewport):
 *   Dark: #0B121A background, #111B25 surface, #142231 elevated, #192B3A pressed
 *   Light: #F7F9FA background, #FFFFFF surface, #F0F4F7 elevated
 *   Typography: #F5F7F8 / #18212A (Primary), #A3AFBB / #5F6C77 (Secondary), #788694 / #84919C (Tertiary)
 * - System semantics: Green (#10B981), Amber (#F59E0B), Red (#EF4444)
 */
data class NexiqColors(
    val surfaceBase: Color,
    val surfaceContainer: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val surfacePressed: Color,
    val brandPrimary: Color,
    val brandPrimaryPressed: Color,
    val brandPrimaryContainer: Color,
    val brandPrimaryOnContainer: Color,
    val brandSecondary: Color,
    val brandSecondaryPressed: Color,
    val brandSecondaryContainer: Color,
    val brandSecondaryOnContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val borderSubtle: Color,
    val borderMedium: Color,
    val borderFocus: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val isDark: Boolean
)

// Dark Theme: Deep technical navy/slate with Cyan & Translation Orange accents
val DarkNexiqColors = NexiqColors(
    surfaceBase = Color(0xFF0B121A),
    surfaceContainer = Color(0xFF111B25),
    surfaceElevated = Color(0xFF142231),
    surfaceSubtle = Color(0xFF111B25),
    surfacePressed = Color(0xFF192B3A),
    brandPrimary = Color(0xFF00A8E8),
    brandPrimaryPressed = Color(0xFF008BC2),
    brandPrimaryContainer = Color(0xFF00A8E8).copy(alpha = 0.14f),
    brandPrimaryOnContainer = Color(0xFF38BDF8),
    brandSecondary = Color(0xFFFF6B4A),
    brandSecondaryPressed = Color(0xFFE85537),
    brandSecondaryContainer = Color(0xFFFF6B4A).copy(alpha = 0.14f),
    brandSecondaryOnContainer = Color(0xFFFF8A65),
    textPrimary = Color(0xFFF5F7F8),
    textSecondary = Color(0xFFA3AFBB),
    textTertiary = Color(0xFF788694),
    borderSubtle = Color(0xFF1C2C3D),
    borderMedium = Color(0xFF283E54),
    borderFocus = Color(0xFF00A8E8),
    success = Color(0xFF10B981),
    successContainer = Color(0xFF10B981).copy(alpha = 0.14f),
    warning = Color(0xFFF59E0B),
    warningContainer = Color(0xFFF59E0B).copy(alpha = 0.14f),
    error = Color(0xFFEF4444),
    errorContainer = Color(0xFFEF4444).copy(alpha = 0.14f),
    isDark = true
)

// Light Theme: Calm off-white with white surfaces, dark slate text, Cyan & Orange accents
val LightNexiqColors = NexiqColors(
    surfaceBase = Color(0xFFF7F9FA),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceSubtle = Color(0xFFF0F4F8),
    surfacePressed = Color(0xFFE2E8F0),
    brandPrimary = Color(0xFF00A8E8),
    brandPrimaryPressed = Color(0xFF008BC2),
    brandPrimaryContainer = Color(0xFF00A8E8).copy(alpha = 0.10f),
    brandPrimaryOnContainer = Color(0xFF0077A6),
    brandSecondary = Color(0xFFFF6B4A),
    brandSecondaryPressed = Color(0xFFE85537),
    brandSecondaryContainer = Color(0xFFFF6B4A).copy(alpha = 0.10f),
    brandSecondaryOnContainer = Color(0xFFD84315),
    textPrimary = Color(0xFF18212A),
    textSecondary = Color(0xFF5F6C77),
    textTertiary = Color(0xFF84919C),
    borderSubtle = Color(0xFFE5EAEF),
    borderMedium = Color(0xFFCBD5E1),
    borderFocus = Color(0xFF00A8E8),
    success = Color(0xFF059669),
    successContainer = Color(0xFFD1FAE5),
    warning = Color(0xFFD97706),
    warningContainer = Color(0xFFFEF3C7),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    isDark = false
)

val LocalNexiqColors = staticCompositionLocalOf { DarkNexiqColors }

object AppTheme {
    val colors: NexiqColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNexiqColors.current
}

// Convenient semantic properties for composables
val SurfaceBase @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceBase
val SurfaceContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceContainer
val SurfaceElevated @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceElevated
val SurfaceSubtle @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceSubtle
val SurfacePressed @Composable @ReadOnlyComposable get() = AppTheme.colors.surfacePressed

val BrandPrimary @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimary
val BrandPrimaryPressed @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryPressed
val BrandPrimaryContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryContainer
val BrandPrimaryMuted @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryContainer
val BrandPrimarySoft @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryContainer
val BrandPrimaryLighter @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryOnContainer

val BrandSecondary @Composable @ReadOnlyComposable get() = AppTheme.colors.brandSecondary
val BrandSecondaryPressed @Composable @ReadOnlyComposable get() = AppTheme.colors.brandSecondaryPressed
val BrandSecondaryContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.brandSecondaryContainer
val BrandSecondarySoft @Composable @ReadOnlyComposable get() = AppTheme.colors.brandSecondaryContainer
val BrandSecondaryLighter @Composable @ReadOnlyComposable get() = AppTheme.colors.brandSecondaryOnContainer

val TextPrimary @Composable @ReadOnlyComposable get() = AppTheme.colors.textPrimary
val TextSecondary @Composable @ReadOnlyComposable get() = AppTheme.colors.textSecondary
val TextTertiary @Composable @ReadOnlyComposable get() = AppTheme.colors.textTertiary

val BorderSubtle @Composable @ReadOnlyComposable get() = AppTheme.colors.borderSubtle
val BorderMedium @Composable @ReadOnlyComposable get() = AppTheme.colors.borderMedium
val BorderFocus @Composable @ReadOnlyComposable get() = AppTheme.colors.borderFocus

val SuccessGreen @Composable @ReadOnlyComposable get() = AppTheme.colors.success
val SuccessGreenContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.successContainer
val WarningAmber @Composable @ReadOnlyComposable get() = AppTheme.colors.warning
val ErrorRed @Composable @ReadOnlyComposable get() = AppTheme.colors.error
val SurfaceCard @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceElevated
val SurfaceCardSubtle @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceSubtle
