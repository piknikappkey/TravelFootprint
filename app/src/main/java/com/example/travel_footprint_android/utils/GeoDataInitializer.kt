// app/src/main/java/com/example/travel_footprint_android/utils/GeoDataInitializer.kt
package com.example.travel_footprint_android.utils

import android.content.Context
import android.util.Log
import com.example.travel_footprint_android.data.dao.CityDao
import com.example.travel_footprint_android.data.dao.ProvinceDao
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.Province
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object GeoDataInitializer {

    private const val TAG = "GeoDataInit"
    private const val JSON_FILE = "china_all_data.json"

    /**
     * 初始化省份和城市数据到数据库
     * 如果已有数据则跳过
     */
    suspend fun initializeData(
        context: Context,
        provinceDao: ProvinceDao,
        cityDao: CityDao
    ) = withContext(Dispatchers.IO) {
        // 检查是否已有数据
        val provinceCount = provinceDao.getProvinceCount()
        if (provinceCount > 0) {
            Log.d(TAG, "数据已存在，跳过初始化 (省份数: $provinceCount)")
            return@withContext
        }

        try {
            Log.d(TAG, "开始解析 JSON 文件...")
            val jsonString = context.assets.open(JSON_FILE)
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)

            // 1. 解析省份
            val provinces = parseProvinces(jsonObject)
            provinceDao.insertProvinces(provinces)
            Log.d(TAG, "✅ 成功插入 ${provinces.size} 个省份")

            // 2. 解析城市
            val cities = parseCities(jsonObject)
            cityDao.insertCities(cities)
            Log.d(TAG, "✅ 成功插入 ${cities.size} 个城市")

        } catch (e: Exception) {
            Log.e(TAG, "初始化失败: ${e.message}", e)
        }
    }

    /**
     * 解析省份数据
     */
    private fun parseProvinces(jsonObject: JSONObject): List<Province> {
        val provincesArray = jsonObject.getJSONArray("provinces")
        val provinces = mutableListOf<Province>()

        for (i in 0 until provincesArray.length()) {
            val item = provincesArray.getJSONObject(i)
            val adcode = item.getString("adcode")
            val name = item.getString("name")

            // 只添加省级行政区（adcode 以 0000 结尾）
            if (adcode.endsWith("0000")) {
                provinces.add(
                    Province(
                        adcode = adcode,
                        name = name,
                        centerLat = 0.0,
                        centerLng = 0.0,
                        sortOrder = i
                    )
                )
            }
        }
        return provinces
    }

    /**
     * 解析城市数据
     */
    private fun parseCities(jsonObject: JSONObject): List<City> {
        val citiesArray = jsonObject.getJSONArray("cities")
        val cities = mutableListOf<City>()
        var cityCount = 0

        for (i in 0 until citiesArray.length()) {
            val item = citiesArray.getJSONObject(i)
            val adcode = item.getString("adcode")
            val name = item.getString("name")
            val parentAdcode = item.getString("parent")

            // 只添加市级行政区（adcode 以 00 结尾，且不是 0000 结尾）
            if (adcode.endsWith("00") ||adcode.endsWith("0000")) {
                cities.add(
                    City(
                        adcode = adcode,
                        name = name,
                        provinceAdcode = parentAdcode,
                        centerLat = 0.0,
                        centerLng = 0.0,
                        sortOrder = cityCount++
                    )
                )
            }
        }
        return cities
    }
}