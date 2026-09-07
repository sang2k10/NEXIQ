package com.screentranslator.data.repository

import com.screentranslator.domain.model.TranslationSession
import com.screentranslator.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepositoryImpl : SessionRepository {

    private val _activeSession = MutableStateFlow<TranslationSession?>(null)
    override val activeSession: StateFlow<TranslationSession?> = _activeSession.asStateFlow()

    override fun startSession(session: TranslationSession) {
        // End existing session if any to avoid leaking previous bitmaps
        endSession()
        _activeSession.value = session
    }

    override fun updateSession(transform: (TranslationSession) -> TranslationSession) {
        _activeSession.value?.let { current ->
            _activeSession.value = transform(current)
        }
    }

    override fun endSession() {
        val current = _activeSession.value
        if (current != null) {
            current.clearBitmaps()
            _activeSession.value = null
        }
    }
}
