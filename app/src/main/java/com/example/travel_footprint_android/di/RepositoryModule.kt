// app/src/main/java/com/example/travel_footprint_android/di/RepositoryModule.kt
package com.example.travel_footprint_android.di

import com.example.travel_footprint_android.data.dao.*
import com.example.travel_footprint_android.data.repository.*
import com.example.travel_footprint_android.domain.service.FileStorageService
import com.example.travel_footprint_android.domain.service.LocationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideJourneyRepository(
        journeyDao: JourneyDao,
        footprintDao: FootprintDao,
        footprintRepository: FootprintRepository  // 改为注入 FootprintRepository
    ): JourneyRepository {
        return JourneyRepository(journeyDao, footprintDao)
    }

    @Provides
    @Singleton
    fun provideFootprintRepository(
        footprintDao: FootprintDao,
        locationDao: LocationDao,
        mediaDao: MediaDao,
        tagDao: TagDao,
        locationService: LocationService
    ): FootprintRepository {
        return FootprintRepository(
            footprintDao = footprintDao,
            locationDao = locationDao,
            mediaDao = mediaDao,
            tagDao = tagDao,
            locationService = locationService
        )
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaDao: MediaDao,
        fileStorageService: FileStorageService
    ): MediaRepository {
        return MediaRepository(mediaDao, fileStorageService)
    }
}