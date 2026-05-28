package com.example.travel_footprint_android.data.repository

import com.example.travel_footprint_android.data.dao.CheckInRecordDao
import com.example.travel_footprint_android.data.entity.CheckInRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRecordRepository @Inject constructor(
    private val checkInRecordDao: CheckInRecordDao
) {

    fun getAllRecords(): Flow<List<CheckInRecordEntity>> = checkInRecordDao.getAllRecords()

    fun getRecordsByCity(adcode: String): Flow<List<CheckInRecordEntity>> =
        checkInRecordDao.getRecordsByCity(adcode)

    suspend fun insertRecord(
        cityAdcode: String,
        cityName: String,
        note: String,
        tags: List<String> = emptyList(),
        photoPaths: List<String> = emptyList()
    ): Long {
        return withContext(Dispatchers.IO) {
            val record = CheckInRecordEntity(
                cityAdcode = cityAdcode,
                cityName = cityName,
                note = note,
                time = Date(),
                tags = tags,
                photoPaths = photoPaths
            )
            checkInRecordDao.insertRecord(record)
        }
    }

    suspend fun deleteRecordsByCity(adcode: String) {
        withContext(Dispatchers.IO) {
            checkInRecordDao.deleteRecordsByCity(adcode)
        }
    }
}