package com.nexsusai.workstation.di

import android.content.Context
import androidx.room.Room
import com.nexsusai.workstation.data.local.AppDatabase
import com.nexsusai.workstation.data.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "nexsusai.db").build()

    @Provides
    @Singleton
    fun provideSessionRepository(database: AppDatabase): SessionRepository =
        SessionRepository(database.workSessionDao())
}
