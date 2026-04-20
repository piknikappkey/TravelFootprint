// app/src/main/java/com/example/travel_footprint_android/data/database/AppDatabase.kt
package com.example.travel_footprint_android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travel_footprint_android.data.dao.*
import com.example.travel_footprint_android.data.entity.*

@Database(
    entities = [
        Journey::class,
        Footprint::class,
        Location::class,
        MediaAttachment::class,
        Tag::class,
        FootprintTagCrossRef::class,
        LightedCity::class,
        Province::class,
        City::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journeyDao(): JourneyDao
    abstract fun footprintDao(): FootprintDao
    abstract fun locationDao(): LocationDao
    abstract fun mediaDao(): MediaDao
    abstract fun tagDao(): TagDao
    abstract fun lightedCityDao(): LightedCityDao
    abstract fun provinceDao(): ProvinceDao
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 版本 2 到 3 的迁移
        val MIGRATION_2_3 = object : Migration(2, 3) {
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_journal.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}