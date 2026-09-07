package com.screentranslator.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.screentranslator.domain.model.AppSettings
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun setDefaultSourceLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(defaultSourceLanguageCode = language.code) }
        }
    }

    fun setDefaultTargetLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(defaultTargetLanguageCode = language.code) }
        }
    }

    fun setSelectedOcrEngine(engineId: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(selectedOcrEngineId = engineId) }
        }
    }

    fun setSelectedTranslationEngine(engineId: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(selectedTranslationEngineId = engineId) }
        }
    }

    fun setOverlayOpacity(opacity: Float) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(overlayOpacity = opacity) }
        }
    }

    fun setInitialScale(scale: Float) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(initialScale = scale) }
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(hapticFeedback = enabled) }
        }
    }

    fun setSoundFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(soundFeedback = enabled) }
        }
    }

    fun setRememberLastLanguagePair(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(rememberLastLanguagePair = enabled) }
        }
    }

    fun setAutoStartTranslation(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(autoStartTranslation = enabled) }
        }
    }

    fun setSaveHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(saveHistory = enabled) }
        }
    }

    fun setFloatingButtonEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(floatingButtonEnabled = enabled) }
        }
    }

    companion object {
        fun provideFactory(settingsRepository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(settingsRepository) as T
                }
            }
    }
}
