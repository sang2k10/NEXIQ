package com.screentranslator.core.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.domain.model.TranslationSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coordinates starting an interactive translation session from a captured screen bitmap.
 * Centralizes session initialization, haptic feedback, overlay presentation, and OCR dispatch.
 */
object TranslationSessionCoordinator {

    suspend fun startSession(bitmap: Bitmap, context: Context) {
        val container = ScreenTranslatorApp.instance.container
        val settings = container.settingsRepository.getSettings()

        val session = TranslationSession(
            originalBitmap = bitmap,
            workingBitmap = bitmap,
            sourceLanguage = settings.defaultSourceLanguage,
            targetLanguage = settings.defaultTargetLanguage,
            isOcrRunning = settings.autoStartTranslation
        )
        container.sessionRepository.startSession(session)

        if (settings.hapticFeedback) {
            triggerHapticFeedback(context)
        }

        // Show the interactive overlay on main thread
        withContext(Dispatchers.Main) {
            container.overlayManager.showOverlay()
        }

        // Auto-start OCR and translation pipeline
        if (settings.autoStartTranslation) {
            container.executeOcrUseCase()
            container.translateBlocksUseCase()
        }
    }

    private fun triggerHapticFeedback(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(30)
                }
            }
        } catch (_: Exception) {}
    }
}
