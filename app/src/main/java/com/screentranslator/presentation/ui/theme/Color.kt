package com.screentranslator.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// NEXIQ Color Tokens — Harmonized with Option 1 Chubby Logo & Ice-Cyan Theme

data class NexiqColors(
    val surfaceBase: Color,
    val surfaceContainer: Color,
    val surfaceCard: Color,
    val surfaceCardSubtle: Color,
    val brandPrimary: Color,
    val brandPrimaryPressed: Color,
    val brandPrimaryLighter: Color,
    val brandPrimaryMuted: Color,
    val brandAccent: Color,
    val brandAccentMuted: Color,
    val brandGold: Color,
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

val DarkNexiqColors = NexiqColors(
    surfaceBase = Color(0xFF0B131B),
    surfaceContainer = Color(0xFF121E2B),
    surfaceCard = Color(0xFF18283A),
    surfaceCardSubtle = Color(0xFF152232),
    brandPrimary = Color(0xFF00AEEF),
    brandPrimaryPressed = Color(0xFF0284C7),
    brandPrimaryLighter = Color(0xFF38BDF8),
    brandPrimaryMuted = Color(0xFF00AEEF).copy(alpha = 0.16f),
    brandAccent = Color(0xFFFF5A36),
    brandAccentMuted = Color(0xFFFF5A36).copy(alpha = 0.18f),
    brandGold = Color(0xFFFF5A36),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF94A3B8),
    textTertiary = Color(0xFF64748B),
    borderSubtle = Color(0xFF1E354B),
    borderMedium = Color(0xFF2E4D6C),
    borderFocus = Color(0xFF00AEEF),
    success = Color(0xFF10B981),
    successContainer = Color(0xFF064E3B).copy(alpha = 0.35f),
    warning = Color(0xFFFF5A36),
    warningContainer = Color(0xFFFF5A36).copy(alpha = 0.18f),
    error = Color(0xFFEF4444),
    errorContainer = Color(0xFF4C0519).copy(alpha = 0.35f),
    isDark = true
)

val LightNexiqColors = NexiqColors(
    surfaceBase = Color(0xFFF4F8FA),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceCard = Color(0xFFF1F5F9),
    surfaceCardSubtle = Color(0xFFF8FAFC),
    brandPrimary = Color(0xFF0284C7),
    brandPrimaryPressed = Color(0xFF0369A1),
    brandPrimaryLighter = Color(0xFF00AEEF),
    brandPrimaryMuted = Color(0xFF0284C7).copy(alpha = 0.12f),
    brandAccent = Color(0xFFFF5A36),
    brandAccentMuted = Color(0xFFFF5A36).copy(alpha = 0.14f),
    brandGold = Color(0xFFFF5A36),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF64748B),
    borderSubtle = Color(0xFFE2E8F0),
    borderMedium = Color(0xFFCBD5E1),
    borderFocus = Color(0xFF0284C7),
    success = Color(0xFF059669),
    successContainer = Color(0xFFD1FAE5),
    warning = Color(0xFFFF5A36),
    warningContainer = Color(0xFFFFF7ED),
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

// Fallback direct references for backward compatibility
val SurfaceBase @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceBase
val SurfaceContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceContainer
val SurfaceCard @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceCard
val SurfaceCardSubtle @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceCardSubtle
val BrandPrimary @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimary
val BrandPrimaryPressed @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryPressed
val BrandPrimaryLighter @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryLighter
val BrandPrimaryMuted @Composable @ReadOnlyComposable get() = AppTheme.colors.brandPrimaryMuted
val BrandAccent @Composable @ReadOnlyComposable get() = AppTheme.colors.brandAccent
val BrandAccentMuted @Composable @ReadOnlyComposable get() = AppTheme.colors.brandAccentMuted
val BrandGold @Composable @ReadOnlyComposable get() = AppTheme.colors.brandGold
val TextPrimary @Composable @ReadOnlyComposable get() = AppTheme.colors.textPrimary
val TextSecondary @Composable @ReadOnlyComposable get() = AppTheme.colors.textSecondary
val TextTertiary @Composable @ReadOnlyComposable get() = AppTheme.colors.textTertiary
val BorderSubtle @Composable @ReadOnlyComposable get() = AppTheme.colors.borderSubtle
val BorderMedium @Composable @ReadOnlyComposable get() = AppTheme.colors.borderMedium
val BorderFocus @Composable @ReadOnlyComposable get() = AppTheme.colors.borderFocus
val SuccessGreen @Composable @ReadOnlyComposable get() = AppTheme.colors.success
val SuccessGreenContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.successContainer
val WarningAmber @Composable @ReadOnlyComposable get() = AppTheme.colors.warning
val WarningAmberContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.warningContainer
val ErrorRed @Composable @ReadOnlyComposable get() = AppTheme.colors.error
val ErrorRedContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.errorContainer

