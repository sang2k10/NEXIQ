package com.screentranslator

import android.graphics.Bitmap
import com.screentranslator.data.repository.SessionRepositoryImpl
import com.screentranslator.domain.engine.OcrEngine
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.OcrBlock
import com.screentranslator.domain.model.TranslationSession
import com.screentranslator.domain.usecase.ExecuteOcrUseCase
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExecuteOcrUseCaseTest {

    private class FakeOcrEngine(
        private val mockBlocks: List<OcrBlock> = emptyList(),
        private val shouldFail: Boolean = false
    ) : OcrEngine {
        override val id: String = "fake_ocr"
        override val displayName: String = "Fake OCR Engine"

        override suspend fun recognize(
            bitmap: Bitmap,
            preferredLanguage: Language?
        ): Result<List<OcrBlock>> {
            return if (shouldFail) {
                Result.failure(RuntimeException("Mock OCR Failure"))
            } else {
                Result.success(mockBlocks)
            }
        }

        override suspend fun isLanguageSupported(language: Language): Boolean = true
    }

    @Test
    fun executeOcr_withRecognizedBlocks_populatesSessionAndDetectsLanguage() = runBlocking {
        val sessionRepo = SessionRepositoryImpl()
        val dummyBitmap = mockk<Bitmap>(relaxed = true)

        sessionRepo.startSession(
            TranslationSession(
                originalBitmap = dummyBitmap,
                sourceLanguage = Language.AUTO,
                targetLanguage = Language.VIETNAMESE
            )
        )

        val mockBlocks = listOf(
            OcrBlock(text = "こんにちは", boundingBox = BoundingBox(10f, 10f, 80f, 30f), detectedLanguageCode = "ja"),
            OcrBlock(text = "世界", boundingBox = BoundingBox(10f, 40f, 60f, 60f), detectedLanguageCode = "ja")
        )

        val useCase = ExecuteOcrUseCase(FakeOcrEngine(mockBlocks), sessionRepo)
        useCase.invoke()

        val updated = sessionRepo.activeSession.value
        assertNotNull(updated)
        assertFalse(updated!!.isOcrRunning)
        assertEquals(2, updated.ocrBlocks.size)
        assertEquals("こんにちは", updated.ocrBlocks[0].text)
        assertEquals(Language.JAPANESE, updated.detectedLanguage)
        assertNull(updated.errorMessage)
    }

    @Test
    fun executeOcr_withEmptyBlocks_setsNoTextErrorMessage() = runBlocking {
        val sessionRepo = SessionRepositoryImpl()
        val dummyBitmap = mockk<Bitmap>(relaxed = true)

        sessionRepo.startSession(
            TranslationSession(
                originalBitmap = dummyBitmap,
                sourceLanguage = Language.ENGLISH,
                targetLanguage = Language.VIETNAMESE
            )
        )

        val useCase = ExecuteOcrUseCase(FakeOcrEngine(emptyList()), sessionRepo)
        useCase.invoke()

        val updated = sessionRepo.activeSession.value
        assertNotNull(updated)
        assertFalse(updated!!.isOcrRunning)
        assertTrue(updated.ocrBlocks.isEmpty())
        assertTrue(updated.errorMessage?.contains("No text detected") == true)
    }
}
