package com.screentranslator.data.engine

import com.screentranslator.domain.engine.TranslationEngine
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.repository.SettingsRepository

/**
 * Dynamically routes translation calls to the user's selected engine in AppSettings.
 * Allows switching between Google ML Kit (Offline) and Google Translate (Cloud / No downloads).
 */
class DelegatingTranslationEngine(
    private val mlKitEngine: TranslationEngine,
    private val googleWebEngine: TranslationEngine,
    private val settingsRepository: SettingsRepository
) : TranslationEngine {

    override val id: String = "delegating"
    override val displayName: String = "Dynamic Delegating Engine"

    private suspend fun activeEngine(): TranslationEngine {
        val settings = settingsRepository.getSettings()
        return when (settings.selectedTranslationEngineId) {
            "google_web" -> googleWebEngine
            else -> mlKitEngine
        }
    }

    override suspend fun translate(
        text: String,
        sourceLang: Language,
        targetLang: Language
    ): Result<String> {
        return activeEngine().translate(text, sourceLang, targetLang)
    }

    override suspend fun translateBatch(
        texts: List<String>,
        sourceLang: Language,
        targetLang: Language
    ): Result<List<String>> {
        return activeEngine().translateBatch(texts, sourceLang, targetLang)
    }

    override suspend fun getSupportedLanguages(): List<Language> {
        return activeEngine().getSupportedLanguages()
    }
}
