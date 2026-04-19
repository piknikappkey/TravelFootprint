// app/src/main/java/com/example/travel_footprint_android/data/database/AppDatabase.kt
package com.example.travel_footprint_android.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travel_footprint_android.data.entity.*
import com.example.travel_footprint_android.data.dao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [
        Journey::class,
        Footprint::class,
        Location::class,
        MediaAttachment::class,
        Tag::class,
        FootprintTagCrossRef::class,
        LightedCity::class
    ],
    version = 2,
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

    companion object {
        // 数据库迁移：添加 lighted_cities 表
        val MIGRATION_1_2 = object : Migration(1, 2) {
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
                // 创建索引
                database.execSQL("CREATE INDEX index_lighted_cities_cityAdcode ON lighted_cities(cityAdcode)")
                database.execSQL("CREATE INDEX index_lighted_cities_provinceAdcode ON lighted_cities(provinceAdcode)")
                database.execSQL("CREATE INDEX index_lighted_cities_lightedTime ON lighted_cities(lightedTime)")
            }
        }
    }
}