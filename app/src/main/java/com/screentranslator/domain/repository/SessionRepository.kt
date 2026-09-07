package com.screentranslator.domain.repository

import com.screentranslator.domain.model.TranslationSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository managing the active translation session lifecycle.
 */
interface SessionRepository {
    val activeSession: StateFlow<TranslationSession?>
    fun startSession(session: TranslationSession)
    fun updateSession(transform: (TranslationSession) -> TranslationSession)
    fun endSession()
}
