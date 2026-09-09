package com.screentranslator.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * NEXIQ Design System — Unified Color Tokens
 * Derived from the Precision Viewframe 'N' brand identity.
 * 85% Neutral Slate surfaces, 15% Precision Azure accent.
 */
data class NexiqColors(
    val surfaceBase: Color,
    val surfaceContainer: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val brandPrimary: Color,
    val brandPrimaryPressed: Color,
    val brandPrimaryContainer: Color,
    val brandPrimaryOnContainer: Color,
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

// Dark Theme: Deep Obsidian Slate with luminous Azure accent
val DarkNexiqColors = NexiqColors(
    surfaceBase = Color(0xFF090D14),
    surfaceContainer = Color(0xFF0F172A),
    surfaceElevated = Color(0xFF1E293B),
    surfaceSubtle = Color(0xFF131C2E),
    brandPrimary = Color(0xFF38BDF8),
    brandPrimaryPressed = Color(0xFF0284C7),
    brandPrimaryContainer = Color(0xFF38BDF8).copy(alpha = 0.14f),
    brandPrimaryOnContainer = Color(0xFF7DD3FC),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textTertiary = Color(0xFF64748B),
    borderSubtle = Color(0xFF1E293B),
    borderMedium = Color(0xFF334155),
    borderFocus = Color(0xFF38BDF8),
    success = Color(0xFF10B981),
    successContainer = Color(0xFF10B981).copy(alpha = 0.14f),
    warning = Color(0xFFF59E0B),
    warningContainer = Color(0xFFF59E0B).copy(alpha = 0.14f),
    error = Color(0xFFEF4444),
    errorContainer = Color(0xFFEF4444).copy(alpha = 0.14f),
    isDark = true
)

// Light Theme: Crisp Slate-50 with authoritative Cobalt Azure accent
val LightNexiqColors = NexiqColors(
    surfaceBase = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF1F5F9),
    surfaceSubtle = Color(0xFFF1F5F9),
    brandPrimary = Color(0xFF0284C7),
    brandPrimaryPressed = Color(0xFF0369A1),
    brandPrimaryContainer = Color(0xFF0284C7).copy(alpha = 0.10f),
    brandPrimaryOnContainer = Color(0xFF0369A1),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF94A3B8),
    borderSubtle = Color(0xFFE2E8F0),
    borderMedium = Color(0xFFCBD5E1),
    borderFocus = Color(0xFF0284C7),
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
val BrandPrimary @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimary
val BrandPrimaryPressed @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryPressed
val BrandPrimaryContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryContainer
val BrandPrimaryMuted @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryContainer
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
val BrandPrimaryLighter @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryOnContainer
