package com.screentranslator.domain.usecase

import com.screentranslator.domain.engine.TranslationEngine
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.TranslatedBlock
import com.screentranslator.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslateBlocksUseCase(
    private val translationEngine: TranslationEngine,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        val currentSession = sessionRepository.activeSession.value ?: return@withContext
        val blocks = currentSession.ocrBlocks
        if (blocks.isEmpty()) return@withContext

        sessionRepository.updateSession { it.copy(isTranslating = true, errorMessage = null) }

        // If source language is Auto Detect, use detected language if available
        val effectiveSource = if (currentSession.sourceLanguage.isAutoDetect) {
            currentSession.detectedLanguage ?: Language.ENGLISH
        } else {
            currentSession.sourceLanguage
        }
        val targetLang = currentSession.targetLanguage

        val texts = blocks.map { it.text }
        val bitmap = currentSession.workingBitmap ?: currentSession.originalBitmap
        val result = translationEngine.translateBatch(texts, effectiveSource, targetLang)

        result.onSuccess { translatedTexts ->
            val translatedBlocks = blocks.mapIndexed { index, ocrBlock ->
                val translated = translatedTexts.getOrElse(index) { ocrBlock.text }
                val colorAnalysis = if (bitmap != null && !bitmap.isRecycled) {
                    com.screentranslator.presentation.overlay.render.TextBackgroundMasker.analyzeColors(bitmap, ocrBlock.boundingBox)
                } else null

                TranslatedBlock(
                    originalText = ocrBlock.text,
                    translatedText = translated,
                    boundingBox = ocrBlock.boundingBox,
                    sourceLanguage = effectiveSource,
                    targetLanguage = targetLang,
                    orientationDegrees = ocrBlock.orientationDegrees,
                    backgroundColor = colorAnalysis?.backgroundColor,
                    textColor = colorAnalysis?.textColor ?: 0xFFFFFFFF.toInt()
                )
            }

            sessionRepository.updateSession {
                it.copy(
                    isTranslating = false,
                    translatedBlocks = translatedBlocks,
                    errorMessage = null
                )
            }
        }.onFailure { error ->
            sessionRepository.updateSession {
                it.copy(
                    isTranslating = false,
                    errorMessage = "Translation error: ${error.localizedMessage ?: "Failed to translate"}"
                )
            }
        }
    }
}
