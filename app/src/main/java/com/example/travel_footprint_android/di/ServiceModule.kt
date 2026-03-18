// app/src/main/java/com/example/travel_footprint_android/di/ServiceModule.kt
package com.example.travel_footprint_android.di

import android.content.Context
import com.example.travel_footprint_android.domain.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context
    ): LocationService {
        return LocationService(context)
    }

    @Provides
    @Singleton
    fun provideFileStorageService(
        @ApplicationContext context: Context
    ): FileStorageService {
        return FileStorageService(context)
    }

    @Provides
    @Singleton
    fun provideLocalFileManager(
        @ApplicationContext context: Context
    ): LocalFileManager {
        return LocalFileManager(context)
    }

    @Provides
    @Singleton
    fun provideHandDrawEngine(): HandDrawEngine {
        return HandDrawEngine()
    }

    @Provides
    @Singleton
    fun provideTrailAnimator(): TrailAnimator {
        return TrailAnimator()
    }
}