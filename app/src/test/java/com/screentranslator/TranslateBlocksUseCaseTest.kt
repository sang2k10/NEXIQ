package com.screentranslator

import com.screentranslator.data.repository.SessionRepositoryImpl
import com.screentranslator.domain.engine.TranslationEngine
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.OcrBlock
import com.screentranslator.domain.model.TranslationSession
import com.screentranslator.domain.usecase.TranslateBlocksUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TranslateBlocksUseCaseTest {

    private class FakeTranslationEngine(
        private val mockTranslations: Map<String, String> = emptyMap(),
        private val shouldFail: Boolean = false
    ) : TranslationEngine {
        override val id: String = "fake_translate"
        override val displayName: String = "Fake Translation Engine"

        override suspend fun translate(
            text: String,
            sourceLang: Language,
            targetLang: Language
        ): Result<String> {
            return if (shouldFail) {
                Result.failure(RuntimeException("Translation network error"))
            } else {
                Result.success(mockTranslations[text] ?: "translated_$text")
            }
        }

        override suspend fun translateBatch(
            texts: List<String>,
            sourceLang: Language,
            targetLang: Language
        ): Result<List<String>> {
            return if (shouldFail) {
                Result.failure(RuntimeException("Translation batch failure"))
            } else {
                Result.success(texts.map { mockTranslations[it] ?: "translated_$it" })
            }
        }

        override suspend fun getSupportedLanguages(): List<Language> = Language.SUPPORTED_TARGET_LANGUAGES
    }

    @Test
    fun translateBlocks_mapsTranslatedTextToOriginalBoundingBoxes() = runBlocking {
        val sessionRepo = SessionRepositoryImpl()
        val ocrBlocks = listOf(
            OcrBlock(text = "Hello", boundingBox = BoundingBox(10f, 20f, 100f, 50f)),
            OcrBlock(text = "World", boundingBox = BoundingBox(10f, 60f, 120f, 90f))
        )

        sessionRepo.startSession(
            TranslationSession(
                sourceLanguage = Language.ENGLISH,
                targetLanguage = Language.VIETNAMESE,
                ocrBlocks = ocrBlocks
            )
        )

        val fakeEngine = FakeTranslationEngine(
            mapOf("Hello" to "Xin chào", "World" to "Thế giới")
        )

        val useCase = TranslateBlocksUseCase(fakeEngine, sessionRepo)
        useCase.invoke()

        val updated = sessionRepo.activeSession.value
        assertNotNull(updated)
        assertFalse(updated!!.isTranslating)
        assertEquals(2, updated.translatedBlocks.size)

        // Check first block
        val block1 = updated.translatedBlocks[0]
        assertEquals("Hello", block1.originalText)
        assertEquals("Xin chào", block1.translatedText)
        assertEquals(10f, block1.boundingBox.left, 0.001f)
        assertEquals(50f, block1.boundingBox.bottom, 0.001f)

        // Check second block
        val block2 = updated.translatedBlocks[1]
        assertEquals("World", block2.originalText)
        assertEquals("Thế giới", block2.translatedText)
        assertEquals(120f, block2.boundingBox.right, 0.001f)
    }

    @Test
    fun translateBlocks_onFailure_preservesOcrBlocksAndSetsErrorMessage() = runBlocking {
        val sessionRepo = SessionRepositoryImpl()
        val ocrBlocks = listOf(
            OcrBlock(text = "Sample", boundingBox = BoundingBox(0f, 0f, 50f, 20f))
        )

        sessionRepo.startSession(
            TranslationSession(
                sourceLanguage = Language.ENGLISH,
                targetLanguage = Language.VIETNAMESE,
                ocrBlocks = ocrBlocks
            )
        )

        val failingEngine = FakeTranslationEngine(shouldFail = true)
        val useCase = TranslateBlocksUseCase(failingEngine, sessionRepo)
        useCase.invoke()

        val updated = sessionRepo.activeSession.value
        assertNotNull(updated)
        assertFalse(updated!!.isTranslating)
        assertTrue(updated.translatedBlocks.isEmpty())
        assertEquals(1, updated.ocrBlocks.size)
        assertTrue(updated.errorMessage?.contains("Translation error") == true)
    }
}
