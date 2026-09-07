package com.screentranslator.domain.usecase

import com.screentranslator.domain.engine.OcrEngine
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExecuteOcrUseCase(
    private val ocrEngine: OcrEngine,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        val currentSession = sessionRepository.activeSession.value ?: return@withContext
        val bitmap = currentSession.workingBitmap ?: currentSession.originalBitmap ?: return@withContext

        sessionRepository.updateSession { it.copy(isOcrRunning = true, errorMessage = null) }

        val result = ocrEngine.recognize(bitmap, currentSession.sourceLanguage)
        result.onSuccess { blocks ->
            if (blocks.isEmpty()) {
                sessionRepository.updateSession {
                    it.copy(
                        isOcrRunning = false,
                        ocrBlocks = emptyList(),
                        errorMessage = "No text detected. Try cropping the text area."
                    )
                }
            } else {
                // Determine dominant detected language if auto-detect is enabled
                val detectedLang = if (currentSession.sourceLanguage.isAutoDetect) {
                    val dominantCode = blocks.mapNotNull { it.detectedLanguageCode }
                        .groupingBy { it }
                        .eachCount()
                        .maxByOrNull { it.value }?.key ?: "en"
                    Language.fromCode(dominantCode)
                } else {
                    currentSession.sourceLanguage
                }

                sessionRepository.updateSession {
                    it.copy(
                        isOcrRunning = false,
                        ocrBlocks = blocks,
                        detectedLanguage = detectedLang,
                        errorMessage = null
                    )
                }
            }
        }.onFailure { error ->
            sessionRepository.updateSession {
                it.copy(
                    isOcrRunning = false,
                    errorMessage = "OCR error: ${error.localizedMessage ?: "Unknown recognition failure"}"
                )
            }
        }
    }
}
