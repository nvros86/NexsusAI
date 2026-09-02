package com.nexusai.di

import android.content.Context
import com.nexusai.core.preferences.AppPreferences
import com.nexusai.data.ai.AIProviderManager
import com.nexusai.data.security.ApiKeyEncryption
import com.nexusai.core.preferences.AppPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideApiKeyEncryption(@ApplicationContext context: Context): ApiKeyEncryption {
        return ApiKeyEncryption(context)
    }

    @Provides
    @Singleton
    fun provideAIProviderManager(encryption: ApiKeyEncryption): AIProviderManager {
        return AIProviderManager(encryption)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(appPreferences: AppPreferences): AppPreferencesRepository {
        return appPreferences
    }
}
