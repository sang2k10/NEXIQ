package com.screentranslator.domain.model

import android.graphics.Bitmap
import java.util.UUID

/**
 * State of an active translation session.
 */
data class TranslationSession(
    val id: String = UUID.randomUUID().toString(),
    val originalBitmap: Bitmap? = null,
    val workingBitmap: Bitmap? = null,
    val cropRect: CropRect? = null,
    val sourceLanguage: Language = Language.AUTO,
    val targetLanguage: Language = Language.VIETNAMESE,
    val detectedLanguage: Language? = null,
    val ocrBlocks: List<OcrBlock> = emptyList(),
    val translatedBlocks: List<TranslatedBlock> = emptyList(),
    val isOcrRunning: Boolean = false,
    val isTranslating: Boolean = false,
    val isCropMode: Boolean = false,
    val errorMessage: String? = null
) {
    val isLoading: Boolean get() = isOcrRunning || isTranslating

    fun clearBitmaps(): TranslationSession {
        originalBitmap?.let { if (!it.isRecycled) it.recycle() }
        workingBitmap?.let { if (!it.isRecycled && it != originalBitmap) it.recycle() }
        return copy(originalBitmap = null, workingBitmap = null)
    }
}
