package com.screentranslator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.screentranslator.domain.model.AppSettings
import com.screentranslator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screen_translator_settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val DEFAULT_SOURCE_LANG = stringPreferencesKey("default_source_lang")
        val DEFAULT_TARGET_LANG = stringPreferencesKey("default_target_lang")
        val SELECTED_OCR_ENGINE = stringPreferencesKey("selected_ocr_engine")
        val SELECTED_TRANSLATION_ENGINE = stringPreferencesKey("selected_translation_engine")
        val OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")
        val INITIAL_SCALE = floatPreferencesKey("initial_scale")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val REMEMBER_LAST_PAIR = booleanPreferencesKey("remember_last_pair")
        val AUTO_START_TRANSLATION = booleanPreferencesKey("auto_start_translation")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
        val FLOATING_BUTTON_ENABLED = booleanPreferencesKey("floating_button_enabled")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapPreferencesToSettings(preferences)
        }

    override suspend fun getSettings(): AppSettings {
        return settingsFlow.first()
    }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { preferences ->
            val current = mapPreferencesToSettings(preferences)
            val updated = transform(current)

            preferences[PreferencesKeys.DEFAULT_SOURCE_LANG] = updated.defaultSourceLanguageCode
            preferences[PreferencesKeys.DEFAULT_TARGET_LANG] = updated.defaultTargetLanguageCode
            preferences[PreferencesKeys.SELECTED_OCR_ENGINE] = updated.selectedOcrEngineId
            preferences[PreferencesKeys.SELECTED_TRANSLATION_ENGINE] = updated.selectedTranslationEngineId
            preferences[PreferencesKeys.OVERLAY_OPACITY] = updated.overlayOpacity
            preferences[PreferencesKeys.INITIAL_SCALE] = updated.initialScale
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = updated.hapticFeedback
            preferences[PreferencesKeys.SOUND_FEEDBACK] = updated.soundFeedback
            preferences[PreferencesKeys.REMEMBER_LAST_PAIR] = updated.rememberLastLanguagePair
            preferences[PreferencesKeys.AUTO_START_TRANSLATION] = updated.autoStartTranslation
            preferences[PreferencesKeys.SAVE_HISTORY] = updated.saveHistory
            preferences[PreferencesKeys.FLOATING_BUTTON_ENABLED] = updated.floatingButtonEnabled
        }
    }

    private fun mapPreferencesToSettings(preferences: Preferences): AppSettings {
        return AppSettings(
            defaultSourceLanguageCode = preferences[PreferencesKeys.DEFAULT_SOURCE_LANG] ?: "auto",
            defaultTargetLanguageCode = preferences[PreferencesKeys.DEFAULT_TARGET_LANG] ?: "vi",
            selectedOcrEngineId = preferences[PreferencesKeys.SELECTED_OCR_ENGINE] ?: "mlkit",
            selectedTranslationEngineId = preferences[PreferencesKeys.SELECTED_TRANSLATION_ENGINE] ?: "mlkit",
            overlayOpacity = preferences[PreferencesKeys.OVERLAY_OPACITY] ?: 0.95f,
            initialScale = preferences[PreferencesKeys.INITIAL_SCALE] ?: 0.90f,
            hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
            soundFeedback = preferences[PreferencesKeys.SOUND_FEEDBACK] ?: false,
            rememberLastLanguagePair = preferences[PreferencesKeys.REMEMBER_LAST_PAIR] ?: true,
            autoStartTranslation = preferences[PreferencesKeys.AUTO_START_TRANSLATION] ?: true,
            saveHistory = preferences[PreferencesKeys.SAVE_HISTORY] ?: false,
            floatingButtonEnabled = preferences[PreferencesKeys.FLOATING_BUTTON_ENABLED] ?: false
        )
    }
}
