// app/src/main/java/com/example/travel_footprint_android/data/repository/LightedCityRepository.kt
package com.example.travel_footprint_android.data.repository

import android.util.Log
import com.example.travel_footprint_android.data.dao.LightedCityDao
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.dao.ProvinceCityCount
import com.example.travel_footprint_android.data.entity.LightedCity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LightedCityRepository @Inject constructor(
    private val lightedCityDao: LightedCityDao
) {



    /**
     * 点亮城市
     */
    suspend fun lightCity(
        cityAdcode: String,
        cityName: String,
        provinceAdcode: String,
        provinceName: String,
        latitude: Double,
        longitude: Double,
        remark: String = ""
    ): Long {
        // 检查是否已点亮
        if (lightedCityDao.isCityLighted(cityAdcode)) {
            Log.d("LightedCityRepo", "城市已点亮: $cityName")
            return -1
        }

        val city = LightedCity(
            cityAdcode = cityAdcode,
            cityName = cityName,
            provinceAdcode = provinceAdcode,
            provinceName = provinceName,
            lightedTime = Date(),
            latitude = latitude,
            longitude = longitude,
            remark = remark
        )
        return lightedCityDao.insertLightedCity(city)
    }

    /**
     * 取消点亮城市
     */
    suspend fun unlightCity(cityAdcode: String) {
        lightedCityDao.deleteLightedCityByAdcode(cityAdcode)
    }

    /**
     * 获取所有点亮城市
     */
    fun getAllLightedCities(): Flow<List<LightedCity>> = lightedCityDao.getAllLightedCities()

    /**
     * 获取点亮城市数量
     */
    suspend fun getLightedCityCount(): Int = lightedCityDao.getLightedCityCount()

    /**
     * 检查城市是否已点亮
     */
    suspend fun isCityLighted(cityAdcode: String): Boolean =
        lightedCityDao.isCityLighted(cityAdcode)

    /**
     * 清空所有点亮城市
     */
    suspend fun clearAllLightedCities() = lightedCityDao.clearAllLightedCities()

    /**
     * 获取所有已点亮的省份
     */
    suspend fun getLightedProvinces(): List<LightedProvince> =
        lightedCityDao.getDistinctProvinces()

    /**
     * 获取已点亮省份的数量
     */
    suspend fun getLightedProvinceCount(): Int =
        lightedCityDao.getLightedProvinceCount()

    /**
     * 检查省份是否已点亮
     */
    suspend fun isProvinceLighted(provinceAdcode: String): Boolean =
        lightedCityDao.isProvinceLighted(provinceAdcode)

    /**
     * 获取省份下所有点亮的城市
     */
    fun getLightedCitiesByProvince(provinceAdcode: String): Flow<List<LightedCity>> =
        lightedCityDao.getLightedCitiesByProvince(provinceAdcode)

    /**
     * 按省份统计点亮城市数量
     */
    suspend fun getLightedCitiesCountByProvince(): List<ProvinceCityCount> =
        lightedCityDao.getLightedCitiesCountByProvince()
}
