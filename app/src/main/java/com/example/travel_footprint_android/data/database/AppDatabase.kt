// app/src/main/java/com/example/travel_footprint_android/data/database/AppDatabase.kt
package com.example.travel_footprint_android.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
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
        FootprintTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journeyDao(): JourneyDao
    abstract fun footprintDao(): FootprintDao
    abstract fun locationDao(): LocationDao
    abstract fun mediaDao(): MediaDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_journal.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 数据库创建时的初始化操作
                            // 插入测试数据
                            INSTANCE?.let { database ->
                                // 使用协程在后台线程执行
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.journeyDao().insertJourney(
                                        Journey(
                                            title = "北京之旅",
                                            description = "第一次北京旅行",
                                            startDate = Date(),
                                            endDate = Date(),
                                            coverStyle = "watercolor",
                                            coverImagePath = "",
                                            journeyImagePaths = emptyList()
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
            }.also { INSTANCE = it }
        }
    }
}