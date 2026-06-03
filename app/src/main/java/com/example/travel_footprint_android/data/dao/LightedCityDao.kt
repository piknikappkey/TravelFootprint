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

    @Query("""
    SELECT * FROM lighted_cities 
    WHERE cityAdcode NOT LIKE '%0000'  -- 排除省级（后4位为0）
    ORDER BY lightedTime DESC
""")
    fun getAllLightedCities(): Flow<List<LightedCity>>


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

    /**
     * 获取所有已点亮的省份（去重）
     */
    @Query("""
        SELECT DISTINCT provinceName, provinceAdcode 
        FROM lighted_cities 
        WHERE provinceAdcode LIKE '__0000'  -- 后4位为00的
        ORDER BY lightedTime DESC
    """)
    fun getDistinctProvinces(): Flow<List<LightedProvince>>

    /**
     * 获取已点亮省份的数量
     */
    @Query("""
        SELECT COUNT(DISTINCT provinceAdcode) 
        FROM lighted_cities 
        WHERE provinceAdcode LIKE '__0000'
    """)
    suspend fun getLightedProvinceCount(): Int

    /**
     * 检查省份是否已点亮（该省份下是否有任何城市被点亮）
     */
    @Query("SELECT EXISTS(SELECT 1 FROM lighted_cities WHERE provinceAdcode = :provinceAdcode)")
    suspend fun isProvinceLighted(provinceAdcode: String): Boolean


    /**
     * 获取省份下所有点亮的城市
     */
    @Query("SELECT * FROM lighted_cities WHERE provinceAdcode = :provinceAdcode ORDER BY lightedTime DESC")
    fun getLightedCitiesByProvince(provinceAdcode: String): Flow<List<LightedCity>>

    /**
     * 按省份分组统计点亮城市数量
     */
    @Query("""
        SELECT provinceName, provinceAdcode, COUNT(*) as cityCount 
        FROM lighted_cities 
        GROUP BY provinceAdcode 
        ORDER BY cityCount DESC
    """)
    suspend fun getLightedCitiesCountByProvince(): List<ProvinceCityCount>


    /**
     * 通过 cityAdcode 点亮城市
     * 如果已存在则更新，否则插入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun lightCityByAdcode(city: LightedCity): Long

    /**
     * 更新某个点亮城市记录的省份名称
     */
    @Query("UPDATE lighted_cities SET provinceName = :provinceName WHERE cityAdcode = :cityAdcode")
    suspend fun updateProvinceName(cityAdcode: String, provinceName: String)

    /**
     * 获取所有点亮城市记录（包括省级记录，用于修复数据）
     */
    @Query("SELECT * FROM lighted_cities ORDER BY lightedTime DESC")
    suspend fun getAllLightedCitiesSync(): List<LightedCity>

}

// 点亮省份数据类
data class LightedProvince(
    val provinceName: String,
    val provinceAdcode: String
)

// 省份点亮城市统计
data class ProvinceCityCount(
    val provinceName: String,
    val provinceAdcode: String,
    val cityCount: Int
)

