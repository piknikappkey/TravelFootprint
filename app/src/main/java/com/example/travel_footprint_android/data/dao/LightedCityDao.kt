// app/src/main/java/com/example/travel_footprint_android/data/dao/LightedCityDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.LightedCity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LightedCityDao {

    @Insert
    suspend fun insertLightedCity(city: LightedCity): Long

    @Insert
    suspend fun insertLightedCities(cities: List<LightedCity>)

    @Delete
    suspend fun deleteLightedCity(city: LightedCity)

    @Query("DELETE FROM lighted_cities WHERE cityAdcode = :adcode")
    suspend fun deleteLightedCityByAdcode(adcode: String)

    @Query("SELECT * FROM lighted_cities ORDER BY lightedTime DESC")
    fun getAllLightedCities(): Flow<List<LightedCity>>

    @Query("SELECT * FROM lighted_cities WHERE provinceAdcode = :provinceAdcode ORDER BY lightedTime DESC")
    fun getLightedCitiesByProvince(provinceAdcode: String): Flow<List<LightedCity>>

    @Query("SELECT * FROM lighted_cities WHERE cityAdcode = :adcode")
    suspend fun getLightedCityByAdcode(adcode: String): LightedCity?

    @Query("SELECT COUNT(*) FROM lighted_cities")
    suspend fun getLightedCityCount(): Int

    @Query("SELECT COUNT(*) FROM lighted_cities WHERE provinceAdcode = :provinceAdcode")
    suspend fun getLightedCityCountByProvince(provinceAdcode: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM lighted_cities WHERE cityAdcode = :adcode)")
    suspend fun isCityLighted(adcode: String): Boolean

    @Query("DELETE FROM lighted_cities")
    suspend fun clearAllLightedCities()
}