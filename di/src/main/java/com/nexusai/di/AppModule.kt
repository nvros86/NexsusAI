package com.nexusai.di

import android.content.Context
import androidx.room.Room
import com.nexusai.core.common.constants.AppConstants
import com.nexusai.data.ai.AIProviderManager
import com.nexusai.data.local.AIProviderDao
import com.nexusai.data.local.AppDatabase
import com.nexusai.data.local.TabDao
import com.nexusai.data.repository.AIProviderRepositoryImpl
import com.nexusai.data.repository.ChainRepositoryImpl
import com.nexusai.data.repository.MarketplaceRepositoryImpl
import com.nexusai.data.repository.ModuleRepositoryImpl
import com.nexusai.data.repository.PluginRepositoryImpl
import com.nexusai.data.repository.PromptRepositoryImpl
import com.nexusai.data.repository.SplitViewRepositoryImpl
import com.nexusai.data.repository.TabRepositoryImpl
import com.nexusai.data.repository.TaskTemplateRepositoryImpl
import com.nexusai.data.security.ApiKeyEncryption
import com.nexusai.domain.repository.AIProviderRepository
import com.nexusai.domain.repository.ChainRepository
import com.nexusai.domain.repository.MarketplaceRepository
import com.nexusai.domain.repository.ModuleRepository
import com.nexusai.domain.repository.PluginRepository
import com.nexusai.domain.repository.PromptRepository
import com.nexusai.domain.repository.SplitViewRepository
import com.nexusai.domain.repository.TabRepository
import com.nexusai.domain.repository.TaskTemplateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppConstants.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideTabDao(database: AppDatabase): TabDao = database.tabDao()

    @Provides
    fun provideAIProviderDao(database: AppDatabase): AIProviderDao = database.aiProviderDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTabRepository(dao: TabDao): TabRepository {
        return TabRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideAIProviderRepository(dao: AIProviderDao): AIProviderRepository {
        return AIProviderRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideTaskTemplateRepository(): TaskTemplateRepository {
        return TaskTemplateRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideMarketplaceRepository(@ApplicationContext context: Context): MarketplaceRepository {
        return MarketplaceRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePromptRepository(): PromptRepository {
        return PromptRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideModuleRepository(@ApplicationContext context: Context): ModuleRepository {
        return ModuleRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSplitViewRepository(@ApplicationContext context: Context): SplitViewRepository {
        return SplitViewRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideChainRepository(
        providerRepository: AIProviderRepository,
        aiProviderManager: AIProviderManager
    ): ChainRepository {
        return ChainRepositoryImpl(providerRepository, aiProviderManager)
    }

    @Provides
    @Singleton
    fun providePluginRepository(): PluginRepository {
        return PluginRepositoryImpl()
    }
}

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
}
