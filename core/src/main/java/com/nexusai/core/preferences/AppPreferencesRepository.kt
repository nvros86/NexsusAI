package com.nexusai.core.preferences

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    val isIncognitoMode: Flow<Boolean>
    val isHapticEnabled: Flow<Boolean>
    val isAppLockEnabled: Flow<Boolean>
    val fontScale: Flow<Int>
    val isHighContrast: Flow<Boolean>

    suspend fun setOnboardingCompleted()
    suspend fun setIncognitoMode(enabled: Boolean)
    suspend fun setHapticEnabled(enabled: Boolean)
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setFontScale(scale: Int)
    suspend fun setHighContrast(enabled: Boolean)
}
