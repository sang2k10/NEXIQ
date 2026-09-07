package com.screentranslator.domain.engine

import com.screentranslator.domain.model.Language

/**
 * Replaceable Translation subsystem interface.
 */
interface TranslationEngine {
    val id: String
    val displayName: String

    /**
     * Translates a single text string from source to target language.
     */
    suspend fun translate(
        text: String,
        sourceLang: Language,
        targetLang: Language
    ): Result<String>

    /**
     * Translates a batch of text strings efficiently.
     */
    suspend fun translateBatch(
        texts: List<String>,
        sourceLang: Language,
        targetLang: Language
    ): Result<List<String>>

    /**
     * Returns list of supported target languages.
     */
    suspend fun getSupportedLanguages(): List<Language>
}
