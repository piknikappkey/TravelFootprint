package com.example.travel_footprint_android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travel_footprint_android.data.entity.CheckInRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInRecordDao {

    @Query("SELECT * FROM check_in_records ORDER BY time DESC")
    fun getAllRecords(): Flow<List<CheckInRecordEntity>>

    @Query("SELECT * FROM check_in_records WHERE cityAdcode = :adcode ORDER BY time DESC")
    fun getRecordsByCity(adcode: String): Flow<List<CheckInRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CheckInRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: CheckInRecordEntity)

    @Query("DELETE FROM check_in_records WHERE cityAdcode = :adcode")
    suspend fun deleteRecordsByCity(adcode: String)

    @Query("SELECT * FROM check_in_records WHERE cityAdcode = :adcode ORDER BY time DESC LIMIT 1")
    suspend fun getLatestRecord(adcode: String): CheckInRecordEntity?
}