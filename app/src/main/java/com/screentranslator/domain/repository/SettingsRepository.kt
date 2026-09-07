package com.screentranslator.domain.repository

import com.screentranslator.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing persistent application settings.
 */
interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
}
