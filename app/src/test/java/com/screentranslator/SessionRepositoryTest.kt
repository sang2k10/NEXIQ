package com.screentranslator

import com.screentranslator.data.repository.SessionRepositoryImpl
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.TranslationSession
import org.junit.Assert.*
import org.junit.Test

class SessionRepositoryTest {

    @Test
    fun sessionLifecycle_startUpdateAndEnd_updatesStateAndCleansBitmaps() {
        val repo = SessionRepositoryImpl()
        assertNull(repo.activeSession.value)

        val session = TranslationSession(
            sourceLanguage = Language.JAPANESE,
            targetLanguage = Language.VIETNAMESE,
            isOcrRunning = true
        )

        repo.startSession(session)
        assertNotNull(repo.activeSession.value)
        assertEquals(Language.JAPANESE, repo.activeSession.value?.sourceLanguage)
        assertEquals(Language.VIETNAMESE, repo.activeSession.value?.targetLanguage)
        assertTrue(repo.activeSession.value?.isOcrRunning == true)

        // Update session
        repo.updateSession { it.copy(isOcrRunning = false, detectedLanguage = Language.JAPANESE) }
        assertFalse(repo.activeSession.value?.isOcrRunning == true)
        assertEquals(Language.JAPANESE, repo.activeSession.value?.detectedLanguage)

        // End session
        repo.endSession()
        assertNull(repo.activeSession.value)
    }

    @Test
    fun endSession_recyclesBothOriginalAndWorkingBitmaps() {
        val repo = SessionRepositoryImpl()
        val original = io.mockk.mockk<android.graphics.Bitmap>(relaxed = true)
        val working = io.mockk.mockk<android.graphics.Bitmap>(relaxed = true)

        io.mockk.every { original.isRecycled } returns false
        io.mockk.every { working.isRecycled } returns false

        val session = TranslationSession(
            originalBitmap = original,
            workingBitmap = working
        )
        repo.startSession(session)
        repo.endSession()

        io.mockk.verify(exactly = 1) { original.recycle() }
        io.mockk.verify(exactly = 1) { working.recycle() }
        assertNull(repo.activeSession.value)
    }

    @Test
    fun endSession_whenOriginalEqualsWorking_recyclesOnlyOnce() {
        val repo = SessionRepositoryImpl()
        val original = io.mockk.mockk<android.graphics.Bitmap>(relaxed = true)

        io.mockk.every { original.isRecycled } returns false

        val session = TranslationSession(
            originalBitmap = original,
            workingBitmap = original
        )
        repo.startSession(session)
        repo.endSession()

        io.mockk.verify(exactly = 1) { original.recycle() }
        assertNull(repo.activeSession.value)
    }
}
