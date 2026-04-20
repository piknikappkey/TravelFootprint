// app/src/main/java/com/example/travel_footprint_android/data/dao/CityDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.City
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: City)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<City>)

    @Query("SELECT * FROM cities ORDER BY sortOrder, name")
    fun getAllCities(): Flow<List<City>>

    @Query("SELECT * FROM cities WHERE provinceAdcode = :provinceAdcode ORDER BY sortOrder, name")
    fun getCitiesByProvince(provinceAdcode: String): Flow<List<City>>

    @Query("SELECT * FROM cities WHERE adcode = :adcode")
    suspend fun getCityByAdcode(adcode: String): City?

    @Query("SELECT * FROM cities WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchCities(keyword: String): List<City>

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCityCount(): Int

    @Query("SELECT COUNT(*) FROM cities WHERE provinceAdcode = :provinceAdcode")
    suspend fun getCityCountByProvince(provinceAdcode: String): Int

    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()
}