// app/src/main/java/com/example/travel_footprint_android/data/repository/RegionRepository.kt
package com.example.travel_footprint_android.data.repository

import com.example.travel_footprint_android.data.dao.CityDao
import com.example.travel_footprint_android.data.dao.ProvinceDao
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.Province
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionRepository @Inject constructor(
    private val provinceDao: ProvinceDao,
    private val cityDao: CityDao
) {

    // 省份相关
    fun getAllProvinces(): Flow<List<Province>> = provinceDao.getAllProvinces()

    suspend fun getProvinceByAdcode(adcode: String): Province? =
        provinceDao.getProvinceByAdcode(adcode)

    suspend fun getProvinceCount(): Int = provinceDao.getProvinceCount()

    // 城市相关
    fun getAllCities(): Flow<List<City>> = cityDao.getAllCities()

    fun getCitiesByProvince(provinceAdcode: String): Flow<List<City>> =
        cityDao.getCitiesByProvince(provinceAdcode)

    suspend fun getCityByAdcode(adcode: String): City? =
        cityDao.getCityByAdcode(adcode)

    suspend fun getCityCount(): Int = cityDao.getCityCount()

    suspend fun getCityCountByProvince(provinceAdcode: String): Int =
        cityDao.getCityCountByProvince(provinceAdcode)

    // 添加搜索城市方法
    suspend fun searchCities(keyword: String): List<City> =
        cityDao.searchCities(keyword)
}