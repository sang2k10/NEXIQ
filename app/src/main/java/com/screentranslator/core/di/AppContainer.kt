package com.screentranslator.core.di

import android.content.Context
import com.screentranslator.data.capture.ScreenCaptureManagerImpl
import com.screentranslator.data.repository.SessionRepositoryImpl
import com.screentranslator.data.repository.SettingsRepositoryImpl
import com.screentranslator.domain.capture.ScreenCaptureManager
import com.screentranslator.domain.repository.SessionRepository
import com.screentranslator.domain.repository.SettingsRepository

/**
 * Lightweight Dependency Container / Service Locator.
 */
class AppContainer(context: Context) {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context.applicationContext)
    }

    val screenCaptureManager: ScreenCaptureManager by lazy {
        ScreenCaptureManagerImpl(context.applicationContext)
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl()
    }

    val ocrEngine: com.screentranslator.domain.engine.OcrEngine by lazy {
        com.screentranslator.data.engine.mlkit.MlKitOcrEngine()
    }

    val translationEngine: com.screentranslator.domain.engine.TranslationEngine by lazy {
        com.screentranslator.data.engine.mlkit.MlKitTranslationEngine()
    }

    val executeOcrUseCase: com.screentranslator.domain.usecase.ExecuteOcrUseCase by lazy {
        com.screentranslator.domain.usecase.ExecuteOcrUseCase(ocrEngine, sessionRepository)
    }

    val translateBlocksUseCase: com.screentranslator.domain.usecase.TranslateBlocksUseCase by lazy {
        com.screentranslator.domain.usecase.TranslateBlocksUseCase(translationEngine, sessionRepository)
    }

    val retranslateCropUseCase: com.screentranslator.domain.usecase.RetranslateCropUseCase by lazy {
        com.screentranslator.domain.usecase.RetranslateCropUseCase(sessionRepository, ocrEngine, translateBlocksUseCase)
    }

    val overlayManager: com.screentranslator.presentation.overlay.OverlayManager by lazy {
        com.screentranslator.presentation.overlay.OverlayManager(context.applicationContext)
    }

    val floatingBubbleManager: com.screentranslator.presentation.overlay.FloatingBubbleManager by lazy {
        com.screentranslator.presentation.overlay.FloatingBubbleManager(context.applicationContext)
    }
}
