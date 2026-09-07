package com.screentranslator.domain.engine

import android.graphics.Bitmap
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.OcrBlock

/**
 * Replaceable OCR subsystem interface.
 */
interface OcrEngine {
    val id: String
    val displayName: String

    /**
     * Performs optical character recognition on the given bitmap.
     */
    suspend fun recognize(
        bitmap: Bitmap,
        preferredLanguage: Language? = null
    ): Result<List<OcrBlock>>

    /**
     * Checks if this engine supports recognizing the given language script.
     */
    suspend fun isLanguageSupported(language: Language): Boolean
}
