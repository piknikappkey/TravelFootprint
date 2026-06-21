// app/src/main/java/com/example/travel_footprint_android/data/dao/LocationDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocation(location: Location)

    @Insert
    suspend fun insertAllLocations(locations: List<Location>)

    @Update
    suspend fun updateLocation(location: Location)

    @Query("SELECT * FROM locations WHERE footprintId = :footprintId ORDER BY idx")
    suspend fun getLocationsByFootprint(footprintId: Long): List<Location>

    @Query("SELECT * FROM locations WHERE footprintId = :footprintId AND idx = 0")
    suspend fun getPrimaryLocationByFootprint(footprintId: Long): Location?

    @Query("DELETE FROM locations WHERE footprintId = :footprintId")
    suspend fun deleteLocationsByFootprint(footprintId: Long)

    @Delete
    suspend fun deleteLocation(location: Location)

    @Query("UPDATE locations SET latitude = :latitude, longitude = :longitude, idx = :index WHERE footprintId = :footprintId")
    suspend fun updateLocationsByFootprint(footprintId: Long, latitude: Double, longitude: Double, index: Int)

    @Query("SELECT * FROM locations WHERE footprintId = :footprintId ORDER BY idx")
    fun getAddressesByFootprint(footprintId: Long): Flow<List <Location>>

    @Insert
    suspend fun addAddress(location: Location)

    @Query("UPDATE locations SET latitude = :latitude, longitude = :longitude, idx = :index WHERE footprintId = :footprintId AND id = :id")
    suspend fun setAddressByFootprint(id: Long, footprintId: Long, latitude: Double, longitude: Double, index: Int)
}