package com.screentranslator.domain.model

/**
 * User application settings persisted in DataStore.
 */
data class AppSettings(
    val defaultSourceLanguageCode: String = "auto",
    val defaultTargetLanguageCode: String = "vi",
    val selectedOcrEngineId: String = "mlkit",
    val selectedTranslationEngineId: String = "mlkit",
    val overlayOpacity: Float = 0.95f,
    val initialScale: Float = 0.90f,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val rememberLastLanguagePair: Boolean = true,
    val autoStartTranslation: Boolean = true,
    val saveHistory: Boolean = false,
    val floatingButtonEnabled: Boolean = false
) {
    val defaultSourceLanguage: Language get() = Language.fromCode(defaultSourceLanguageCode)
    val defaultTargetLanguage: Language get() = Language.fromCode(defaultTargetLanguageCode)
    val showFloatingButton: Boolean get() = floatingButtonEnabled
}
