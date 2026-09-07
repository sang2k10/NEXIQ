package com.screentranslator

import android.graphics.Bitmap
import com.screentranslator.domain.engine.OcrEngine
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.CropRect
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.OcrBlock
import com.screentranslator.domain.model.TranslationSession
import com.screentranslator.domain.repository.SessionRepository
import com.screentranslator.domain.usecase.RetranslateCropUseCase
import com.screentranslator.domain.usecase.TranslateBlocksUseCase
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RetranslateCropUseCaseTest {

    private val sessionRepo = mockk<SessionRepository>(relaxed = true)
    private val ocrEngine = mockk<OcrEngine>()
    private val translateBlocksUseCase = mockk<TranslateBlocksUseCase>(relaxed = true)
    private val useCase = RetranslateCropUseCase(sessionRepo, ocrEngine, translateBlocksUseCase)

    @Test
    fun invoke_whenBitmapIsRecycled_returnsFailure() = runBlocking {
        val bitmap = mockk<Bitmap>()
        every { bitmap.isRecycled } returns true

        val session = TranslationSession(
            id = "test-1",
            originalBitmap = bitmap,
            sourceLanguage = Language.AUTO,
            targetLanguage = Language.VIETNAMESE
        )
        every { sessionRepo.activeSession } returns MutableStateFlow(session)

        val result = useCase(CropRect(0f, 0f, 100f, 100f))
        assertTrue(result.isFailure)
    }

    @Test
    fun invoke_whenNoTextDetectedInCrop_returnsSuccessWithEmptyOcr() = runBlocking {
        mockkStatic(Bitmap::class)
        val originalBitmap = mockk<Bitmap>()
        val subBitmap = mockk<Bitmap>(relaxed = true)

        every { originalBitmap.isRecycled } returns false
        every { originalBitmap.width } returns 1080
        every { originalBitmap.height } returns 1920

        every { Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any()) } returns subBitmap
        coEvery { ocrEngine.recognize(subBitmap, any()) } returns Result.success(emptyList())

        val session = TranslationSession(
            id = "test-2",
            originalBitmap = originalBitmap,
            sourceLanguage = Language.AUTO,
            targetLanguage = Language.VIETNAMESE
        )
        every { sessionRepo.activeSession } returns MutableStateFlow(session)

        val cropRect = CropRect(100f, 200f, 500f, 600f)
        val result = useCase(cropRect)

        assertTrue(result.isSuccess)
        verify { sessionRepo.updateSession(any()) }

        unmockkStatic(Bitmap::class)
    }

    @Test
    fun invoke_shiftsBoundingBoxesByCropOrigin() = runBlocking {
        mockkStatic(Bitmap::class)
        val originalBitmap = mockk<Bitmap>()
        val subBitmap = mockk<Bitmap>(relaxed = true)

        every { originalBitmap.isRecycled } returns false
        every { originalBitmap.width } returns 1080
        every { originalBitmap.height } returns 1920

        val cropLeft = 100
        val cropTop = 200
        every { Bitmap.createBitmap(originalBitmap, cropLeft, cropTop, 400, 400) } returns subBitmap

        // OCR detects a block at (10, 20, 110, 70) relative to subBitmap
        val localBox = BoundingBox(10f, 20f, 110f, 70f)
        val detectedBlock = OcrBlock(
            text = "Konnichiwa",
            boundingBox = localBox
        )
        coEvery { ocrEngine.recognize(subBitmap, any()) } returns Result.success(listOf(detectedBlock))

        var updatedSession: TranslationSession? = null
        every { sessionRepo.updateSession(any()) } answers {
            val transform = firstArg<(TranslationSession) -> TranslationSession>()
            val initial = TranslationSession(
                id = "test-3",
                originalBitmap = originalBitmap,
                sourceLanguage = Language.JAPANESE,
                targetLanguage = Language.VIETNAMESE
            )
            updatedSession = transform(initial)
        }

        val session = TranslationSession(
            id = "test-3",
            originalBitmap = originalBitmap,
            sourceLanguage = Language.JAPANESE,
            targetLanguage = Language.VIETNAMESE
        )
        every { sessionRepo.activeSession } returns MutableStateFlow(session)

        val cropRect = CropRect(100f, 200f, 500f, 600f)
        val result = useCase(cropRect)

        assertTrue(result.isSuccess)
        coVerify { translateBlocksUseCase() }

        // Verify that bounding box was shifted by cropLeft=100 and cropTop=200
        assertNotNull(updatedSession)
        val shiftedBlock = updatedSession!!.ocrBlocks.first()
        assertEquals(110f, shiftedBlock.boundingBox.left, 0.01f)
        assertEquals(220f, shiftedBlock.boundingBox.top, 0.01f)
        assertEquals(210f, shiftedBlock.boundingBox.right, 0.01f)
        assertEquals(270f, shiftedBlock.boundingBox.bottom, 0.01f)

        unmockkStatic(Bitmap::class)
    }
}
