// app/src/main/java/com/example/travel_footprint_android/data/dao/LocationDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Location

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocation(location: Location)

    @Insert
    suspend fun insertAllLocations(locations: List<Location>)

    @Update
    suspend fun updateLocation(location: Location)

    @Query("SELECT * FROM locations WHERE footprintId = :footprintId ORDER BY orderIndex")
    suspend fun getLocationsByFootprint(footprintId: Long): List<Location>

    @Query("SELECT * FROM locations WHERE footprintId = :footprintId AND orderIndex = 0")
    suspend fun getPrimaryLocationByFootprint(footprintId: Long): Location?

    @Query("DELETE FROM locations WHERE footprintId = :footprintId")
    suspend fun deleteLocationsByFootprint(footprintId: Long)
}