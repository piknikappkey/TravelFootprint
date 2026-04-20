// app/src/main/java/com/example/travel_footprint_android/di/DatabaseModule.kt
package com.example.travel_footprint_android.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travel_footprint_android.data.dao.*
import com.example.travel_footprint_android.data.database.AppDatabase
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)  // 添加两个迁移
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

    @Provides
    @Singleton
    fun provideLightedCityDao(database: AppDatabase): LightedCityDao {
        return database.lightedCityDao()
    }

    // ========== 新增：省份和城市 DAO ==========
    @Provides
    @Singleton
    fun provideProvinceDao(database: AppDatabase): ProvinceDao {
        return database.provinceDao()
    }

    @Provides
    @Singleton
    fun provideCityDao(database: AppDatabase): CityDao {
        return database.cityDao()
    }

    // 数据库迁移：从版本1到版本2（添加 lighted_cities 表）
    private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `lighted_cities` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `cityAdcode` TEXT NOT NULL,
                    `cityName` TEXT NOT NULL,
                    `provinceAdcode` TEXT NOT NULL,
                    `provinceName` TEXT NOT NULL,
                    `lightedTime` INTEGER NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `remark` TEXT NOT NULL DEFAULT ''
                )
            """)
            // 创建索引以提高查询性能
            database.execSQL("CREATE INDEX IF NOT EXISTS index_lighted_cities_cityAdcode ON lighted_cities(cityAdcode)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_lighted_cities_provinceAdcode ON lighted_cities(provinceAdcode)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_lighted_cities_lightedTime ON lighted_cities(lightedTime)")
        }
    }

    // 数据库迁移：从版本2到版本3（添加 provinces 和 cities 表）
    private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建省份表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `provinces` (
                    `adcode` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `centerLat` REAL NOT NULL,
                    `centerLng` REAL NOT NULL,
                    `sortOrder` INTEGER NOT NULL DEFAULT 0
                )
            """)
            // 创建城市表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `cities` (
                    `adcode` TEXT NOT NULL PRIMARY KEY,
                    `name` TEXT NOT NULL,
                    `provinceAdcode` TEXT NOT NULL,
                    `centerLat` REAL NOT NULL,
                    `centerLng` REAL NOT NULL,
                    `sortOrder` INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(`provinceAdcode`) REFERENCES `provinces`(`adcode`) ON DELETE CASCADE
                )
            """)
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_cities_provinceAdcode ON cities(provinceAdcode)")
        }
    }
}