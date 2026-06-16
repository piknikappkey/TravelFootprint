package com.example.travel_footprint_android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travel_footprint_android.data.dao.*
import com.example.travel_footprint_android.data.entity.CheckInRecordEntity
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.FootprintTagCrossRef
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.data.entity.MediaAttachment
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.data.entity.Tag

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
        City::class,
        CheckInRecordEntity::class
    ],
    version = 8,
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
    abstract fun checkInRecordDao(): CheckInRecordDao

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

        // 版本 3 到 4 的迁移：添加旅程地址和经纬度字段
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

        // 版本 4 到 5 的迁移：添加打卡记录表
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `check_in_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cityAdcode` TEXT NOT NULL,
                        `cityName` TEXT NOT NULL,
                        `note` TEXT NOT NULL DEFAULT '',
                        `time` INTEGER NOT NULL,
                        `tags` TEXT NOT NULL DEFAULT '[]',
                        `photoPaths` TEXT NOT NULL DEFAULT '[]'
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_check_in_records_cityAdcode ON check_in_records(cityAdcode)")
            }
        }

        // 版本 5 到 6 的迁移：足迹表新增字段，位置表orderIndex重命名为idx
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `startTime` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `duration` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `distance` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `speed` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `calories` REAL NOT NULL DEFAULT 0.0")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `locations_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `footprintId` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `idx` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`footprintId`) REFERENCES `footprints`(`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("""
                    INSERT INTO `locations_new` (`id`, `footprintId`, `latitude`, `longitude`, `idx`)
                    SELECT `id`, `footprintId`, `latitude`, `longitude`, `orderIndex` FROM `locations`
                """)
                database.execSQL("DROP TABLE `locations`")
                database.execSQL("ALTER TABLE `locations_new` RENAME TO `locations`")
            }
        }

        // 版本 6 到 7 的迁移：足迹表新增经度纬度字段
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `longitude` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `footprints` ADD COLUMN `latitude` REAL NOT NULL DEFAULT 0.0")
            }
        }

        // 版本 7 到 8 的迁移：重建所有表以匹配当前实体定义
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 重建 journeys 表
                database.execSQL("DROP TABLE IF EXISTS `journeys`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `journeys` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `coverStyle` TEXT NOT NULL,
                        `coverImagePath` TEXT NOT NULL,
                        `journeyImagePaths` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `longitude` REAL NOT NULL,
                        `latitude` REAL NOT NULL
                    )
                """)

                // 重建 footprints 表
                database.execSQL("DROP TABLE IF EXISTS `footprints`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `footprints` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `journeyId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `createTime` INTEGER NOT NULL,
                        `address` TEXT NOT NULL,
                        `longitude` REAL NOT NULL,
                        `latitude` REAL NOT NULL,
                        `rating` INTEGER NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `duration` INTEGER NOT NULL,
                        `distance` REAL NOT NULL,
                        `speed` REAL NOT NULL,
                        `calories` REAL NOT NULL,
                        FOREIGN KEY(`journeyId`) REFERENCES `journeys`(`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_footprints_journeyId` ON `footprints` (`journeyId`)")

                // 重建 locations 表
                database.execSQL("DROP TABLE IF EXISTS `locations`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `locations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `footprintId` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `idx` INTEGER NOT NULL,
                        FOREIGN KEY(`footprintId`) REFERENCES `footprints`(`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_locations_footprintId` ON `locations` (`footprintId`)")

                // 重建 media_attachments 表
                database.execSQL("DROP TABLE IF EXISTS `media_attachments`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `media_attachments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `footprintId` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `localPath` TEXT NOT NULL,
                        `thumbnailPath` TEXT NOT NULL,
                        `createTime` INTEGER NOT NULL,
                        `caption` TEXT NOT NULL,
                        FOREIGN KEY(`footprintId`) REFERENCES `footprints`(`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_attachments_footprintId` ON `media_attachments` (`footprintId`)")

                // 重建 tags 表
                database.execSQL("DROP TABLE IF EXISTS `tags`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` TEXT NOT NULL,
                        `usageCount` INTEGER NOT NULL
                    )
                """)

                // 重建 footprint_tag_cross_ref 表
                database.execSQL("DROP TABLE IF EXISTS `footprint_tag_cross_ref`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `footprint_tag_cross_ref` (
                        `footprintId` INTEGER NOT NULL,
                        `tagId` INTEGER NOT NULL,
                        PRIMARY KEY(`footprintId`, `tagId`),
                        FOREIGN KEY(`footprintId`) REFERENCES `footprints`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_footprint_tag_cross_ref_tagId` ON `footprint_tag_cross_ref` (`tagId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_footprint_tag_cross_ref_footprintId` ON `footprint_tag_cross_ref` (`footprintId`)")

                // 重建 lighted_cities 表
                database.execSQL("DROP TABLE IF EXISTS `lighted_cities`")
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
                        `remark` TEXT NOT NULL
                    )
                """)

                // 重建 provinces 表
                database.execSQL("DROP TABLE IF EXISTS `provinces`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `provinces` (
                        `adcode` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `centerLat` REAL NOT NULL,
                        `centerLng` REAL NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        PRIMARY KEY(`adcode`)
                    )
                """)

                // 重建 cities 表
                database.execSQL("DROP TABLE IF EXISTS `cities`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cities` (
                        `adcode` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `provinceAdcode` TEXT NOT NULL,
                        `centerLat` REAL NOT NULL,
                        `centerLng` REAL NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        PRIMARY KEY(`adcode`),
                        FOREIGN KEY(`provinceAdcode`) REFERENCES `provinces`(`adcode`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_cities_provinceAdcode` ON `cities` (`provinceAdcode`)")

                // 重建 check_in_records 表
                database.execSQL("DROP TABLE IF EXISTS `check_in_records`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `check_in_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cityAdcode` TEXT NOT NULL,
                        `cityName` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `time` INTEGER NOT NULL,
                        `tags` TEXT NOT NULL,
                        `photoPaths` TEXT NOT NULL
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_check_in_records_cityAdcode` ON `check_in_records` (`cityAdcode`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_journal.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}