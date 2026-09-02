package com.nexusai.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) : AppPreferencesRepository {

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
        private val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val FONT_SCALE = intPreferencesKey("font_scale")
        private val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    override val isIncognitoMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[INCOGNITO_MODE] ?: false
    }

    override val isHapticEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_ENABLED] ?: true
    }

    override val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_ENABLED] ?: false
    }

    override val fontScale: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FONT_SCALE] ?: 1
    }

    override val isHighContrast: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGH_CONTRAST] ?: false
    }

    override val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: true
    }

    override suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun setIncognitoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[INCOGNITO_MODE] = enabled
        }
    }

    override suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED] = enabled
        }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }

    override suspend fun setFontScale(scale: Int) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SCALE] = scale.coerceIn(0, 3)
        }
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST] = enabled
        }
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
}
