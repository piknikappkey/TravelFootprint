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
    version = 4,
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
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `provinces` (
                        `adcode` TEXT NOT NULL PRIMARY KEY,
                        `name` TEXT NOT NULL,
                        `centerLat` REAL NOT NULL,
                        `centerLng` REAL NOT NULL,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0
                    )
                """)
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
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cities_provinceAdcode ON cities(provinceAdcode)")
            }
        }

        // 🆕 版本 3 到 4 的迁移：添加旅程地址和经纬度字段
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 address 列
                database.execSQL("ALTER TABLE `journeys` ADD COLUMN `address` TEXT NOT NULL DEFAULT ''")
                // 添加 longitude 列
                database.execSQL("ALTER TABLE `journeys` ADD COLUMN `longitude` REAL NOT NULL DEFAULT 0.0")
                // 添加 latitude 列
                database.execSQL("ALTER TABLE `journeys` ADD COLUMN `latitude` REAL NOT NULL DEFAULT 0.0")
                // 创建索引以提升按经纬度查询的性能
                database.execSQL("CREATE INDEX IF NOT EXISTS index_journeys_coordinates ON journeys(latitude, longitude)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_journal.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}