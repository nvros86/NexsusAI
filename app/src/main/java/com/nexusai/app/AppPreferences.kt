package com.nexusai.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
        private val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val isIncognitoMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[INCOGNITO_MODE] ?: false
    }

    val isHapticEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_ENABLED] ?: true
    }

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_ENABLED] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setIncognitoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[INCOGNITO_MODE] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }
}
