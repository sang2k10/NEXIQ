package com.screentranslator.data.engine.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.screentranslator.domain.engine.TranslationEngine
import com.screentranslator.domain.model.Language
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitTranslationEngine : TranslationEngine {

    override val id: String = "mlkit"
    override val displayName: String = "Google ML Kit (On-Device, Offline)"

    private val translators = ConcurrentHashMap<String, Translator>()
    private val downloadSemaphore = Semaphore(2)

    override suspend fun translate(
        text: String,
        sourceLang: Language,
        targetLang: Language
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) return@withContext Result.success("")

            val srcCode = mapToMlKitLanguageCode(sourceLang)
            val tgtCode = mapToMlKitLanguageCode(targetLang)

            val translator = getOrCreateTranslator(srcCode, tgtCode)
            ensureModelDownloaded(translator)

            val translated = translateInternal(translator, text)
            Result.success(translated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateBatch(
        texts: List<String>,
        sourceLang: Language,
        targetLang: Language
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (texts.isEmpty()) return@withContext Result.success(emptyList())

            val srcCode = mapToMlKitLanguageCode(sourceLang)
            val tgtCode = mapToMlKitLanguageCode(targetLang)

            val translator = getOrCreateTranslator(srcCode, tgtCode)
            ensureModelDownloaded(translator)

            // Concurrently translate blocks with a concurrency limit of 5
            val batchSemaphore = Semaphore(5)
            val deferredTranslations = texts.map { text ->
                async {
                    if (text.isBlank()) ""
                    else {
                        batchSemaphore.withPermit {
                            translateInternal(translator, text)
                        }
                    }
                }
            }

            val results = deferredTranslations.awaitAll()
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSupportedLanguages(): List<Language> {
        return Language.SUPPORTED_TARGET_LANGUAGES
    }

    private fun getOrCreateTranslator(sourceLangCode: String, targetLangCode: String): Translator {
        val key = "${sourceLangCode}_$targetLangCode"
        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLangCode)
                .setTargetLanguage(targetLangCode)
                .build()
            Translation.getClient(options)
        }
    }

    private suspend fun ensureModelDownloaded(translator: Translator): Unit = downloadSemaphore.withPermit {
        suspendCancellableCoroutine<Unit> { continuation ->
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }
    }

    private suspend fun translateInternal(translator: Translator, text: String): String =
        suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    if (continuation.isActive) continuation.resume(translatedText)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }

    private fun mapToMlKitLanguageCode(language: Language): String {
        return when (language.code.lowercase()) {
            "vi" -> TranslateLanguage.VIETNAMESE
            "ja" -> TranslateLanguage.JAPANESE
            "en" -> TranslateLanguage.ENGLISH
            "zh", "zh-cn" -> TranslateLanguage.CHINESE
            "ko" -> TranslateLanguage.KOREAN
            "fr" -> TranslateLanguage.FRENCH
            "de" -> TranslateLanguage.GERMAN
            "es" -> TranslateLanguage.SPANISH
            "ru" -> TranslateLanguage.RUSSIAN
            else -> TranslateLanguage.ENGLISH
        }
    }
}
