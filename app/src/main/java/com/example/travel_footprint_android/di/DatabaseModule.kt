// app/src/main/java/com/example/travel_footprint_android/di/DatabaseModule.kt
package com.example.travel_footprint_android.di

import android.content.Context
import androidx.room.Room
import com.example.travel_footprint_android.data.dao.*
import com.example.travel_footprint_android.data.database.AppDatabase
import com.example.travel_footprint_android.data.database.Converters
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "travel_journal.db"
        )
            .addTypeConverter(Converters())
            .build()
    }

    @Provides
    @Singleton
    fun provideJourneyDao(database: AppDatabase): JourneyDao {
        return database.journeyDao()
    }

    @Provides
    @Singleton
    fun provideFootprintDao(database: AppDatabase): FootprintDao {
        return database.footprintDao()
    }

    @Provides
    @Singleton
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideMediaDao(database: AppDatabase): MediaDao {
        return database.mediaDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }
}